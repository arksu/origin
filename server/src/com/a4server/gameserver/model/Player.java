package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.model.position.MoveToPoint;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.MapGrid;
import com.a4server.gameserver.network.serverpackets.ObjectAdd;
import com.a4server.gameserver.network.serverpackets.PlayerAppearance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by arksu on 04.01.2015.
 */
public class Player extends Human
{
    private static final Logger _log = LoggerFactory.getLogger(Player.class);

    private static final String LOAD_CHARACTER = "SELECT account, charName, x, y, lvl, face, hairColor, hairStyle, sex, lastAccess, onlineTime, title, createDate FROM characters WHERE charId = ?";
    private static final String UPDATE_LAST_CHAR = "UPDATE accounts SET lastChar = ? WHERE login = ?";

    private GameClient _client = null;
    private boolean _isOnline = false;
    private final PcAppearance _appearance;
    private String _account;

    public Player(int objectId, ResultSet rset)
    {
        super(objectId);

        _typeId = 1;
        _appearance = new PcAppearance(rset, objectId);
        setVisibleDistance(1000);
        try
        {
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

    static public Player load(int objectId)
    {
        Player player = null;

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(LOAD_CHARACTER))
        {
            // Retrieve the L2PcInstance from the characters table of the database
            statement.setInt(1, objectId);
            try (ResultSet rset = statement.executeQuery())
            {
                if (rset.next())
                {
                    // загрузим игрока из строки базы
                    player = new Player(objectId, rset);
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
        if (isObjectVisible(object))
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
     *
     * @param object
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
     *
     * @param object
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
     *
     * @return
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
        // деактивировать занятые гриды
        for (Grid g : _grids)
        {
            g.deactivate(this);
        }
        if (getPos().getGrid() != null)
        {
            getPos().getGrid().removeObject(this);
        }

        // удалим игрока из мира
        if (!World.getInstance().removePlayer(getObjectId()))
        {
            _log.warn("deleteMe: World remove player fail");
        }

        if (_isOnline)
        {
            // также тут надо сохранить состояние перса в базу.
            storeInDb();
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
        if (_client != null)
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
     *
     * @return
     * @throws InterruptedException
     */
    public boolean activateGrids() throws InterruptedException
    {
        for (Grid g : _grids)
        {
            if (!g.activate(this))
            {
                throw new RuntimeException("fail to activate grids by " + this.toString());
            }
        }
        return true;
    }

    /**
     * сохранить состояние персонажа в базу
     */
    public void storeInDb()
    {
        // todo player storeInDb
    }
    
    public void UpdateLastChar() {
        try
        {
            try (Connection con = Database.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(UPDATE_LAST_CHAR))
            {
                ps.setInt(1, getObjectId());
                ps.setString(2, _account);
                ps.execute();
            }
        } catch (SQLException e) {
            
        }
    }

    /**
     * получить текущую скорость игрока
     * @return скорость в единицах координат в секунду (в тайле TILE_SIZE единиц)
     */
    @Override
    public int getSpeed()
    {
        return 10;
    }

    /**
     * двигаться к заданной точке на карте
     * @param x точка на карте
     * @param y точка на карте
     */
    public void MoveToPoint(int x, int y)
    {
        _moveController = new MoveToPoint(x, y);
    }
}
