package com.a4server.gameserver.model;

import com.a4server.gameserver.model.position.ObjectPosition;
import javolution.util.FastList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 02.02.15.
 * объект описывающий поведение живых, активных объектов (игроки, животные)
 */
public abstract class Human extends MoveObject
{
    /**
     * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
     * любое добалвение в этот список, а равно как и удаление из него должно быть
     * синхронизировано с клиентом
     */
    protected FastList<GameObject> _knownKist = new FastList<>();

    /**
     * список гридов в которых находится объект. 9 штук.
     */
    protected List<Grid> _grids = new ArrayList<>();

    /**
     * дистанция на которой мы видим объекты
     * может изменяться динамически (ночью видим хуже)
     */
    protected int _visibleDistance = 30;

    /**
     * дистанция которую нужно пройти чтобы произошел апдейт видимых объектов
     */
    protected static final int VISIBLE_UPDATE_DISTANCE = 44;

    /**
     * последняя позиция в которой было обновление видимых объектов
     * нужно чтобы часто не обновлять список видимых (слишком накладно)
     */
    private ObjectPosition _lastVisibleUpdatePos = null;

    public Human(int objectId)
    {
        super(objectId);
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
     * изменился грид в котором находимся. надо отреагировать
     */
    public void onGridChanged()
    {
        // todo onGridChanged
        // надо обновить список гридов
        // новые активировать
        // старые деактивировать

    }

    public void setVisibleDistance(int visibleDistance)
    {
        _visibleDistance = visibleDistance;
    }

    /**
     * обновить список видимых объектов
     * все новые что увидим - отправятся клиенту. старые что перестали видеть - будут удалены
     *
     * @param force принудительно
     */
    public void UpdateVisibleObjects(boolean force)
    {
        // только если отошли достаточно далеко от последней позиции апдейта
        if (force || (getPos() != null && _lastVisibleUpdatePos != null &&
                !getPos().equals(_lastVisibleUpdatePos) &&
                getPos().getDistance(_lastVisibleUpdatePos) > VISIBLE_UPDATE_DISTANCE))
        {
            // запомним те объекты которые видимы при текущем апдейте
            FastList<GameObject> newList = new FastList<>();

            // проходим по всем гридам в которых находимся
            for (Grid g : _grids)
            {
                // по всем объектам в гридах
                FastList<GameObject> objs = g.getObjects();
                for (GameObject o : objs)
                {
                    // только если мы видим объект
                    if (isObjectVisible(o))
                    {
                        // добавим его в список видимых
                        addKnownObject(o);
                        newList.add(o);
                    }
                }
            }
            // какие объекты больше не видимы?
            FastList<GameObject> del = new FastList<>();
            for (GameObject o : _knownKist)
            {
                // если в новом списке нет - значит больше не видим,
                // пометим на удаление
                if (!newList.contains(o))
                {
                    del.add(o);
                }
            }
            // удалим объекты которые больше не видим
            for (GameObject o : del)
            {
                removeKnownObject(o);
            }
            // запомним последнее место где произвели апдейт
            if (getPos() != null)
            {
                _lastVisibleUpdatePos = getPos().clone();
            }
        }
    }

    protected void addKnownObject(GameObject object)
    {
        _knownKist.add(object);
    }

    protected void removeKnownObject(GameObject object)
    {
        _knownKist.remove(object);

    }

    /**
     * видим ли данный объект для меня
     *
     * @param object
     * @return
     */
    public boolean isObjectVisible(GameObject object)
    {
        // по дефолту просто смотрим на расстояние мжеду нами
        return (getPos().getDistance(object.getPos()) < _visibleDistance);
    }
}
