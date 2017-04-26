package com.a4server.gameserver.model.knownlist;

import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;

/**
 * список объектов о которых знает игрок
 * отправляем пакеты о добавлении / удалении объектов для клиента
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

		GameClient client = getActivePlayer().getClient();
		if (client != null)
		{
			client.sendPacket(object.makeRemoveFromWorldPacket());
		}
		return true;
	}
}
