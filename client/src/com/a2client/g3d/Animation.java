package com.a2client.g3d;

import com.a2client.g3d.math.DualQuat;
import com.a2client.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * анимации скелета
 * Created by arksu on 19.03.17.
 */
public class Animation
{
	private static final Logger _log = LoggerFactory.getLogger(Animation.class.getName());

	/**
	 * кадры анимации и другое
	 */
	private final AnimationData _data;

	/**
	 * расчитанные позиции костей для текущего кадра анимации (в настоящий момент времени)
	 */
	public DualQuat[] joint;

	/**
	 * режим цикла анимации
	 */
	private LoopMode _loopMode = LoopMode.Repeat;

	/**
	 * кадр с которого начинаем играть анимацию
	 */
	public int _frameStart;

	/**
	 * предыщущий кадр
	 */
	public int _framePrev;

	/**
	 * следующий кадр
	 */
	public int _frameNext;

	/**
	 * сколько прошло между предыдущим кадром и следующим (интерполяция кадров)
	 */
	public float _frameDelta;

	/**
	 * время начала анимации (тик)
	 */
	private long _startTime;

	private float _weight = 0;

	private float _startWeight = 0;

	private float _blendWeight = 0;

	private float _blendTime = 0;

	/**
	 * анимация проигрывается в обратном направлении?
	 */
	private boolean _isReverse = false;

	/**
	 * был завершен хотябы 1 полный цикл?
	 */
	private boolean _isEnded = false;

	public enum LoopMode
	{
		None, Last, Repeat, PingPong
	}

	public Animation(AnimationData data)
	{
		_data = data;
		joint = new DualQuat[_data.getSkeleton().getJointsCount()];
	}

	public float getWeight()
	{
		return _weight;
	}

	/**
	 * @param c default joint if frame does not exist
	 * @param idx index joint
	 */
	public void lerpJoint(DualQuat c, int idx)
	{
//		if (state[idx] != Render.frame_flag)
//		{
		DualQuat prev = _data.getFrames()[_framePrev][idx];
		DualQuat next = _data.getFrames()[_frameNext][idx];

		if (prev == null && next == null)
		{
			joint[idx] = c;
		}
		else
		{
			if (prev == null) prev = c;
			if (next == null) next = c;

			joint[idx] = prev.lerp(next, _frameDelta);
		}
//			state[idx] = Render.frame_flag;
//		}
	}

	public void update()
	{
		float dt = (float) (System.currentTimeMillis() - _startTime);

		_frameDelta = Utils.frac(dt * _data.getFps() / 1000);
		_framePrev = (int) (System.currentTimeMillis() - _startTime) * _data.getFps() / 1000;

		if (_isReverse)
		{
			_framePrev = _frameStart - _framePrev;
		}
		else
		{
			_framePrev = _frameStart + _framePrev;
		}

		if ((!_isReverse && _framePrev >= _data.getFramesCount() - 1) || (_isReverse && _framePrev <= 0))
		{
			if (!_isEnded)
			{
				_isEnded = true;
				onEnd();
			}
		}

		switch (_loopMode)
		{
			case Repeat:
				if (_isReverse)
				{
					_frameNext = (_framePrev - 1) % _data.getFramesCount();
				}
				else
				{
					_frameNext = (_framePrev + 1) % _data.getFramesCount();
				}
				break;

			case None:
				if (_framePrev >= _data.getFramesCount() - 1)
				{
					_frameNext = 0;
					_frameDelta = 0;
					_framePrev = 0;
				}
				else
				{
					_frameNext = _framePrev + 1;
				}
				break;

			case Last:
				_framePrev = Utils.min(_framePrev, _data.getFramesCount() - 1);
				_frameNext = Utils.min(_framePrev + 1, _data.getFramesCount() - 1);
				break;

			case PingPong:
				if ((_framePrev / (_data.getFramesCount() - 1)) % 2 == 0)
				{
					// по нарастающей. от 0 до data.count
					_framePrev = _framePrev % (_data.getFramesCount() - 1);
					_frameNext = _framePrev + 1;
				}
				else
				{
					_framePrev = _data.getFramesCount() - (_framePrev % (_data.getFramesCount() - 1)) - 1;
					_frameNext = _framePrev - 1;
				}
				break;
		}

		_framePrev = _framePrev % _data.getFramesCount();

		_frameNext = Utils.max(_frameNext, 0);
		_framePrev = Utils.max(_framePrev, 0);

		if ((Math.abs(_weight - _blendWeight) > Const.EPS) && (_blendTime > Const.EPS))
		{
			_weight = Utils.lerp(_startWeight, _blendWeight, Utils.min(1f, (dt * 0.001f) / _blendTime));
		}
		else
		{
			_weight = _blendWeight;
		}
	}

	protected void onEnd()
	{
//		_log.debug("animation end: " + _data.getName());
	}

	public void play()
	{
		play(1f, 0.3f, LoopMode.Repeat);
	}

	public void play(float blendWeight, float blendTime, LoopMode loopMode)
	{
		_blendWeight = Utils.min(blendWeight, 1f);
		_startWeight = _weight;
		_blendTime = blendTime;
		_loopMode = loopMode;

		_startTime = System.currentTimeMillis();
		_frameStart = 0;
		_isEnded = false;
		_isReverse = false;
	}
}
