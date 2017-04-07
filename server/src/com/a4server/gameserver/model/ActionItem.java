package com.a4server.gameserver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * действие доступное игроку. отображется в списке доступных действий
 * передается в виде json клиенту
 * Created by arksu on 13.10.15.
 */
public class ActionItem
{
	@SerializedName("name")
	private final String _name;

	@SerializedName("list")
	private List<ActionItem> _list;

	public ActionItem(String name)
	{
		_name = name;
	}

	public ActionItem(String name, List<ActionItem> list)
	{
		_name = name;
		_list = list;
	}

	public void add(ActionItem actionItem)
	{
		if (_list == null)
		{
			_list = new ArrayList<>();
		}
		_list.add(actionItem);
	}
}
