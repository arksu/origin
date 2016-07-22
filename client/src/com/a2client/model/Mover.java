package com.a2client.model;

import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * класс который сглаживает передвижения объекта путем линейной интерполяции
 * Created by arksu on 12.02.15.
 */
public class Mover
{
	private static final Logger _log = LoggerFactory.getLogger(Mover.class.getName());

	/**
	 * объект который двигаем
	 */
	private final GameObject _object;

	/**
	 * текущая позиция объекта
	 */
	private Vector2 _current;

	/**
	 * куда должны передвинутся, конечная позиция
	 */
	private Vector2 _end;

	/**
	 * начальная позиция
	 */
	private Vector2 _start;

	/**
	 * время начала движения
	 */
	private long _startTime;

	/**
	 * скорость движения в единицах / сек
	 */
	private double _speed;

	/**
	 * длина
	 */
	private double _len;

	/**
	 * достигли конечной точки? передвижение закончено?
	 */
	public boolean _arrived = false;

	static final int SMOTH_RADIUS = 20;

	public Mover(GameObject object, int cx, int cy, int vx, int vy, int speed)
	{
		_object = object;
		_start = new Vector2(cx, cy);
		_end = new Vector2(vx, vy);
		_startTime = System.currentTimeMillis();
		_speed = speed;
		_len = 0;
		_current = _start;
		_arrived = _start.dst(_end) < 0.5f;
		if (_arrived)
		{
			_log.debug("Mover already arrived");
		}
	}

	public void newMove(int cx, int cy, int vx, int vy, int speed)
	{
		Vector2 new_end = new Vector2(vx, vy);
		Vector2 new_start = new Vector2(cx, cy);
		_startTime = System.currentTimeMillis();
		// определяем где мы. и где точка начала нового движения, проверяем радиус
		if (_current.dst(new_start) > SMOTH_RADIUS)
		{
			_start = new Vector2(cx, cy);
			_end = new Vector2(vx, vy);
			_speed = speed;
			_len = 0;
			_current = _start;
		}
		else
		{
			_start = _current;
			_end = new_end;
			_len = 0;
			//            _current = _start;
			if (new_start.dst(new_end) < 0.5f)
			{
				_speed = speed;
			}
			else
			{
				_speed = _start.dst(_end) / new_start.dst(new_end) * (double) speed;
			}
		}
		_arrived = _start.dst(_end) < 0.5f;
		if (_arrived)
		{
			_log.debug("newMove already arrived");
		}
	}

	public void update()
	{
		if (_arrived)
		{
			return;
		}
		long dt = System.currentTimeMillis() - _startTime;
		_len = (((double) dt / 1000) * _speed);
		double d = _start.dst(_end);
		if (_len > _start.dst(_end) || _current.dst(_end) < 0.5f)
		{
			_current = _end;
			_arrived = true;
			_log.debug("arrived");
		}
		else
		{
			Vector2 dv = new Vector2(_end).sub(_start);
			_current = new Vector2((float) ((dv.x / d) * _len), (float) ((dv.y / d) * _len));
			_current.add(_start);
		}

//        _log.info("dt= " + dt + " cur=" + _current.toString() + " cur_len=" + _len + " speed=" + _speed);
		_object.setCoord(_current);
		_object.updateCoordAndBB();
	}
}
