package com.a4server.gameserver.model.ai;

import com.a4server.gameserver.model.collision.CollisionResult;

/**
 * Created by arksu on 26.04.17.
 */
public interface ArrivedCallback
{
	void onArrived(CollisionResult moveResult);
}
