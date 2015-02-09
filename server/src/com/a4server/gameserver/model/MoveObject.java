package com.a4server.gameserver.model;

import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.position.MoveController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 09.01.2015.
 * объект который может передвигаться в мире
 */
public abstract class MoveObject extends GameObject
{
    protected static final Logger _log = LoggerFactory.getLogger(MoveObject.class.getName());

    /**
     * контроллер который управляет передвижением объекта
     */
    protected MoveController _moveController = null;

    /**
     * результат передвижения 
     */
    protected CollisionResult _moveResult = null;

    /**
     * список гридов в которых находится объект. 9 штук.
     */
    protected List<Grid> _grids = new ArrayList<>();

    public MoveObject(int objectId)
    {
        super(objectId);
    }

    /**
     * получить скорость объекта 
     * @return скорость в единицах координат в секунду
     */
    public abstract int getSpeed();

    public MoveController getMoveController()
    {
        return _moveController;
    }

    /**
     * начать передвижение объекта
     * @param controller
     */
    public void StartMove(MoveController controller)
    {
        _moveController = controller;
        _moveController.setActiveObject(this);
        _moveResult = null;
        GameTimeController.getInstance().AddMovingObject(this);
    }

    /**
     * прекратить движение объекта по той или иной причине
     */
    public void StopMove() {
        _moveController = null;
        _moveResult = null;
    }

    /**
     * прибыли в место назначения при передвижении
     */
    public void onArrived()
    {
        // занулим мув контроллер, чтобы корректно завершить движение
        _moveController = null;
    }

    public List<Grid> getGrids()
    {
        return _grids;
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
     * все нужные гриды реально загружены
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
     * изменился грид в котором находимся. надо отреагировать
     */
    public void onGridChanged()
    {
        try
        {
            // надо обновить список гридов
            ArrayList<Grid> newList = new ArrayList<>();
            int gridX = getPos().getGridX();
            int gridY = getPos().getGridY();

            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 1; j++)
                {
                    Grid grid = World.getInstance().getGrid(gridX + i, gridY + j, getPos()._level);
                    // если грид существует
                    if (grid != null)
                    {
                        // только новые гриды в которые мы входим
                        if (!_grids.contains(grid))
                        {
                            _grids.add(grid);
                            grid.waitLoad();
                            onEnterGrid(grid);
                        }
                        newList.add(grid);
                    }
                }
            }
            // старые гриды деактивируем
            if (!newList.isEmpty())
            {
                for (Grid grid : _grids)
                {
                    if (!newList.contains(grid))
                    {
                        onLeaveGrid(grid);
                    }
                }
            }
            _grids = newList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            _log.error("onGridChanged failed " + this.toString());
        }
    }

    /**
     * входим в новый для объекта грид, нужно отреагировать
     * @param grid
     */
    protected void onEnterGrid(Grid grid)
    {
    }

    /**
     * покидаем грид
     * @param grid
     */
    public void onLeaveGrid(Grid grid)
    {
    }
}
