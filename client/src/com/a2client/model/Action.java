package com.a2client.model;

/**
 * действие доступное игроку в меню действий
 * Created by arksu on 13.10.15.
 */
public class Action
{
	public String name;

	/**
	 * список дочерних действий если есть (вложенность)
	 */
	public Action[] list;
}
