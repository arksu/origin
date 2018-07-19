package com.a4server.gameserver;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.network.packets.serverpackets.GameServerPacket;

/**
 * рассылка различных уведомлений / событий / пакетов
 * Created by arksu on 28.03.17.
 */
public class Broadcast
{
	public static void toGrid(GameObject object, GameServerPacket pkt)
	{
		object.getGrid().broadcastPacket(object, pkt);
	}
}
