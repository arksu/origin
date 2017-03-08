package com.a4server.gameserver.model.knownlist;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;

/**
 * Created by arksu on 08.03.17.
 */
public class PcKnownList extends ObjectKnownList
{
	public PcKnownList(GameObject activeObject)
	{
		super(activeObject);
	}

	protected Player getActivePlayer()
	{
		return ((Player) getActiveObject());
	}

	@Override
	public boolean addKnownObject(GameObject object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}

		getActivePlayer().getClient().sendPacket(object.makeAddToWorldPacket());
		return true;
	}

	@Override
	public boolean removeKnownObject(GameObject object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}

		getActivePlayer().getClient().sendPacket(object.makeRemoveFromWorldPacket());
		return true;
	}
}
