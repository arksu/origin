package com.a4server.gameserver.model.position;

import com.a4server.Config;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.World;
import com.a4server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * описание позиции объекта в игровом мире
 * храним координаты. спавним привязанный объект в мир.
 * обрабатываем движение и обновляем координаты
 */
public class ObjectPosition
{
    protected static final Logger _log = LoggerFactory.getLogger(ObjectPosition.class.getName());

    public volatile int _x;
    public volatile int _y;
    private volatile int _z;
    /**
     * уровень земли
     */
    public volatile int _level;
    /**
     * грид в котором находимся
     */
    private Grid _grid;
    /**
     * объект родитель чьи координаты описываем
     */
    private GameObject _activeObject;

    public ObjectPosition(int x, int y, int level)
    {
        _x = x;
        _y = y;
        _z = 0;
        _level = level;
        _grid = null;
    }

    /**
     * пробуем заспавнить привязанный объект в мир
     * ищем грид по координатам
     * просим у грида чтобы он добавил нас в себя
     *
     * @return истина если получилось
     */
    public boolean trySpawn()
    {
        // получаем грид в указанной позиции
        Grid grid = World.getInstance().getGridInWorldCoord(_x, _y, _level);
        if (grid != null && _activeObject != null)
        {
            try
            {
                boolean success;
                // сначала пытаемся 5 раз заспавнить в указанные координаты
                for (int tries = 0; tries < 5; tries++)
                {
                    success = grid.trySpawn(_activeObject);
                    if (success)
                    {
                        _grid = grid;
                        return true;
                    }
                    Thread.sleep(Rnd.get(20, 120));
                }
                // ежели не получилось туда. спавним рядом
                for (int tries = 0; tries < 5; tries++)
                {
                    success = grid.trySpawnNear(_activeObject, 20);
                    if (success)
                    {
                        _grid = grid;
                        return true;
                    }
                    Thread.sleep(Rnd.get(20, 120));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean trySpawnRandom() {
        int tries = 5;
        while (tries > 0) {
            setRandomPostion();
            if (trySpawn()) {
                return true;
            }
            tries--;
        }
        return false;
    }

    public void setActiveObject(GameObject activeObject)
    {
        _activeObject = activeObject;
    }

    public GameObject getActiveObject()
    {
        return _activeObject;
    }

    /**
     * изменить координаты в пределах уровня
     *
     * @param x
     * @param y
     */
    public void setXY(int x, int y)
    {
        _x = x;
        _y = y;
        updateGrid();
    }

    /**
     * обновить привязку к гриду если перешли в другой
     */
    private void updateGrid()
    {
        Grid new_grid = World.getInstance().getGridInWorldCoord(_x, _y, _level);
        if (_grid != new_grid)
        {
            setGrid(new_grid);
        }

    }

    /**
     * установить грид в котором находимся
     *
     * @param value
     */
    public void setGrid(Grid value)
    {
        if ((_grid != null) && (getActiveObject() != null))
        {
            if (value != null)
            {
                // ставим не нулл
                // в старом гриде надо обновить состояние зон
            }
            else
            {
                // ставим нулл
                // в старом гриде удалим игрока из всех зон
            }
        }

        if (getActiveObject() instanceof Player)
        {
            ((Player) getActiveObject()).onGridChanged();
        }
        _grid = value;
    }

    public Grid getGrid()
    {
        return _grid;
    }

    /**
     * получить X в координатах гридов
     *
     * @return
     */
    public int getGridX()
    {
        return _x / (Grid.GRID_FULL_SIZE);
    }

    /**
     * получить Y в координатах гридов
     *
     * @return
     */
    public int getGridY()
    {
        return _y / (Grid.GRID_FULL_SIZE);
    }

    /**
     * установить случайную позицию на поверхности мира
     */
    private void setRandomPostion()
    {
        _level = 0;
        _x = Rnd.get(Config.WORLD_SG_WIDTH * Grid.SUPERGRID_FULL_SIZE);
        _y = Rnd.get(Config.WORLD_SG_HEIGHT * Grid.SUPERGRID_FULL_SIZE);
    }

}
