package com.a4server.gameserver.model;

import com.a4server.gameserver.model.ai.Mind;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.position.ObjectPosition;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * объект описывающий поведение живых, активных объектов (игроки, животные)
 * Created by arksu on 02.02.15.
 */
public abstract class Human extends MoveObject
{
	private static final Logger _log = LoggerFactory.getLogger(Human.class.getName());

	/**
	 * мозг объекта который определяет его поведение, реагирует на все события
	 */
	protected Mind _mind = null;

	/**
	 * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
	 * любое добалвение в этот список, а равно как и удаление из него должно быть
	 * синхронизировано с клиентом
	 */
	protected FastList<GameObject> _knownKist = new FastList<GameObject>().shared();

	/**
	 * дистанция на которой мы видим объекты
	 * может изменяться динамически (ночью видим хуже)
	 */
	protected int _visibleDistance = 100;

	/**
	 * дистанция которую нужно пройти чтобы произошел апдейт видимых объектов
	 */
	protected static final int VISIBLE_UPDATE_DISTANCE = 5 * Grid.TILE_SIZE;

	/**
	 * последняя позиция в которой было обновление видимых объектов
	 * нужно чтобы часто не обновлять список видимых (слишком накладно)
	 */
	private ObjectPosition _lastVisibleUpdatePos = null;

	public Human(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	public Mind getMind()
	{
		return _mind;
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
	public void UpdateVisibleObjects(boolean force)
	{
		// только если отошли достаточно далеко от последней позиции апдейта
		if (force || (
				getPos() != null && _lastVisibleUpdatePos != null &&
				!getPos().equals(_lastVisibleUpdatePos) &&
				getPos().getDistance(_lastVisibleUpdatePos) > VISIBLE_UPDATE_DISTANCE
		))
		{
			_log.debug("UpdateVisibleObjects " + toString());
			// запомним те объекты которые видимы при текущем апдейте
			FastList<GameObject> newList = new FastList<>();

			// проходим по всем гридам в которых находимся
			for (Grid g : _grids)
			{
				// по всем объектам в гридах
				FastList<GameObject> objs = g.getObjects();
				for (GameObject o : objs)
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
			FastList<GameObject> del = new FastList<>();
			for (GameObject o : _knownKist)
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
		_knownKist.add(object);
	}

	protected void removeKnownObject(GameObject object)
	{
		_knownKist.remove(object);
	}

	/**
	 * знаю ли я об указанном объекте (он есть на клиенте)
	 * @param object объект
	 * @return истина если знаю
	 */
	public boolean isKnownObject(GameObject object)
	{
		return _knownKist.contains(object);
	}

	public GameObject isKnownObject(int objectId)
	{
		for (GameObject object : _knownKist)
		{
			if (object.getObjectId() == objectId)
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
	public boolean HandleEvent(Event event)
	{
		// событие движения
		switch (event.getType())
		{
			case MOVE:
			case STOP_MOVE:
				// знаю ли я этот объект?
				if (isKnownObject(event.getObject()))
				{
					// я больше не вижу объект
					if (!isObjectVisibleForMe(event.getObject()))
					{
						// удалим его из списка видимых
						removeKnownObject(event.getObject());
						// т.к. объект мы больше не знаем. пакет слать не будем
						return false;
					}
					// т.к. я его знаю отправим пакет клиенту
					return true;
				}
				else
				{
					// объекта я не знаю. проверим может я теперь вижу его?
					if (isObjectVisibleForMe(event.getObject()))
					{
						// ага. вижу. добавим в список видимых
						addKnownObject(event.getObject());
						// мы видим объект, отправим пакет клиенту
						return true;
					}
				}
				break;

			case CHAT_GENERAL_MESSAGE:
				return onChatMessage(event);

			case INTERACT:
				// если мы знаем такой объект - пошлем пакет клиенту
				return isKnownObject(event.getObject());
		}
		return false;
	}

	/**
	 * обработать сообщение в чате
	 */
	protected boolean onChatMessage(Event event)
	{
		return isKnownObject(event.getObject());
	}

	@Override
	public void onArrived()
	{
		super.onArrived();
		if (_mind != null)
		{
			_mind.onArrived(_moveResult);
		}
	}

	public void setMind(Mind mind)
	{
		if (_mind != null)
		{
			Mind old = _mind;
			_mind = null;
			old.free();
		}

		_mind = mind;

		if (_mind != null)
		{
			_mind.begin();
		}
	}
}
