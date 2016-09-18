package com.a2client.render.skybox;

import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Label;
import com.a2client.gui.GUI_Scrollbar;
import com.a2client.gui.GUI_Window;
import com.a2client.render.Render;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * окно настроек параметров шейдера
 * Created by arksu on 18.09.16.
 */
public class TuningWindow
{
	private static final Logger _log = LoggerFactory.getLogger(TuningWindow.class.getName());

	GUI_Window _window;
	GUI_Scrollbar _scrollbarSun;
	GUI_Scrollbar _scrollbarRadius;
	GUI_Scrollbar _scrollbarCameraHeight;
	GUI_Scrollbar _scrollbarOutRadius;

	GUI_Label _label1;
	GUI_Label _label2;
	GUI_Label _label3;
	GUI_Label _label4;

	public TuningWindow()
	{
		_window = new GUI_Window(GUI.rootNormal());
		_window.setSize(600, 200);
		_window.setPos(10, 350);

		// -----------------------------
		_label1 = new GUI_Label(_window);
		_label1.setPos(10, 29);

		_scrollbarSun = new GUI_Scrollbar(_window);
		_scrollbarSun.SetVertical(false);
		_scrollbarSun.setSize(580, 20);
		_scrollbarSun.setPos(_label1.pos.add(0, 15));
		_scrollbarSun.setMax(200);
		_scrollbarSun.setMin(0);
		_scrollbarSun.setValue(100);
		_scrollbarSun.SetPageSize(10);

		// -----------------------------
		_label2 = new GUI_Label(_window);
		_label2.setPos(_scrollbarSun.pos.add(0, 20));

		_scrollbarRadius = new GUI_Scrollbar(_window);
		_scrollbarRadius.SetVertical(false);
		_scrollbarRadius.setSize(580, 20);
		_scrollbarRadius.setPos(_label2.pos.add(0, 15));
		_scrollbarRadius.setMax(500);
		_scrollbarRadius.setMin(50);
		_scrollbarRadius.setValue(100);
		_scrollbarRadius.SetPageSize(20);

		// -----------------------------
		_label3 = new GUI_Label(_window);
		_label3.setPos(_scrollbarRadius.pos.add(0, 20));

		_scrollbarCameraHeight = new GUI_Scrollbar(_window);
		_scrollbarCameraHeight.SetVertical(false);
		_scrollbarCameraHeight.setSize(580, 20);
		_scrollbarCameraHeight.setPos(_label3.pos.add(0, 15));
		_scrollbarCameraHeight.setMax(200);
		_scrollbarCameraHeight.setMin(0);
		_scrollbarCameraHeight.setValue(100);
		_scrollbarCameraHeight.SetPageSize(20);

		// -----------------------------
		_label4 = new GUI_Label(_window);
		_label4.setPos(_scrollbarCameraHeight.pos.add(0, 20));

		_scrollbarOutRadius = new GUI_Scrollbar(_window);
		_scrollbarOutRadius.SetVertical(false);
		_scrollbarOutRadius.setSize(580, 20);
		_scrollbarOutRadius.setPos(_label4.pos.add(0, 15));
		_scrollbarOutRadius.setMax(200);
		_scrollbarOutRadius.setMin(0);
		_scrollbarOutRadius.setValue(100);
		_scrollbarOutRadius.SetPageSize(20);

	}

	public float getSunAngle()
	{
		return (-((float) _scrollbarSun.getValue()) / ((float) _scrollbarSun.getMax())) * 100f;
	}

	public float getRadius()
	{
		return ((float) _scrollbarRadius.getValue());
	}

	public float getCameraHeigjt()
	{
		return ((float) _scrollbarCameraHeight.getValue()) / 100f + 0.5f;
	}

	public float getOutRadius()
	{
		return ((float) _scrollbarOutRadius.getValue()) / 100f + 1f;
	}

	public void update()
	{
		Render.sunAngle = getSunAngle();

		_label1.caption = "sun angle " + getSunAngle();
		_label2.caption = "radius " + getRadius();
		_label3.caption = "camera height" + getCameraHeigjt();
		_label4.caption = "out raius " + getOutRadius();
	}
}
