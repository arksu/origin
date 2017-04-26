package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.g3d.Model;
import com.a2client.model.Character;
import com.a2client.model.GameObject;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arksu on 26.04.17.
 */
public class ObjectLift extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x26, ObjectLift.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(ObjectLift.class.getName());

	private int _parentObjectId;

	/**
	 * слоты с ид прилинкованных объектов
	 */
	private Map<Integer, Integer> _lift = new HashMap<>();

	@Override
	public void readImpl()
	{
		_parentObjectId = readD();
		int c = readC();
		while (c > 0)
		{
			c--;
			int slot = readC();
			int id = readD();
			_lift.put(slot, id);
		}
	}

	@Override
	public void run()
	{
		_log.debug("lift: " + _parentObjectId + " sz=" + _lift.size());
		GameObject parent = ObjectCache.getInstance().getObject(_parentObjectId);
		if (parent != null)
		{
			// это игрок?
			if (parent instanceof Character)
			{
				Character character = ((Character) parent);
				if (_lift.size() > 0)
				{
					GameObject lift = ObjectCache.getInstance().getObject(_lift.get(0));
					if (lift != null)
					{
						_log.debug("lift up: " + lift);
						Model model = lift.getModel();
						model.setPos(0, 4.3f, 0);
						model.setHeading(0, true);
						character.getModel().addChild(model);
						character.getLift().put(0, model);

						character.getModel().playMergeAnimation("arms_up");

						character.getEquip().unbindHands(true);
					}
				}
				else
				{
					// список пуст. надо убрать все над головой и опустить руки
					character.getEquip().unbindHands(false);
					Model model = character.getLift().get(0);
					character.getModel().removeChild(model);

					character.getModel().removeMergeAnimation("arms_up");
				}
			}
		}
	}
}
