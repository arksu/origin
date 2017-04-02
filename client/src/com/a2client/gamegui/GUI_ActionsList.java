package com.a2client.gamegui;

import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Icon;
import com.a2client.model.Action;
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

	public GUI_ActionsList(GUI_Control parent)
	{
		super(parent);
	}

	public void add(Action action)
	{
		_actions.add(action);

		GUI_Icon icon = new GUI_Icon(action.name, GUI.rootNormal());
		icon.setPos(200, 200);
		_guiIcons.add(icon);
	}

	public void clear()
	{
		_actions.clear();
	}

	public void doClick(Action action) {}
}
