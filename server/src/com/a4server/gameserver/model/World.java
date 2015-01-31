package com.a4server.gameserver.model;

import cern.colt.map.tobject.OpenIntObjectHashMap;
import com.a4server.Config;
import com.a4server.Database;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

import static com.a4server.gameserver.model.Grid.*;

/**
 * Created by arksu on 04.01.2015.
 */
public class World
{
    protected static final Logger _log = LoggerFactory.getLogger(World.class.getName());

    private Grid[][][] _grids; // x, y, level

    private FastList<Grid> _activeGrids = new FastList<Grid>(9).shared();

    /**
     * лок на создание грида
     */
    private final ReentrantLock _createGridLock = new ReentrantLock();

    /**
     * лок на добавление игрока в мир
     */
    private final ReentrantLock _addPlayerLock = new ReentrantLock();

    /**
     * список всех игроков которые находятся в игре
     */
    private final OpenIntObjectHashMap _allPlayers;

    public World()
    {
        _allPlayers = new OpenIntObjectHashMap();

        _log.info("World size: " + Config.WORLD_SG_WIDTH + "x" + Config.WORLD_SG_HEIGHT + " supergrids, " + Config.WORLD_LEVELS + " levels");

        _log.info("World allocate grids array mem...");

        // создаем массив гридов для хранения
        _grids = new Grid[Config.WORLD_SG_WIDTH * Grid.SUPERGRID_SIZE][Config.WORLD_SG_HEIGHT * Grid.SUPERGRID_SIZE][Config.WORLD_LEVELS];

        _log.info("World check tables...");
        checkSgTables();

        _log.info("World ready");
    }

    /**
     * есть ли игрок с указанным ид в мире
     *
     * @param objectId
     * @return
     */
    public Player getPlayer(int objectId)
    {
        return (Player) _allPlayers.get(objectId);
    }

    /**
     * добавить игрока с указанным ид в мир
     *
     * @param player игрок
     * @return если добавить не получилось вернет ложь, всегда проверять!
     */
    public boolean addPlayer(Player player)
    {
        boolean ret = false;
        _addPlayerLock.lock();
        try
        {
            // только если такого игрока еще нет в списке
            if (!_allPlayers.containsKey(player.getObjectId()))
            {
                _allPlayers.put(player.getObjectId(), player);
                ret = true;
            }

        }
        finally
        {
            _addPlayerLock.unlock();
        }
        return ret;
    }

    public boolean removePlayer(int objectId)
    {
        return _allPlayers.removeKey(objectId);
    }

    /**
     * проверим что существуют все таблицы с супергридами
     *
     * @return
     */
    private boolean checkSgTables()
    {
        int sg = 0;
        while (sg < Config.WORLD_SG_WIDTH * Config.WORLD_SG_HEIGHT)
        {
            if (!Database.getInstance().isTableExist("sg_" + sg))
            {
                _log.error("World: table [sg_" + sg + "] not exist in DB!");
                return false;
            }
            sg++;
        }
        return true;
    }

    /**
     * получить грид по координатам грида (сетка гридов)
     *
     * @param gx координаты грида
     * @param gy координаты грида
     * @return загруженный грид
     */
    public Grid getGrid(int gx, int gy, int level)
    {
        if (!validGrid(gx, gy))
        {
            return null;
        }
        if (!validLevel(level))
        {
            return null;
        }

        Grid g = _grids[gx][gy][level];

        if (g == null)
        {
            _createGridLock.lock();
            try
            {
                if (_grids[gx][gy][level] != null)
                {
                    _log.warn("Create grid: " + gx + "x" + gy + " it already exist in world");
                    return _grids[gx][gy][level];
                }
                g = new Grid(gx, gy, level);
                g.scheduleLoad();
                _grids[gx][gy][level] = g;

                if (Config.DEBUG)
                {
                    _log.debug("grid " + gx + "x" + gy + " lvl:" + level + " created");
                }
            }
            finally
            {
                _createGridLock.unlock();
            }
        }
        return g;
    }

    public void addActiveGrid(Grid g)
    {
        // только если в списке еще нет такого грида
        // нужно соблюдать уникальность
        if (!_activeGrids.contains(g))
        {
            _activeGrids.add(g);
        }
    }

    public void removeActiveGrid(Grid g)
    {
        _activeGrids.remove(g);
    }

    /**
     * получить грид зная абсолютные координаты в мире
     *
     * @param x
     * @param y
     * @param level
     * @return
     */
    public Grid getGridInWorldCoord(int x, int y, int level)
    {
        int gx = x / (GRID_FULL_SIZE);
        int gy = y / (GRID_FULL_SIZE);
        return (getGrid(gx, gy, level));
    }

    /**
     * находятся ли координаты грида в пределах мира?
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean validGrid(int x, int y)
    {
        return ((x >= 0) && (x < Config.WORLD_SG_WIDTH * Grid.SUPERGRID_SIZE) && (y >= 0) && (y < Config.WORLD_SG_HEIGHT * Grid.SUPERGRID_SIZE));
    }

    public static boolean validLevel(int level)
    {
        return (level >= 0 && level < Config.WORLD_LEVELS);
    }

    public static int getTileIndex(int x, int y)
    {
        int IndexX = (x % (GRID_FULL_SIZE)) / (Grid.TILE_SIZE);
        int IndexY = (y % (GRID_FULL_SIZE)) / (Grid.TILE_SIZE);
        return IndexX + IndexY * Grid.GRID_SIZE;
    }

    public static int getGridIndex(int x, int y, int lv)
    {
        int GridX = (x % (SUPERGRID_FULL_SIZE)) / (GRID_FULL_SIZE);
        int GridY = (y % (SUPERGRID_FULL_SIZE)) / (GRID_FULL_SIZE);
        return GridX + GridY * SUPERGRID_SIZE + lv * SUPERGRID_SIZE * SUPERGRID_SIZE;
    }

    public static World getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final World _instance = new World();
    }
}
