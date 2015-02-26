package com.a2client;

import com.a2client.model.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * тут храним объекты которые нам присылает сервер
 * Created by arksu on 06.02.15.
 */
public class ObjectCache
{
    private static final Logger _log = LoggerFactory.getLogger(ObjectCache.class.getName());
    private static ObjectCache _instance;

    List<GameObject> _objects = new ArrayList<>();

    /**
     * объект моего персонажа
     */
    GameObject _me;

    public void addObject(GameObject object)
    {
        _objects.add(object);
        if (object.getObjectId() == Player.getInstance().getObjectId())
        {
            _me = object;
        }
    }

    public GameObject getObject(int objectId)
    {
        for (GameObject o : _objects)
        {
            if (o.getObjectId() == objectId)
            {
                return o;
            }
        }
        return null;
    }

    public void removeObject(int objectId)
    {
        GameObject toRemove = null;
        for (GameObject o : _objects)
        {
            if (o.getObjectId() == objectId)
            {
                toRemove = o;
                break;
            }
        }
        if (toRemove != null)
        {
            _objects.remove(toRemove);
            if (_me == toRemove)
            {
                _me = null;
            }
        }
    }

    public List<GameObject> getObjects()
    {
        return _objects;
    }

    public void clear()
    {
        _objects.clear();
        _me = null;
    }

    /**
     * получить объект моего персонажа
     * @return
     */
    public GameObject getMe()
    {
        return _me;
    }

    public static ObjectCache getInstance()
    {
        if (_instance == null) {
            _instance = new ObjectCache();
        }
        return _instance;
    }
}
