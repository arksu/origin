package com.a2client.modelviewer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;

/**
 * Created by arksu on 15.03.17.
 */
public interface ModelSorter
{
	void sort(Camera camera, Array<Model> renderables);
}
