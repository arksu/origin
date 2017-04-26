package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.ai.ArrivedCallback;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.VirtualObject;
import com.a4server.gameserver.model.event.GridEvent;
import com.a4server.gameserver.model.position.MoveToVirtual;

/**
 * Created by arksu on 26.04.17.
 */
public class MoveVirtualAI extends PlayerAI
{
	private final VirtualObject _virtual;

	private final ArrivedCallback _arrivedCallback;

	public MoveVirtualAI(Player player, VirtualObject virtual, ArrivedCallback callback)
	{
		super(player);
		_virtual = virtual;
		_arrivedCallback = callback;
	}

	@Override
	public void onArrived(CollisionResult moveResult)
	{
		if (_arrivedCallback != null)
		{
			_arrivedCallback.onArrived(moveResult);
		}
	}

	@Override
	public void onTick()
	{

	}

	@Override
	public void handleGridEvent(GridEvent gridEvent)
	{

	}

	@Override
	public void begin()
	{
		_player.startMove(new MoveToVirtual(_virtual));
	}
}
