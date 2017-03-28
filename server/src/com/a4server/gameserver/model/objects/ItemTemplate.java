package com.a4server.gameserver.model.objects;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.util.List;

/**
 * шаблон для описания предмета инвентаря
 * Created by arksu on 24.02.15.
 */
public class ItemTemplate
{
	@SerializedName("width")
	private int _width = 1;

	@SerializedName("height")
	private int _height = 1;

	@SerializedName("equip")
	private List<String> _equipSlots;

	private transient int _typeId;
	private transient String _name;

	private transient ObjectTemplate _objectTemplate;

	/**
	 * у вещи может быть вложенный инвентарь (какая-нибудь сумочка)
	 */
	private InventoryTemplate _inventory = null;

	public static ItemTemplate load(JsonReader in, int itemId, String name, ObjectTemplate objectTemplate)
	{
		Gson gson = new Gson();
		ItemTemplate template = gson.fromJson(in, ItemTemplate.class);
		template._typeId = itemId;
		template._name = name;
		template._objectTemplate = objectTemplate;
		return template;
	}

	public int getTypeId()
	{
		return _typeId;
	}

	public String getName()
	{
		return _name;
	}

	/**
	 * получить имя иконки которую будем отображать на клиенте
	 */
	public String getIconName()
	{
		return _name;
	}

	public int getWidth()
	{
		return _width;
	}

	public int getHeight()
	{
		return _height;
	}

	public List<String> getEquipSlots()
	{
		return _equipSlots;
	}

	public InventoryTemplate getInventory()
	{
		return _inventory;
	}

	public void setInventory(InventoryTemplate inventory)
	{
		_inventory = inventory;
	}

	public ObjectTemplate getObjectTemplate()
	{
		return _objectTemplate;
	}

	@Override
	public String toString()
	{
		return "(" + _name + " [" + _typeId + "] " + _width + "x" + _height +
		       (_inventory != null ? " " + _inventory.toString() : "") +
		       ")";
	}
}
