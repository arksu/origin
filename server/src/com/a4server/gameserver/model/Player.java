package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.model.position.ObjectPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 04.01.2015.
 */
public class Player extends MoveObject
{
    private static final Logger _log = LoggerFactory.getLogger(Player.class);

    private static final String LOAD_CHARACTER = "SELECT account, charName, x, y, lvl, face, hairColor, hairStyle, sex, lastAccess, onlineTime, title, createDate FROM characters WHERE charId = ?";


    private GameClient _client = null;
    private boolean _isOnline = false;
    private final PcAppearance _appearance;
    private String _account;
    /**
     * список гридов в которых находится игрок. 9 штук.
     */
    private List<Grid> _grids = new ArrayList<>();

    public Player(int objectId, ResultSet rset)
    {
        super(objectId);

        _typeId = 1;
        _appearance = new PcAppearance(rset);
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

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    public void onGridChanged()
    {

    }

    /**
     * корректно все освободить и удалить из мира
     */
    public void deleteMe()
    {
        // деактивировать занятые гриды
        for (Grid g : _grids) {
            g.deactivate(this);
        }

        // удалим игрока из мира
        if (!World.getInstance().removePlayer(getObjectId()))
        {
            _log.warn("deleteMe: World remove player fail");
        }

        if (_isOnline) {
            // также тут надо сохранить состояние перса в базу.
            storeInDb();
        }

    }

    public String getName()
    {
        return _name;
    }

    public List<Grid> getGrids()
    {
        return _grids;
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
     * получить окружающие гриды и дождаться их загрузки
     */
    public void loadGrids() throws Exception
    {
        _grids.clear();
        int gridX = getPos().getGridX();
        int gridY = getPos().getGridY();
        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                Grid grid = World.getInstance().getGrid(gridX + i, gridY + j, getPos()._level);
                if (grid != null)
                {
                    _grids.add(grid);
                }
            }
        }

        for (Grid grid : _grids)
        {
            grid.waitLoad();
        }
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
                throw new RuntimeException("fail to activate player grids");
            }
        }
        return true;
    }

    /**
     * все нужные гриды реально загружены
     *
     * @return
     */
    public boolean isGridsLoaded()
    {
        for (Grid g : _grids)
        {
            // если хоть 1 не готов
            if (!g.isLoaded())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * сохранить состояние персонажа в базу
     */
    public void storeInDb() {

    }

    @Override
    public int getSpeed()
    {
        return 10;
    }
}
