package com.a4server.gameserver.model.objects;

import com.a4server.gameserver.model.GameObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * шаблон для коллизий
 * Created by arksu on 23.02.15.
 */
public class CollisionTemplate
{
	private static final Logger _log = LoggerFactory.getLogger(CollisionTemplate.class.getName());

	/**
	 * применять для всех объектов? тогда список исключений работает как исключения. для них коллизий не будет
	 * если приенять ни для одного. тогда список исключений работает как список объектов которые будут давать коллизию
	 */
	@SerializedName("all")
	private boolean _allYes = true;

	/**
	 * список исключений
	 */
	@SerializedName("exclude")
	private List<String> _exclude = new ArrayList<>();

	public boolean getCollision(GameObject other)
	{
		if (_allYes)
		{
			return !_exclude.contains(other.getTemplate().getName());
		}
		else
		{
			return _exclude.contains(other.getTemplate().getName());
		}
	}
}
