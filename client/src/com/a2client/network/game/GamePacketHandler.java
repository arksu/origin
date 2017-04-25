package com.a2client.network.game;

import com.a2client.network.game.serverpackets.*;
import com.a2client.screens.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GamePacketHandler
{
	private static final Logger _log = LoggerFactory.getLogger(GamePacketHandler.class.getName());

	private static Class<?>[] _pktClasses = {
			Init.class,
			CharacterCreateFail.class,
			CharacterList.class,
			CharInfo.class,
			CharSelected.class,
			MapGrid.class,
			ObjectAdd.class,
			ObjectRemove.class,
			PlayerAppearance.class,
			ServerClose.class,
			StatusUpdate.class,
			TimeUpdate.class,
			WorldInfo.class,
			ObjectMove.class,
			ObjectPos.class,
			CreatureSay.class,
			InventoryUpdate.class,
			ObjectInteractive.class,
			PlayerHand.class,
			EquipUpdate.class,
			ObjectState.class,
			ActionsList.class,
			CursorSet.class,
			ContextMenu.class,
			ActionProgress.class,
			ObjectLift.class
	};

	static public void InitPackets()
	{
		try
		{
			for (Class<?> c : _pktClasses)
			{
				Class.forName(c.getName(), true, c.getClassLoader());
			}
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	static private Map<Integer, Class<? extends GameServerPacket>> _packets = new HashMap<>();

	static public GameServerPacket handlePacket(byte[] data)
	{
		int opcode = data[0] & 0xff;

		GameServerPacket pkt = null;

		Class<? extends GameServerPacket> pktClass = _packets.get(opcode);
		if (pktClass != null)
		{
			try
			{
				pkt = pktClass.newInstance();
				// установим данные в пакет
				if (pkt != null)
				{
					pkt.setData(data);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Login.Error("unknown");
			}
		}
		else
		{
			debugOpcode(opcode);
		}

		return pkt;
	}

	static public void AddPacketType(int opcode, Class<? extends GameServerPacket> pkt)
	{
		_packets.put(opcode, pkt);
	}

	static private void debugOpcode(int opcode)
	{
		_log.info("Unknown game server packet opcode: " + opcode);
		Login.Error("packet_error");
	}
}
