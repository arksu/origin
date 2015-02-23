package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.idfactory.IdFactory;
import com.a4server.gameserver.model.event.AbstractObjectEvent;
import com.a4server.gameserver.model.event.EventChatGeneralMessage;
import com.a4server.gameserver.model.objects.CollisionTemplate;
import com.a4server.gameserver.model.objects.InventoryTemplate;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.position.MoveToPoint;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.*;
import com.a4server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * игрок и все что с ним связано
 * Created by arksu on 04.01.2015.
 */
public class Player extends Human
{
    private static final Logger _log = LoggerFactory.getLogger(Player.class);

    private static final String LOAD_CHARACTER = "SELECT account, charName, x, y, lvl, accessLevel, face, hairColor, hairStyle, sex, lastAccess, onlineTime, title, createDate FROM characters WHERE del=0 AND charId = ?";
    private static final String UPDATE_LAST_CHAR = "UPDATE accounts SET lastChar = ? WHERE login = ?";
    private static final String UPDATE_CHARACTER = "UPDATE characters SET x=?, y=? WHERE charId=?";

    private GameClient _client = null;
    private volatile boolean _isOnline = false;
    private final PcAppearance _appearance;
    private String _account;
    private int _accessLevel;

    public Player(int objectId, ResultSet rset, PlayerTemplate template)
    {
        super(objectId, template);

        _appearance = new PcAppearance(rset, objectId);
        setVisibleDistance(500);
        try
        {
            _accessLevel = rset.getInt("accessLevel");
            _account = rset.getString("account");
            _name = rset.getString("charName");
            _title = rset.getString("title");

            //                    player._lastAccess = rset.getLong("lastAccess");
            //                    player.setOnlineTime(rset.getLong("onlinetime"));
            //                    player.getCreateDate().setTime(rset.getDate("createDate"));
            _pos = new ObjectPosition(rset.getInt("x"), rset.getInt("y"), rset.getInt("lvl"));
            _pos.setActiveObject(this);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static class PlayerTemplate implements ObjectTemplate
    {
        @Override
        public int getTypeId()
        {
            return 1;
        }

        @Override
        public int getWidth()
        {
            return 10;
        }

        @Override
        public int getHeight()
        {
            return 10;
        }

        @Override
        public String getName()
        {
            return "player";
        }

        @Override
        public CollisionTemplate getCollision()
        {
            return null;
        }

        @Override
        public InventoryTemplate getInventory()
        {
            return null;
        }
    }

    static public Player load(int objectId)
    {
        Player player = null;

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(LOAD_CHARACTER))
        {
            statement.setInt(1, objectId);
            try (ResultSet rset = statement.executeQuery())
            {
                if (rset.next())
                {
                    // загрузим игрока из строки базы
                    player = new Player(objectId, rset, new PlayerTemplate());
                }
            }

            if (player == null)
            {
                return null;
            }

            // восстанавливаем инвентарь и прочее

        }
        catch (Exception e)
        {
            _log.error("Failed loading character", e);
        }

        return player;
    }

    @Override
    protected void onEnterGrid(Grid grid)
    {
        if (getClient() != null)
        {
            getClient().sendPacket(new MapGrid(grid, getPos()._x, getPos()._y));
        }
    }

    /**
     * добавили объект в грид в котором находится игрок
     */
    public void onGridObjectAdded(GameObject object)
    {
        // тут проверим видим ли мы этот объект
        if (isObjectVisibleForMe(object))
        {
            addKnownObject(object);
        }
    }

    /**
     * грид говорит что какой то объект был удален
     */
    public void onGridObjectRemoved(GameObject object)
    {
        removeKnownObject(object);
    }

    /**
     * добавить объект в список видимых объектов
     * @param object объект
     */
    @Override
    protected void addKnownObject(GameObject object)
    {
        // такого объекта еще не было в списке
        if (!_knownKist.contains(object))
        {
            super.addKnownObject(object);
            getClient().sendPacket(object.makeAddPacket());
        }
    }

    /**
     * удалить из списка видимых объектов
     * @param object объект
     */
    @Override
    protected void removeKnownObject(GameObject object)
    {
        if (_knownKist.contains(object))
        {
            super.removeKnownObject(object);
            getClient().sendPacket(object.makeRemovePacket());
        }
    }

    /**
     * создать пакет для отсылки другим игрокам
     * @return пакет
     */
    public GameServerPacket makeAddPacket()
    {
        GameServerPacket pkt = new ObjectAdd(this);
        // раз это персонаж, отправим его представление, то как он должен выглядеть
        pkt.addNext(new PlayerAppearance(_appearance));
        return pkt;
    }

    /**
     * корректно все освободить и удалить из мира
     */
    public void deleteMe()
    {
        _log.debug("deleteMe");
        _isDeleteing = true;

        // деактивировать занятые гриды
        for (Grid g : _grids)
        {
            g.deactivate(this);
        }
        Grid grid = getPos().getGrid();
        if (grid != null)
        {
            try
            {
                grid.tryLock(Grid.MAX_WAIT_LOCK);
                grid.removeObject(this);
            }
            catch (InterruptedException e)
            {
                _log.warn("deleteMe: InterruptedException ");
                e.printStackTrace();
            }
            finally
            {
                grid.unlock();
            }
        }

        // удалим игрока из мира
        if (!World.getInstance().removePlayer(getObjectId()))
        {
            _log.warn("deleteMe: World remove player fail");
        }

        if (_isOnline)
        {
            _isOnline = false;
            // также тут надо сохранить состояние перса в базу.
            storeInDb();
            if (_moveController != null)
            {
                _moveController.setActiveObject(null);
                _moveController = null;
            }
            GameTimeController.getInstance().RemoveMovingObject(this);
        }

    }

    public boolean isOnline()
    {
        return _isOnline;
    }

    /**
     * выкинуть из игры
     */
    public void kick()
    {
        if (getClient() != null)
        {
            // выкинуть из игры
            getClient().closeNow();
        }
        else
        {
            deleteMe();
        }
    }

    public void setClient(GameClient client)
    {
        if (_client != null && client != null)
        {
            _log.warn("Player.client not null");
        }
        _client = client;
    }

    public GameClient getClient()
    {
        return _client;
    }

    public void setOnlineStatus(boolean status)
    {
        _isOnline = status;
    }


    /**
     * активировать окружающие гриды
     * @throws InterruptedException
     */
    public void activateGrids() throws InterruptedException
    {
        for (Grid g : _grids)
        {
            if (!g.activate(this))
            {
                throw new RuntimeException("fail to activate grids by " + this.toString());
            }
        }
    }

    /**
     * сохранить состояние персонажа в базу
     */
    @Override
    public void storeInDb()
    {
        // todo player storeInDb
        _log.debug("storeInDb " + toString());
        try
        {
            try (Connection con = Database.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(UPDATE_CHARACTER))
            {
                ps.setInt(1, getPos()._x);
                ps.setInt(2, getPos()._y);
                ps.setInt(3, getObjectId());
                ps.execute();
            }
        }
        catch (SQLException e)
        {
            _log.warn("failed: storeInDb " + e.getMessage());
        }
    }

    public void UpdateLastChar()
    {
        try
        {
            try (Connection con = Database.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(UPDATE_LAST_CHAR))
            {
                ps.setInt(1, getObjectId());
                ps.setString(2, _account);
                ps.execute();
            }
        }
        catch (SQLException e)
        {
            _log.warn("failed: UpdateLastChar " + e.getMessage());
        }
    }

    /**
     * получить текущую скорость игрока
     * @return скорость в единицах координат в секунду (в тайле TILE_SIZE единиц)
     */
    @Override
    public double getMoveSpeed()
    {
        return 15f;
    }

    /**
     * двигаться к заданной точке на карте
     * @param x точка на карте
     * @param y точка на карте
     */
    public void MoveToPoint(int x, int y)
    {
        _log.debug("MoveToPoint to (" + x + ", " + y + ")");
        // запустим движение. создадим контроллер для этого
        StartMove(new MoveToPoint(x, y));

        //randomGrid();
    }

    public void randomGrid()
    {
        int ido = 100;
        int gx = 44;
        int gy = 27;
        int grid;
        for (int i = 0; i < 2000; i++)
        {
            int rx = Rnd.get(0, Grid.GRID_FULL_SIZE) + (gx * 1200);
            int ry = Rnd.get(0, Grid.GRID_FULL_SIZE) + (gy * 1200);
            grid = rx / Grid.GRID_FULL_SIZE + ry / Grid.GRID_FULL_SIZE * Grid.SUPERGRID_SIZE;
            ido++;
            _log.debug("create obj " + ido);

            String q = "INSERT INTO sg_0_obj (id, grid, x, y, type, hp, create_tick) VALUES (?,?,?,?,?,?,?);";
            try
            {
                try (Connection con = Database.getInstance().getConnection();
                     PreparedStatement ps = con.prepareStatement(q))
                {
                    ps.setInt(1, ido);
                    ps.setInt(2, grid);
                    ps.setInt(3, rx);
                    ps.setInt(4, ry);
                    ps.setInt(5, 11);
                    ps.setInt(6, 100);
                    ps.setInt(7, GameTimeController.getInstance().getTickCount());
                    ps.execute();
                }
            }
            catch (SQLException e)
            {
                _log.warn("failed: storeInDb " + e.getMessage());
            }
        }
    }

    @Override
    public boolean HandleEvent(AbstractObjectEvent event)
    {
        if (event instanceof EventChatGeneralMessage)
        {
            EventChatGeneralMessage em = (EventChatGeneralMessage) event;
            if (em.getMessage().startsWith("/"))
            {
                if (_accessLevel >= 100)
                {
                    _log.debug("console command: " + em.getMessage());
                    if ("/randomgrid".equalsIgnoreCase(em.getMessage()))
                    {
                        randomGrid();
                    }
                    else if ("/nextid".equalsIgnoreCase(em.getMessage()))
                    {
                        IdFactory.getInstance().getNextId();
                        IdFactory.getInstance().getNextId();
                        IdFactory.getInstance().getNextId();
                        int nextId = IdFactory.getInstance().getNextId();
                        getClient().sendPacket(new CreatureSay(getObjectId(), "next id: " + nextId));
                        _log.debug("nextid: " + nextId);
                    }
                }

                // тут исполняем обычные команды доступные для всех

                // консольные команды проглотим
                return false;
            }
        }
        return super.HandleEvent(event);
    }
}
