package com.a2client;

import com.a2client.gamegui.GUI_ActionsList;
import com.a2client.model.Action;
import com.a2client.model.EquipWindow;
import com.a2client.model.Hand;
import com.a2client.screens.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * описывает класс моего персонажа и всего что с ним связано
 * параметры, статы, скиллы и прочее
 */
public class PlayerData
{
	private static final Logger _log = LoggerFactory.getLogger(PlayerData.class.getName());
	private static PlayerData _instance;

	private int _objectId = -1;

	/**
	 * никнейм игрока
	 */
	private String _name;

	/**
	 * что держим в руке
	 */
	private Hand _hand;

	/**
	 * эквип игрока
	 */
	private final EquipWindow _equipWindow = new EquipWindow();

	/**
	 * доступные действия для игрока
	 */
	private List<Action> _actions;

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
			_log.error("PlayerData init: instance is not NULL!");
		}
		_instance = new PlayerData();
	}

	public void dispose()
	{
		_instance = null;
	}

	public static PlayerData getInstance()
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

	public EquipWindow getEquipWindow()
	{
		return _equipWindow;
	}

	public void setActions(List<Action> list)
	{
		_actions = list;

		GUI_ActionsList actions = Game.getInstance()._rootActions;

		actions.clear();
		for (Action action : _actions)
		{
			actions.add(action);
		}
		actions.place();
	}
}
