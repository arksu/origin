package com.a4server.gameserver.model.collision;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;

import java.util.List;

/**
 * Created by arksu on 07.01.2015.
 */
public class Collision
{
    public static CollisionResult checkCollision(GameObject object, int fromX, int fromY, int toX, int toY, Move.MoveType moveType, VirtualObject virtual, List<Grid> grids, int targetObjId) {
        return CollisionResult.NONE;
    }
}
