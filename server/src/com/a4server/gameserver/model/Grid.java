package com.a4server.gameserver.model;

/**
 * один игровой грид. хранит в себе объекты которые территориально находятся внутри него.
 * осуществляет взаимодействие игроков между собой которые активировали его
 * Created by arksu on 04.01.2015.
 */

import com.a4server.Config;
import com.a4server.Database;
import com.a4server.ThreadPoolManager;
import com.a4server.gameserver.model.collision.Collision;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.model.collision.VirtualObject;
import com.a4server.gameserver.model.event.Event;
import com.a4server.util.Rect;
import com.a4server.util.Rnd;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * участок карты 1 грид (100 на 100 тайлов)
 * хранит в себе объекты этого грида
 */
public class Grid
{
    private static final Logger _log = LoggerFactory.getLogger(Grid.class.getName());

    private static final String LOAD_OBJECTS = "SELECT id, x, y, type, hp, data, create_tick, last_tick FROM sg_0_obj WHERE del=0 AND grid = ?";
    private static final String LOAD_GRID = "SELECT data, last_tick FROM sg_0 WHERE id = ?";

    /**
     * размер одного тайла в игровых единицах длины
     */
    public static final int TILE_SIZE = 12;
    /**
     * количество тайлов в стороне грида
     */
    public static final int GRID_SIZE = 100;
    /**
     * количество гридов в супергриде
     */
    public static final int SUPERGRID_SIZE = 50;
    /**
     * длина стороны грида в игровых единицах
     */
    public static final int GRID_FULL_SIZE = GRID_SIZE * TILE_SIZE;
    /**
     * полная длина супергрида
     */
    public static final int SUPERGRID_FULL_SIZE = GRID_FULL_SIZE * SUPERGRID_SIZE;
    /**
     * размер блоба для хранения массива тайлов
     */
    public static final int GRID_BLOB_SIZE = GRID_SIZE * GRID_SIZE * 2;

    public static final int MAX_WAIT_LOCK = 1000;

    /**
     * массив тайлов
     */
    private Tile[] _tiles;
    private byte[] _blob;
    /**
     * последний игровой тик когда обновлялся грид
     */
    private int _last_tick;

    /**
     * координаты грида в мире (грид сетка)
     */
    private int _x, _y, _level;
    private int _sg, _grid;
    /**
     * координаты начальной точки грида в мировых координатах
     */
    private int _wx, _wy;

    private boolean _loaded;
    private boolean _loading;
    private Future<?> _loadFuture;
    /**
     * лок на загрузку грида. загружать можно только одним потоком. и только 1 раз
     */
    private ReentrantLock _loadLock = new ReentrantLock();

    /**
     * лок на все операции по гриду
     */
    private ReentrantLock _mainLock = new ReentrantLock();
    /**
     * список игроков которые поддерживают этот грид активным
     */
    private FastList<Player> _activePlayers = new FastList<Player>().shared();
    /**
     * список объектов в гриде
     */
    private FastList<GameObject> _objects = new FastList<GameObject>().shared();

    /**
     * создаем грид
     * @param x координата грида по сетке гридов (не игровых координат!)
     * @param y координата
     */
    public Grid(int x, int y, int level)
    {
        _x = x;
        _y = y;
        _wx = x * GRID_FULL_SIZE;
        _wy = y * GRID_FULL_SIZE;
        // номер супергрида
        _sg = _x / SUPERGRID_SIZE + (_y / SUPERGRID_SIZE) * Config.WORLD_SG_HEIGHT;
        // номер грида внутри супергрида
        _grid = _x % SUPERGRID_SIZE + (_y % SUPERGRID_SIZE) * SUPERGRID_SIZE + _level * SUPERGRID_SIZE * SUPERGRID_SIZE;
        _level = level;
        _loaded = false;
        _loading = false;
        _log.debug("grid create " + toString());
    }

    public FastList<GameObject> getObjects()
    {
        return _objects;
    }

    /**
     * добавить объект в грид
     * перед вызовомгрид обязательно должен быть залочен!!!
     * @param object
     */
    public void addObject(GameObject object) throws RuntimeException
    {
        // если грид не залочен - бросим исключение
        if (!_mainLock.isLocked())
        {
            throw new RuntimeException("addObject: grid is not locked");
        }

        // проверим есть ли уже такой объект в гриде
        if (!_objects.contains(object))
        {
            _objects.add(object);

            // надо проинформировать всех о добавлении объекта
            if (isActive())
            {
                for (Player pl : _activePlayers)
                {
                    pl.onGridObjectAdded(object);
                }
            }
        }
    }

    /**
     * удалить объект из грида
     * @param object объект
     */
    public void removeObject(GameObject object)
    {
        // если грид не залочен - бросим исключение
        if (!_mainLock.isLocked())
        {
            throw new RuntimeException("removeObject: grid is not locked");
        }

        if (_objects.contains(object))
        {
            object.unlinkFromAll();
            _objects.remove(object);

            // надо проинформировать всех о добавлении объекта
            if (isActive())
            {
                for (Player pl : _activePlayers)
                {
                    pl.onGridObjectRemoved(object);
                }
            }
        }
    }

    /**
     * получить тайл грида
     * @param index индекс внутри массива тайлов
     * @return тайл
     */
    public Tile getTile(int index)
    {
        return _tiles[index];
    }

    /**
     * получить сырые данные массива тайлов
     * @return
     */
    public byte[] getTilesBlob()
    {
        return _blob;
    }

    public int getCoordX()
    {
        return _wx;
    }

    public int getCoordY()
    {
        return _wy;
    }

    public int getLevel()
    {
        return _level;
    }

    /**
     * создать задание на загрузку грида
     */
    public void scheduleLoad()
    {
        _loadFuture = ThreadPoolManager.getInstance().scheduleGeneral(new LoadGridTask(this), 0);
    }

    public Future<?> getLoadFuture()
    {
        return _loadFuture;
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    /**
     * подождать полной загрузки грида, блокирует вызывающий поток
     * если в процессе загрузки произошла ошибка бросит исключение
     * @throws GridLoadException
     */
    public void waitLoad() throws GridLoadException
    {
        try
        {
            // подождем выполнения задания
            _loadFuture.get(2000, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            throw new GridLoadException("future get failed", e);
        }
        // если после его выполнения грид не загружен - ошибка
        if (!_loaded)
        {
            throw new GridLoadException("grid not loaded");
        }
    }

    /**
     * исключение при попытке загрузить грид
     */
    public class GridLoadException extends Exception
    {
        public GridLoadException(String message)
        {
            super(message);
        }

        public GridLoadException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    /**
     * задание загрузки грида
     */
    class LoadGridTask implements Runnable
    {
        private final Grid _grid;

        public LoadGridTask(Grid grid)
        {
            _grid = grid;
        }

        @Override
        public void run()
        {
            _grid.load();
        }
    }

    /**
     * загрузить грид
     * тайлы и объекты в нем
     */
    public void load()
    {
        // если загружен или в процессе загрузки - выходим
        if (_loaded || _loading)
        {
            return;
        }

        // если не можем захватить лок на загрузку, выходим
        // ктото другой уже загружает этот грид
        if (!_loadLock.tryLock())
        {
            _log.warn("failed lock for load grid " + toString());
            return;
        }

        // если загружен или в процессе загрузки - выходим
        if (_loaded || _loading)
        {
            _log.warn("grid already loaded or loading " + toString());
            _loadLock.unlock();
            return;
        }
        try
        {
            _loading = true;
            loadInternal();
            _loaded = true;
        }
        finally
        {
            // снимаем лок
            _loadLock.unlock();
            _loading = false;
        }
    }

    /**
     * загрузка грида из базы
     */
    private void loadInternal()
    {
        String query = LOAD_GRID;
        query = query.replaceFirst("sg_0", "sg_" + Integer.toString(_sg));

        try
        {
            try (Connection con = Database.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(query))
            {
                ps.setInt(1, _grid);
                try (ResultSet rset = ps.executeQuery())
                {
                    if (rset.next())
                    {
                        _last_tick = rset.getInt("last_tick");
                        _blob = rset.getBlob("data").getBytes(1, GRID_BLOB_SIZE);
                        loadTiles();
                    }
                }
            }
        }
        catch (SQLException e)
        {
            _log.warn("Cant load grid " + toString());
            throw new RuntimeException("Cant load grid " + toString());
        }

        // загрузить объекты
        query = LOAD_OBJECTS;
        query = query.replaceFirst("sg_0", "sg_" + Integer.toString(_sg));

        int objLoaded = 0;
        try
        {
            try (Connection con = Database.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(query))
            {
                ps.setInt(1, _grid);
                try (ResultSet rset = ps.executeQuery())
                {
                    while (rset.next())
                    {
                        loadObject(rset);
                        objLoaded++;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            _log.warn("Cant load grid " + toString());
            throw new RuntimeException("Cant load grid " + toString());
        }
        _log.debug("objects loaded: " + objLoaded);
    }

    /**
     * загрузить объект грида
     */
    private void loadObject(ResultSet rset) throws SQLException
    {
        GameObject object = new GameObject(this, rset);
        _objects.add(object);
    }

    /**
     * загрузить тайлы. (создать объекты тайлов)
     */
    private void loadTiles()
    {
        _log.debug("grid " + toString() + " load tiles...");
        _tiles = new Tile[GRID_SIZE * GRID_SIZE];
        int n = 0;
        for (int x = 0; x < GRID_SIZE; x++)
        {
            for (int y = 0; y < GRID_SIZE; y++)
            {
                _tiles[n] = new Tile(_blob[n]);
                n++;
            }
        }
        _log.debug("grid " + toString() + " tiles loaded");
    }

    public CollisionResult trySpawn(GameObject player) throws Exception
    {
        return trySpawnNear(player, 0, false);
    }

    /**
     * попробовать заспавнить рядом с исходными координатами игрока
     * грид может быть и не активирован
     * @param object игрок
     * @param len максимально допустимое расстояние спавна от исходной точки
     * @param moveLen двигать ли пробуя переместить на указанную длину, либо просто пытаемся заспавнуть в ту точку сразу
     * @return null=false, успешно только если результат коллизий вернет COLLISION_NONE
     */
    public CollisionResult trySpawnNear(GameObject object, int len, boolean moveLen) throws Exception
    {
        if (object.getPos()._level != _level)
        {
            _log.warn("trySpawn not in player grid! " + object + " " + this.toString());
            return null;
        }

        int x = object.getPos()._x;
        int y = object.getPos()._y;
        if (x < (_x * GRID_FULL_SIZE) || x >= ((_x + 1) * GRID_FULL_SIZE) ||
                y < (_y * GRID_FULL_SIZE) || y >= ((_y + 1) * GRID_FULL_SIZE))
        {
            _log.warn("trySpawn: player coord not in grid! " + object + " " + this.toString());
            return null;
        }


        int toX = x;
        int toY = y;
        // если длина для сдвига есть, ищем случайную точку рядом
        if (len > 0)
        {
            boolean found = false;
            for (int i = 0; i < 30; i++)
            {
                int dx = Rnd.get(len * 2) - len;
                int dy = Rnd.get(len * 2) - len;
                // сдвигаем подальше от изначальной позиции
                if (Math.abs(dx) < TILE_SIZE && Math.abs(dy) < TILE_SIZE)
                {
                    continue;
                }
                if ((dx * dx + dy * dy) < (len * len))
                {
                    toX += dx;
                    toY += dy;
                    // конечные координаты должны быть в моем гриде
                    if (!pointInsideMe(toX, toY))
                    {
                        continue;
                    }
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                return null;
            }
        }

        // если еще не загружен - подождем загрузки грида
        if (!_loaded)
        {
            waitLoad();
        }

        // пытаемся захватить лок на грид
        if (!_mainLock.tryLock(MAX_WAIT_LOCK, TimeUnit.MILLISECONDS))
        {
            return null;
        }

        try
        {
            // в любом случае обновим грид до начала проверок коллизий
            updateGrid();

            // обсчитаем коллизию
            CollisionResult result;
            _log.debug("try spawn (" + x + ", " + y + ") >> (" + toX + ", " + toY + ")");
            if (moveLen)
            {
                result = checkCollision(object, x, y, toX, toY, Move.MoveType.MOVE_SPAWN, null, false);
            }
            else
            {
                result = checkCollision(object, toX, toY, toX, toY, Move.MoveType.MOVE_SPAWN, null, false);
            }

            switch (result.getResultType())
            {
                // только если нет коллизий
                case COLLISION_NONE:
                    // добавим в объекты грида
                    addObject(object);

                    // если сдвигали - обновим позицию
                    if (len > 0)
                    {
                        object.getPos().setXY(toX, toY);
                    }

                    break;
            }
            return result;
        }
        finally
        {
            _mainLock.unlock();
        }
    }

    /**
     * точка в мировых координатах находится внутри грида
     * @param x
     * @param y
     * @return
     */
    public boolean pointInsideMe(int x, int y)
    {
        return (x >= _x * GRID_FULL_SIZE && x < (_x + 1) * GRID_FULL_SIZE && y >= _y * GRID_FULL_SIZE && y < (_y + 1) * GRID_FULL_SIZE);
    }

    /**
     * полное обновление состояния грида. его "жизнь"
     */
    public void updateGrid()
    {
        // TODO updateGrid
    }

    /**
     * проверить коллизию при передвижении
     * @param isMove в результате обсчета надо еще переместить объект между гридами (если перешли из одного в другой)
     * @return результат обсчета в виде CollisionResult
     */
    public synchronized CollisionResult checkCollision(GameObject object,
                                                       int fromX, int fromY,
                                                       int toX, int toY,
                                                       Move.MoveType moveType,
                                                       VirtualObject virtual,
                                                       boolean isMove)
            throws GridLoadException
    {
        if (!_mainLock.isLocked())
        {
            return CollisionResult.FAIL;
        }

        // посмотрим сколько нам нужно гридов для проверки коллизий
        Rect r = new Rect(fromX, fromY, toX, toY);
        // расширим рект на размер объекта
        r.add(object.getBoundRect());

        int gx, gy;
        Grid grid;
        // список гридов которые участвуют в расчете коллизии
        List<Grid> grids = new ArrayList<>();
        grids.add(this);
        try
        {
            // смотрим на 4 точки прямоугольника. в каких гридах они находятся
            // пробуем залочить каждый из них и внести в список гридов по которым будем искать коллизии
            gx = r.getLeft();
            gy = r.getTop();
            grid = World.getInstance().getGridInWorldCoord(gx, gy, _level);
            // если такой грид еще не в списке
            if (!grids.contains(grid))
            {
                // дождемся его загрузки
                grid.waitLoad();
                // если это не мы
                if (grid != this)
                {
                    // пробуем залочить для обсчета коллизий
                    if (!tryLockForCollision(grid))
                    {
                        return CollisionResult.FAIL;
                    }
                    else
                    {
                        grids.add(grid);
                    }
                }
            }

            gx = r.getRight();
            gy = r.getTop();
            grid = World.getInstance().getGridInWorldCoord(gx, gy, _level);
            if (!grids.contains(grid))
            {
                grid.waitLoad();
                if (grid != this)
                {
                    if (!tryLockForCollision(grid))
                    {
                        return CollisionResult.FAIL;
                    }
                    else
                    {
                        grids.add(grid);
                    }
                }
            }

            gx = r.getRight();
            gy = r.getBottom();
            grid = World.getInstance().getGridInWorldCoord(gx, gy, _level);
            if (!grids.contains(grid))
            {
                grid.waitLoad();
                if (grid != this)
                {
                    if (!tryLockForCollision(grid))
                    {
                        return CollisionResult.FAIL;
                    }
                    else
                    {
                        grids.add(grid);
                    }
                }
            }

            gx = r.getLeft();
            gy = r.getBottom();
            grid = World.getInstance().getGridInWorldCoord(gx, gy, _level);
            if (!grids.contains(grid))
            {
                grid.waitLoad();
                if (grid != this)
                {
                    if (!tryLockForCollision(grid))
                    {
                        return CollisionResult.FAIL;
                    }
                    else
                    {
                        grids.add(grid);
                    }
                }
            }

            // гриды залочены, проходим итерациями и ищем коллизию
            CollisionResult result = Collision
                    .checkCollision(object, fromX, fromY, toX, toY, moveType, virtual, grids, 0);

            // узнаем переместился ли объект из одного грида в другой
            // и только в том случае если в передвижении участвует больше 1 грида
            if (isMove && grids.size() > 1)
            {
                int fx = toX;
                int fy = toY;
                if (result.getResultType() != CollisionResult.CollisionType.COLLISION_NONE)
                {
                    fx = result.getX();
                    fy = result.getY();
                }
                grid = World.getInstance().getGridInWorldCoord(fx, fy, _level);
                // реально передвинулись в тот грид
                if (grid != this && grids.contains(grid))
                {
                    // все условия соблюдены. знаем куда надо передвинуть объект
                    // поскольку все гриды залочены - спокойно делаем свое черное дело
                    // вся эта байда с залочиванием и получением списка гридов делалась именно для этого
                    // получился свой велосипед по типу транзакций для обеспечения целостности данных между гридами
                    _log.debug("swap grids " + object + " " + this + " >> " + grid);
                    _objects.remove(object);
                    grid._objects.add(object);
                }
            }

            return result;
        }
        finally
        {
            for (Grid g : grids)
            {
                if (g != this)
                {
                    g.unlock();
                }
            }
        }
    }

    public boolean tryLock(int time) throws InterruptedException
    {
        return _loaded && _mainLock.tryLock(time, TimeUnit.MILLISECONDS);
    }

    public void unlock()
    {
        if (_loaded)
        {
            _mainLock.unlock();
        }
    }

    /**
     * попробовать залочить для обсчета коллизий
     * @param grid
     * @return
     */
    private boolean tryLockForCollision(Grid grid)
    {
        try
        {
            return grid.tryLock(MAX_WAIT_LOCK);
        }
        catch (InterruptedException e)
        {
            _log.warn("Timeout wait tryLockForCollision " + grid);
            return false;
        }
    }

    /**
     * активировать грид
     * только пока есть хоть 1 игрок связанный с гридом - он будет считатся активным
     * если ни одного игрока нет грид становится не активным и не обновляет свое состояние
     * @param player игрок который связывается с гридом
     * @return только если удалось активировать
     */
    public boolean activate(Player player) throws InterruptedException
    {
        if (!_loaded)
        {
            _log.warn("Try to activate non loaded grid! " + this.toString());
            return false;
        }

        // если уже активирован этим игроком - выходим
        if (_activePlayers.contains(player))
        {
            return true;
        }

        // пытаемся захватить лок на грид
        if (!_mainLock.tryLock(MAX_WAIT_LOCK, TimeUnit.MILLISECONDS))
        {
            return false;
        }

        try
        {
            // если грид был НЕ активен. проведем полный обсчет грида
            if (!isActive())
            {
                updateGrid();
            }

            if (!_activePlayers.contains(player))
            {
                _log.debug("activate " + toString() + " " + player.toString());
                _activePlayers.add(player);
            }

            // скажем миру что этот грид теперь активен. и его надо обновлять
            World.getInstance().addActiveGrid(this);
        }
        finally
        {
            _mainLock.unlock();
        }
        return true;
    }

    public void deactivate(Player player)
    {
        _activePlayers.remove(player);
        // если грид больше не активен
        if (_activePlayers.isEmpty())
        {
            // скажем миру что обновлять этот грид больше не надо
            World.getInstance().removeActiveGrid(this);
        }
    }

    public boolean isActive()
    {
        return !_activePlayers.isEmpty();
    }

    /**
     * разослать всем игрокам грида событие на которое они должны отреагировать
     */
    public void broadcastEvent(Event event)
    {
        for (Player p : _activePlayers)
        {
            // если игрок обработал это событие. и оно касается его
            if (!p.isDeleteing() && p.HandleEvent(event))
            {
                // отправим пакет
                p.getClient().sendPacket(event.getPacket());
            }
            
            // это все была базовая обработка событий связанная с рассылкой пакетов
            // а теперь разошлем всем мозгам эти же события
            // также тут потом будет еще проверочка на дистанцию до события... незачем мозгу знать о далеких вещах
            if (p.getMind() != null)
            {
                p.getMind().handleEvent(event);
            }
        }
    }

    @Override
    public String toString()
    {
        return "(" + _x + ", " + _y + " lvl=" + _level + " sg=" + _sg + " id=" + _grid + ")";
    }
}
