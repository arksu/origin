package com.a2client.render.skybox;

import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Scrollbar;
import com.a2client.gui.GUI_Window;
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
	GUI_Scrollbar _scrollbar;

	public TuningWindow()
	{
		_window = new GUI_Window(GUI.rootNormal());
		_window.setSize(300, 200);
		_window.setPos(10,150);


		_scrollbar = new GUI_Scrollbar(_window);
		_scrollbar.SetVertical(false);
		_scrollbar.setSize(200,20);
		_scrollbar.setPos(15, 100);
	}
}
