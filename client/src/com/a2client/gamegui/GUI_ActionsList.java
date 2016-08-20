package com.a2client.gamegui;

import com.a2client.gui.GUIGDX;
import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_ListBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * список доступных действий игроку
 * крафт строительство и тд
 * Created by arksu on 12.08.16.
 */
public class GUI_ActionsList extends GUI_ListBox
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_ActionsList.class.getName());

	private class Item
	{
		String caption;
		String tag;

		public Item(String caption, String tag)
		{
			this.caption = caption;
			this.tag = tag;
		}
	}

	protected final List<Item> _list = new ArrayList<>();

	protected String _fontName = "default";

	public GUI_ActionsList(GUI_Control parent)
	{
		super(parent);
	}

	public String getItemCaption(int index)
	{
		if (index < 0 || index >= _list.size())
		{
			return "";
		}
		return _list.get(index).caption;
	}

	public String getItemTag(int index)
	{
		if (index < 0 || index >= _list.size())
		{
			return "";
		}
		return _list.get(index).tag;
	}

	@Override
	public int GetCount()
	{
		return _list.size();
	}

	@Override
	public int GetItemHeight(int index)
	{
		if (index < 0 || index >= _list.size())
		{
			return 0;
		}
		return GUIGDX.getTextHeight(_fontName, getItemCaption(index)) + 2;
	}

	@Override
	protected void DoDrawItem(int index, int x, int y, int w, int h)
	{
		GUIGDX.Text(_fontName, x, y, getItemCaption(index));
	}

	public void add(String tag, String caption)
	{
		_list.add(new Item(caption, tag));
		OnInsertItem(0);
	}

	public void clear()
	{
		_list.clear();
		OnDeleteItem(0);
	}
}
