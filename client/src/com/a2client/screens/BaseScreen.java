package com.a2client.screens;

import com.a2client.Input;
import com.a2client.gui.GUI;
import com.a2client.gui.GUIGDX;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;

public class BaseScreen implements Screen
{

    /**
     * цвет фона
     */
    protected Color _bgcolor = new Color(0.2f, 0.2f, 0.2f, 1f);

    public void onUpdate()
    {

    }

    public void onRender()
    {

    }

    public void onRender3D()
    {

    }

    @Override
    public void render(float delta)
    {
        onUpdate();

        Gdx.gl.glClearColor(_bgcolor.r, _bgcolor.g, _bgcolor.b, _bgcolor.a);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        onRender3D();
        GUIGDX.getSpriteBatch().begin();
        onRender();
        GUI.getInstance().Render();

        GUIGDX.FillRect(new Vec2i(Input.MouseX, Input.MouseY), new Vec2i(10, 10), Color.RED);

        GUIGDX.getSpriteBatch().end();
    }

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void show()
    {

    }

    @Override
    public void hide()
    {

    }

    @Override
    public void pause()
    {

    }

    @Override
    public void resume()
    {

    }

    @Override
    public void dispose()
    {

    }
}
