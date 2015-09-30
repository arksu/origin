package com.a4server.gameserver.model.craft;

import com.a4server.gameserver.model.objects.ItemTemplate;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * шаблон для крафта
 * Created by arksu on 24.02.15.
 */
public class CraftTemplate
{
	private static final Logger _log = LoggerFactory.getLogger(CraftTemplate.class.getName());

	/**
	 * количество результата на выходе (сколько вещей получим)
	 */
	@SerializedName("count")
	private int _count = 1;

	/**
	 * список требуемых ингридиентов для крафта
	 */
	@SerializedName("required")
	private List<Ingridient> _required = new ArrayList<>();

	/**
	 * что получаем в результате крафта
	 */
	private ItemTemplate _resultTemplate;

	public class Ingridient
	{
		@SerializedName("name")
		private String _name;

		@SerializedName("count")
		private int _count = 1;
	}
}
