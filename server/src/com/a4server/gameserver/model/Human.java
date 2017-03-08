package com.a4server.gameserver.model;

import com.a4server.Config;
import com.a4server.gameserver.model.ai.AI;
import com.a4server.gameserver.model.event.GridEvent;
import com.a4server.gameserver.model.knownlist.ObjectKnownList;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.position.ObjectPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

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
	protected ObjectKnownList _knownKist;

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
		initKnownList();
	}

	protected void initKnownList()
	{
		_knownKist = new ObjectKnownList(this);
	}

	public ObjectKnownList getKnownKist()
	{
		return _knownKist;
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
						getKnownKist().addKnownObject(o);
						newList.add(o);
					}
				}
			}
			// какие объекты больше не видимы?
			List<GameObject> del = new LinkedList<>();
			for (GameObject o : _knownKist.getKnownObjects().values())
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
				getKnownKist().removeKnownObject(o);
			}
			// запомним последнее место где произвели апдейт
			if (getPos() != null)
			{
				_lastVisibleUpdatePos = getPos().clone();
			}
		}
	}

	/**
	 * добавили объект в грид в котором находится игрок
	 */
	public void onGridObjectAdded(GameObject object)
	{
		// тут проверим видим ли мы этот объект
		if (isObjectVisibleForMe(object))
		{
			getKnownKist().addKnownObject(object);
		}
	}

	/**
	 * грид говорит что какой то объект был удален
	 */
	public void onGridObjectRemoved(GameObject object)
	{
		getKnownKist().removeKnownObject(object);
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
	public boolean handleEvent(GridEvent event)
	{
		// событие движения
		switch (event.getType())
		{
			case EVT_START_MOVE:
			case EVT_MOVE:
			case EVT_STOP_MOVE:
				// знаю ли я этот объект?
				if (getKnownKist().isKnownObject(event.getInitiator()))
				{
					// я больше не вижу объект
					if (!isObjectVisibleForMe(event.getInitiator()))
					{
						// удалим его из списка видимых
						getKnownKist().removeKnownObject(event.getInitiator());
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
						getKnownKist().addKnownObject(event.getInitiator());
						// мы видим объект, отправим пакет клиенту
						return true;
					}
				}
				break;

			case EVT_DEFAULT:
				return event.getInitiator() == null || getKnownKist().isKnownObject(event.getInitiator());

			case EVT_CHAT_GENERAL_MESSAGE:
				return onChatMessage(event);

			case EVT_INTERACT:
				// если мы знаем такой объект - пошлем пакет клиенту
				return getKnownKist().isKnownObject(event.getInitiator());
		}
		return false;
	}

	/**
	 * обработать сообщение в чате
	 */
	protected boolean onChatMessage(GridEvent gridEvent)
	{
		return getKnownKist().isKnownObject(gridEvent.getInitiator());
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
