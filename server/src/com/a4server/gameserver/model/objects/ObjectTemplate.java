package com.a4server.gameserver.model.objects;

/**
 * шаблон объекта по которому создаются конечные экземпляры
 * Created by arksu on 15.02.15.
 */
public interface ObjectTemplate
{
	/**
	 * уникальный идентификатор типа объекта
	 */
	int getTypeId();

	/**
	 * ширина объекта
	 */
	int getWidth();

	/**
	 * высота объекта
	 */
	int getHeight();

	/**
	 * имя шаблона
	 */
	String getName();

	CollisionTemplate getCollision();

	InventoryTemplate getInventory();

	ItemTemplate getItem();
}