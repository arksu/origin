package com.a4server.gameserver.model;

import com.a4server.Config;
import com.a4server.gameserver.model.ai.AI;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.position.ObjectPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * объект описывающий поведение живых, активных объектов (игроки, животные)
 * Created by arksu on 02.02.15.
 */
public abstract class Human extends MovingObject
{
	private static final Logger _log = LoggerFactory.getLogger(Human.class.getName());

	/**
	 * ИИ объекта который определяет его поведение, реагирует на все события
	 */
	protected AI _ai = null;

	/**
	 * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
	 * любое добалвение в этот список, а равно как и удаление из него должно быть
	 * синхронизировано с клиентом
	 */
	protected Map<Integer, GameObject> _knownKist = new ConcurrentHashMap<>();

	/**
	 * дистанция на которой мы видим объекты
	 * может изменяться динамически (ночью видим хуже)
	 */
	protected int _visibleDistance = 100;

	/**
	 * последняя позиция в которой было обновление видимых объектов
	 * нужно чтобы часто не обновлять список видимых (слишком накладно)
	 */
	private ObjectPosition _lastVisibleUpdatePos = null;

	public Human(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	public AI getAi()
	{
		return _ai;
	}

	public void setVisibleDistance(int visibleDistance)
	{
		_visibleDistance = visibleDistance;
	}

	/**
	 * обновить список видимых объектов
	 * все новые что увидим - отправятся клиенту. старые что перестали видеть - будут удалены
	 * @param force принудительно
	 */
	public void updateVisibleObjects(boolean force)
	{
		// только если отошли достаточно далеко от последней позиции апдейта
		if (force || (
				getPos() != null && _lastVisibleUpdatePos != null &&
				!getPos().equals(_lastVisibleUpdatePos) &&
				getPos().getDistance(_lastVisibleUpdatePos) > Config.VISIBLE_UPDATE_DISTANCE
		))
		{
			_log.debug("updateVisibleObjects " + toString());
			// запомним те объекты которые видимы при текущем апдейте
			List<GameObject> newList = new LinkedList<>();

			// проходим по всем гридам в которых находимся
			for (Grid grid : _grids)
			{
				// по всем объектам в гридах
				for (GameObject o : grid.getObjects())
				{
					// только если мы видим объект
					if (isObjectVisibleForMe(o))
					{
						// добавим его в список видимых
						addKnownObject(o);
						newList.add(o);
					}
				}
			}
			// какие объекты больше не видимы?
			List<GameObject> del = new LinkedList<>();
			for (GameObject o : _knownKist.values())
			{
				// если в новом списке нет - значит больше не видим,
				// пометим на удаление
				if (!newList.contains(o))
				{
					del.add(o);
				}
			}
			// удалим объекты которые больше не видим
			for (GameObject o : del)
			{
				removeKnownObject(o);
			}
			// запомним последнее место где произвели апдейт
			if (getPos() != null)
			{
				_lastVisibleUpdatePos = getPos().clone();
			}
		}
	}

	protected void addKnownObject(GameObject object)
	{
		_knownKist.put(object.getObjectId(), object);
	}

	protected void removeKnownObject(GameObject object)
	{
		_knownKist.remove(object.getObjectId());
	}

	/**
	 * знаю ли я об указанном объекте (он есть на клиенте)
	 * @param object объект
	 * @return истина если знаю
	 */
	public boolean isKnownObject(GameObject object)
	{
		return _knownKist.containsKey(object.getObjectId());
	}

	public GameObject isKnownObject(int objectId)
	{
		for (GameObject object : _knownKist.values())
		{
			if (!object.isDeleteing() && object.getObjectId() == objectId)
			{
				return object;
			}
		}
		return null;
	}

	/**
	 * вижу ли я указаный объект
	 * @param object другой объект
	 * @return истина если я его вижу
	 */
	public boolean isObjectVisibleForMe(GameObject object)
	{
		// по дефолту просто смотрим на расстояние мжеду нами
		// себя всегда видим!
		return object.getObjectId() == getObjectId() || (getPos().getDistance(object.getPos()) < _visibleDistance);
	}

	/**
	 * обработать игровое событие о котором мне сообщает другой объект
	 * @param event событие
	 * @return истина если событие обработано, значит событие нужно переправить клиенту,
	 * значит к нему уйдет прикрепленный пакет
	 */
	public boolean handleEvent(Event event)
	{
		// событие движения
		switch (event.getType())
		{
			case EVT_MOVE:
			case EVT_STOP_MOVE:
				// знаю ли я этот объект?
				if (isKnownObject(event.getInitiator()))
				{
					// я больше не вижу объект
					if (!isObjectVisibleForMe(event.getInitiator()))
					{
						// удалим его из списка видимых
						removeKnownObject(event.getInitiator());
						// т.к. объект мы больше не знаем. пакет слать не будем
						return false;
					}
					// т.к. я его знаю отправим пакет клиенту
					return true;
				}
				else
				{
					// объекта я не знаю. проверим может я теперь вижу его?
					if (isObjectVisibleForMe(event.getInitiator()))
					{
						// ага. вижу. добавим в список видимых
						addKnownObject(event.getInitiator());
						// мы видим объект, отправим пакет клиенту
						return true;
					}
				}
				break;

			case EVT_DEFAULT:
				return event.getInitiator() == null || isKnownObject(event.getInitiator());

			case EVT_CHAT_GENERAL_MESSAGE:
				return onChatMessage(event);

			case EVT_INTERACT:
				// если мы знаем такой объект - пошлем пакет клиенту
				return isKnownObject(event.getInitiator());
		}
		return false;
	}

	/**
	 * обработать сообщение в чате
	 */
	protected boolean onChatMessage(Event event)
	{
		return isKnownObject(event.getInitiator());
	}

	@Override
	protected void onArrived()
	{
		super.onArrived();
		if (_ai != null)
		{
			_ai.onArrived(_moveResult);
		}
	}

	public void setAi(AI newAi)
	{
		if (_ai != null)
		{
			AI oldAi = _ai;
			_ai = null;
			oldAi.dispose();
		}

		_ai = newAi;

		if (_ai != null)
		{
			_ai.begin();
		}
	}
}
