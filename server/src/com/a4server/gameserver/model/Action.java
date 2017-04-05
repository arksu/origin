package com.a4server.gameserver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * действие доступное игроку. передается в виде json клиенту
 * Created by arksu on 13.10.15.
 */
public class Action
{
	@SerializedName("name")
	private final String _name;

	@SerializedName("list")
	private List<Action> _list;

	public Action(String name)
	{
		_name = name;
	}

	public Action(String name, List<Action> list)
	{
		_name = name;
		_list = list;
	}

	public void add(Action action)
	{
		if (_list == null)
		{
			_list = new ArrayList<>();
		}
		_list.add(action);
	}
}
