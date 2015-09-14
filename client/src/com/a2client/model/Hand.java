package com.a2client.model;

import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Image;
import com.a2client.util.Vec2i;

/**
 * Created by arksu on 13.09.15.
 */
public class Hand
{
//	public static final String UNKNOWN_ICON = "icon_unknown";
	public static final String UNKNOWN_ICON = "hotbar_bg";

	/**
	 * контрол картинка который отображает то что держим в руке
	 */
	private GUI_Image _image;

	private final int _objectId;
	private final int _w;
	private final int _h;
	private final int _offsetX;
	private final int _offsetY;
	private final String _icon;

	public Hand(int objectId, int w, int h, int offsetX, int offsetY, String icon)
	{
		_objectId = objectId;
		_w = w;
		_h = h;
		_offsetX = offsetX;
		_offsetY = offsetY;
		_icon = icon;
	}

	public void makeControl()
	{
		if (_image != null) _image.Unlink();
		_image = new GUI_Image(GUI.rootPopup());
		_image.drag = true;
		_image.drag_offset = new Vec2i(_offsetX, _offsetY);
		_image.skin_element = "icon_" + _icon;

		if (!_image.getSkin().hasElement(_image.skin_element))
		{
			_image.skin_element = UNKNOWN_ICON;
		}

		_image.SetSize(_image.getSkin().getElementSize(_image.skin_element));
	}

	public void dispose()
	{
		if (_image != null)
		{
			_image.Unlink();
		}
	}
}
