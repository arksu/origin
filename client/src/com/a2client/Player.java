package com.a2client;

import com.a2client.model.Action;
import com.a2client.model.Equip;
import com.a2client.model.Hand;
import com.a2client.screens.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * описывает класс моего персонажа и всего что с ним связано
 * параметры, статы, скиллы и прочее
 */
public class Player
{
	private static final Logger _log = LoggerFactory.getLogger(Player.class.getName());
	private static Player _instance;

	private int _objectId = -1;
	private String _name;

	private Hand _hand;
	private final Equip _equip = new Equip();

	private Action _rootAction;

	public void setObjectId(int myObjectId)
	{
		_objectId = myObjectId;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public void setName(String myName)
	{
		_name = myName;
	}

	public String getName()
	{
		return _name;
	}

	public static void init()
	{
		if (_instance != null)
		{
			_log.error("Player init: instance is not NULL!");
		}
		_instance = new Player();
	}

	public void dispose()
	{
		_instance = null;
	}

	public static Player getInstance()
	{
		return _instance;
	}

	public Hand getHand()
	{
		return _hand;
	}

	public void setHand(Hand hand)
	{
		if (_hand != hand)
		{
			if (hand != null)
			{
				hand.makeControl();
			}
			if (_hand != null)
			{
				_hand.dispose();
			}
			_hand = hand;
		}
	}

	public Equip getEquip()
	{
		return _equip;
	}

	public void setActions(Action rootAction)
	{
		_rootAction = rootAction;
		Game.getInstance()._actions.clear();
		for (Action action : _rootAction.list)
		{
			Game.getInstance()._actions.add(action.name, Lang.getTranslate("Game.action." + action.name));
		}
	}
}
