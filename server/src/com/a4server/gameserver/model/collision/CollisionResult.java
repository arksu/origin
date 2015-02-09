package com.a4server.gameserver.model.collision;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 07.01.2015.
 */
public class CollisionResult
{
    protected static final Logger _log = LoggerFactory.getLogger(CollisionResult.class.getName());

    public static final CollisionResult FAIL = new CollisionResult(CollisionType.COLLISION_FAIL);
    public static final CollisionResult NONE = new CollisionResult(CollisionType.COLLISION_NONE);

    public enum CollisionType
    {
        // обсчет коллизии не успешен
        COLLISION_FAIL,

        // нет коллизий
        COLLISION_NONE,

        // коллизия с тайлом
        COLLISION_TILE,

        // виртуальная коллизия
        COLLISION_VIRTUAL,

        // коллизия с объектом
        COLLISION_OBJECT,

        // с концом мира
        COLLISION_WORLD
    }

    private CollisionType _resultType;
    private Tile _tile = null;
    private GameObject _object = null;
    private VirtualObject _virtualObject = null;
    private int _x;
    private int _y;

    public CollisionResult(CollisionType resultType)
    {
        _resultType = resultType;
    }

    public CollisionResult(CollisionType resultType, int x, int y)
    {
        _resultType = resultType;
        _x = x;
        _y = y;
    }

    public CollisionResult(GameObject obj, int x, int y)
    {
        _resultType = CollisionType.COLLISION_OBJECT;
        _object = obj;
        _x = x;
        _y = y;
    }

    public CollisionResult(Tile tile, int x, int y)
    {
        _resultType = CollisionType.COLLISION_TILE;
        _tile = tile;
        _x = x;
        _y = y;
    }

    public CollisionResult(VirtualObject virtualObject)
    {
        _virtualObject = virtualObject;
    }

    public CollisionType getResultType()
    {
        return _resultType;
    }
}
