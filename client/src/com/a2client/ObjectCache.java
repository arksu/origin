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
    protected static final Logger _log = LoggerFactory.getLogger(ObjectCache.class.getName());
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
     *
     * @return
     */
    public GameObject getMe()
    {
        return _me;
    }

    public static void init()
    {
        if (_instance != null)
        {
            _log.error("Player init: instance is not NULL!");
        }
        _instance = new ObjectCache();
    }

    public void dispose()
    {
        _instance = null;
    }

    public static ObjectCache getInstance()
    {
        return _instance;
    }
}
