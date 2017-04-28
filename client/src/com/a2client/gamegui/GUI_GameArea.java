package com.a2client.gamegui;

import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Control;
import com.a2client.network.game.clientpackets.MouseClick;
import com.a2client.screens.Game;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Hotkey.*;

/**
 * игровая область. в которую выводим 3д рендер
 * Created by arksu on 20.08.16.
 */
public class GUI_GameArea extends GUI_Control
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_GameArea.class.getName());

	private final Game _game;

	private boolean _cameraDrag = false;

	public GUI_GameArea(GUI_Control parent, Game game)
	{
		super(parent);
		_game = game;
	}

	@Override
	public boolean onMouseBtn(int btn, boolean down)
	{
		boolean cameraEndDrag = false;
		if (isMouseInMe())
		{
			if (btn == BUTTON_CAMERA)
			{
				// если кнопка нажата
				if (down)
				{
					_cameraDrag = true;
					_game.getCamera().startDrag(new Vec2i(Gdx.input.getX(), Gdx.input.getY()));
				}
				// кнопку отжали
				else
				{
					// закончим вращение перетаскивание камеры
					cameraEndDrag = _game.getCamera().getStartDrag() != null
					                && !_game.getCamera().getStartDrag().equals(new Vec2i(Gdx.input.getX(), Gdx.input.getY()));
					_game.getCamera().startDrag(null);
				}
			}

			GUI.getInstance()._focusedControl = null;
			if (_game.getWorldMousePos() != null)
			{
				int action = -1;
				if (btn == BUTTON_ACTION_PRIMARY && (!cameraEndDrag || BUTTON_ACTION_PRIMARY != BUTTON_CAMERA))
				{
					action = 0;
				}
				if (btn == BUTTON_ACTION_SECONDARY && (!cameraEndDrag || BUTTON_ACTION_SECONDARY != BUTTON_CAMERA))
				{
					action = 1;
				}
				new MouseClick(
						down,
						action,
						_game.getWorldMousePos().x,
						_game.getWorldMousePos().y,
						(_game.getRender().getSelected() != null ? _game.getRender().getSelected().getObjectId() : 0)
				).send();
			}

			return true;
		}
		return false;
	}

	@Override
	public void update()
	{
		super.update();

		if (_cameraDrag)
		{
			_game.getCamera().updateDrag(new Vec2i(Gdx.input.getX(), Gdx.input.getY()));
		}
	}
}
