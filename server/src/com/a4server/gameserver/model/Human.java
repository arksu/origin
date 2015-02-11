package com.a4server.gameserver.model;

import com.a4server.gameserver.model.event.AbstractObjectEvent;
import com.a4server.gameserver.model.event.EventMove;
import com.a4server.gameserver.model.position.ObjectPosition;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 02.02.15.
 * объект описывающий поведение живых, активных объектов (игроки, животные)
 */
public abstract class Human extends MoveObject
{
    protected static final Logger _log = LoggerFactory.getLogger(Human.class.getName());

    /**
     * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
     * любое добалвение в этот список, а равно как и удаление из него должно быть
     * синхронизировано с клиентом
     */
    protected FastList<GameObject> _knownKist = new FastList<>();

    /**
     * дистанция на которой мы видим объекты
     * может изменяться динамически (ночью видим хуже)
     */
    protected int _visibleDistance = 100;

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


    public void setVisibleDistance(int visibleDistance)
    {
        _visibleDistance = visibleDistance;
    }

    /**
     * обновить список видимых объектов
     * все новые что увидим - отправятся клиенту. старые что перестали видеть - будут удалены
     * @param force принудительно
     */
    public void UpdateVisibleObjects(boolean force)
    {
        // только если отошли достаточно далеко от последней позиции апдейта
        if (force || (getPos() != null && _lastVisibleUpdatePos != null &&
                !getPos().equals(_lastVisibleUpdatePos) &&
                getPos().getDistance(_lastVisibleUpdatePos) > VISIBLE_UPDATE_DISTANCE))
        {
            _log.debug("UpdateVisibleObjects " + toString());
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
                    if (isObjectVisibleForMe(o))
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
     * знаю ли я об указанном объекте (он есть на клиенте)
     * @param object объект
     * @return истина если знаю
     */
    protected boolean isKnownObject(GameObject object)
    {
        return _knownKist.contains(object);
    }

    /**
     * вижу ли я указаный объект
     * @param object другой объект
     * @return истина если я его вижу
     */
    public boolean isObjectVisibleForMe(GameObject object)
    {
        // по дефолту просто смотрим на расстояние мжеду нами
        return (getPos().getDistance(object.getPos()) < _visibleDistance);
    }

    /**
     * обработать игровое событие о котором мне сообщает другой объект
     * @param event событие
     * @return истина если событие обработано, значит событие нужно переправить клиенту,
     * значит к нему уйдет прикрепленный пакет
     */
    public boolean HandleEvent(AbstractObjectEvent event)
    {
        // событие движения
        if (event instanceof EventMove)
        {
            // знаю ли я этот объект?
            if (isKnownObject(event.getObject()))
            {
                // я больше не вижу объект
                if (!isObjectVisibleForMe(event.getObject()))
                {
                    // удалим его из списка видимых
                    removeKnownObject(event.getObject());
                    // т.к. объект мы больше не знаем. пакет слать не будем
                    return false;
                }
                // т.к. я его знаю отправим пакет клиенту
                return true;
            }
            else
            {
                // объекта я не знаю. проверим может я теперь вижу его?
                if (isObjectVisibleForMe(event.getObject()))
                {
                    // ага. вижу. добавим в список видимых
                    addKnownObject(event.getObject());
                    // мы видим объект, отправим пакет клиенту
                    return true;
                }
            }
        }
        return false;
    }
}
