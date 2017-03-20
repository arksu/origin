package com.a2client.modelviewer.g3d;

import com.a2client.modelviewer.g3d.math.DualQuat;

/**
 * исходные данные анимации (кадры)
 * Created by arksu on 19.03.17.
 */
public class AnimationData
{
	/**
	 * трансформации костей
	 */
	private final DualQuat[][] _frames;

	/**
	 * сколько всего кадров в анимации
	 */
	private final int _framesCount;

	/**
	 * frame per second
	 */
	private final int _fps;

	private final String _name;

	/**
	 * скелет к которому загружена анимация
	 */
	private final SkeletonData _skeleton;

	public AnimationData(DualQuat[][] frames, int framesCount, int fps, String name, SkeletonData skeleton)
	{
		_name = name;
		_frames = frames;
		_framesCount = framesCount;
		_fps = fps;
		_skeleton = skeleton;
	}

	public DualQuat[][] getFrames()
	{
		return _frames;
	}

	public int getFramesCount()
	{
		return _framesCount;
	}

	public int getFps()
	{
		return _fps;
	}

	public SkeletonData getSkeleton()
	{
		return _skeleton;
	}

	public String getName()
	{
		return _name;
	}
}
