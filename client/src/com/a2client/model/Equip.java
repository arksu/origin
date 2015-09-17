package com.a2client.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 17.09.15.
 */
public class Equip
{
	private static final Logger _log = LoggerFactory.getLogger(Equip.class.getName());

	private final List<EquipSlot> _items = new ArrayList<>();

	public List<EquipSlot> getItems()
	{
		return _items;
	}

	/**
	 * вызывается при любых изменениях эквипа, нужно среагировать и обновить содержимое на экране
	 */
	public void onChange()
	{
		_log.debug("onChange");
	}
}
