package com.a2client.gamegui;

import com.a2client.Lang;
import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Icon;
import com.a2client.model.Action;
import com.a2client.model.InventoryItem;
import com.badlogic.gdx.Gdx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * список доступных действий игроку
 * крафт строительство и тд
 * Created by arksu on 12.08.16.
 */
public class GUI_ActionsList extends GUI_Control
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_ActionsList.class.getName());

	private final List<Action> _actions = new ArrayList<>();

	private final List<GUI_Icon> _guiIcons = new ArrayList<>();

	private GUI_ActionsList _childs;

	public GUI_ActionsList _parentList;

	public int _level = 1;

	public int _startPos = 0;

	public GUI_ActionsList(GUI_Control parent)
	{
		super(parent);
	}

	public void add(final Action action)
	{
		_actions.add(action);

		GUI_Icon icon = new GUI_Icon(action.name, GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				// если это контейнер для вложенного списка действий - откроем его
				if (action.list != null && action.list.length > 0)
				{
					_childs = new GUI_ActionsList(GUI.rootNormal())
					{
						@Override
						public void doClick(Action action)
						{
							GUI_ActionsList.this.doClick(action);
						}
					};
					for (Action a : action.list)
					{
						_childs.add(a);
					}
					_childs._parentList = GUI_ActionsList.this;
					_childs._level = GUI_ActionsList.this._level + 1;
					_childs._startPos = this.pos.y;
					_childs.place();
				}
				else
				{
					GUI_ActionsList.this.doClick(action);
					GUI_ActionsList list = GUI_ActionsList.this;
					while (list != null && list._parentList != null)
					{
						list.clear();
						list.unlink();
						list = list._parentList;
					}
				}
			}
		};
		icon._simpleHint = Lang.getTranslate("Game.action." + action.name);
		_guiIcons.add(icon);
	}

	public void place()
	{
		int y;
		if (_parentList == null)
		{
			y = Gdx.graphics.getHeight() - _guiIcons.size() * (InventoryItem.HEIGHT + InventoryItem.MARGIN);
		}
		else
		{
			y = _startPos;
		}
		int x = Gdx.graphics.getWidth() - (InventoryItem.WIDTH + InventoryItem.MARGIN) * _level;
		for (GUI_Icon icon : _guiIcons)
		{
			icon.setPos(x, y);
			y += InventoryItem.HEIGHT + InventoryItem.MARGIN;
		}
	}

	public void clear()
	{
		for (GUI_Icon icon : _guiIcons)
		{
			icon.unlink();
		}
		_actions.clear();
	}

	public void doClick(Action action) {}
}
