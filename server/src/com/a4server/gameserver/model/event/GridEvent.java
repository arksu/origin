package com.a4server.gameserver.model.event;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.network.packets.serverpackets.GameServerPacket;

/**
 * базовое игровое событие
 * рассылается объектами или гридом всем живым объектам внутри грида
 * к событию может быть прикреплен пакет. тогда он будет разослан всем клиентам которые обработали событие
 * а также дополнительная информация _info описывающая событие
 * Created by arksu on 11.02.15.
 */
public class GridEvent
{
	public enum EventType
	{
		/**
		 * тип события по умолчанию. прикрепленный пакет шлется всем у кого он в known списке
		 */
		EVT_DEFAULT,
		EVT_START_MOVE,
		EVT_MOVE,
		EVT_STOP_MOVE,
		EVT_CHAT_MESSAGE,
	}

	/**
	 * объект который сгенерировал событие
	 * к которому оно относится
	 */
	protected final GameObject _initiator;

	/**
	 * тип сообщения
	 */
	protected final EventType _type;

	/**
	 * дополнительная информация о событии
	 */
	protected Object[] _info = null;

	/**
	 * пакет прикрепленный к событию. если другой объект обработал это событие
	 * то пакет будет отослан ему
	 */
	protected GameServerPacket _packet = null;

	public GridEvent(GameObject initiator, EventType type)
	{
		_initiator = initiator;
		_type = type;
	}

	public GridEvent(GameObject initiator, EventType type, GameServerPacket pkt)
	{
		_initiator = initiator;
		_type = type;
		_packet = pkt;
	}

	public GridEvent(GameObject initiator, EventType type, Object... info)
	{
		_initiator = initiator;
		_type = type;
		_info = info;
	}

	public GameObject getInitiator()
	{
		return _initiator;
	}

	public EventType getType()
	{
		return _type;
	}

	public GameServerPacket getPacket()
	{
		return _packet;
	}

	public void setPacket(GameServerPacket pkt)
	{
		_packet = pkt;
	}

	public Object[] getInfo()
	{
		return _info;
	}

	@Override
	public String toString()
	{
		return "(event " + _initiator +
		       " type=" + _type +
		       (_info != null ? " info=" + _info : "") +
		       ")";
	}
}
