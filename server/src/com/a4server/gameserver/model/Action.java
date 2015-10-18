package com.a4server.gameserver.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by arksu on 13.10.15.
 */
public class Action
{
	@SerializedName("name")
	private final String _name;

	@SerializedName("list")
	private Action[] _list;

	public Action(String name)
	{
		_name = name;
	}

	public Action(String name, Action[] list)
	{
		_name = name;
		_list = list;
	}
}
