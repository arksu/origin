/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2013 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.a2client.corex;

public class Scene
{
    Texture shadow_texture;
    RenderTarget shadow_rt;
    public Node node;
    public boolean shadows = false;

    public void Init()
    {
        node = new Node("root");

        // TODO : подготовка к отрисовке теней: создание текстуры и render target
    }

    public void onRender()
    {
        Camera main = Render.camera;
        main.UpdateMatrix();
        main.UpdatePlanes();

        if (shadows)
        {
            // TODO : отрисовка теней
        }

        Render.setDepthTest(true);
        Render.camera = main;
        Render.camera.Setup();

        Render.Mode = Const.RENDER_MODE.rmOpaque;
        node.onRender();

        Render.Mode = Const.RENDER_MODE.rmOpacity;
        node.onRender();

        Render.lights[0].shadow_map = null;
    }

    public void onUpdate()
    {
        node.onUpdate();
    }

    // for debug
    public void onRenderSkeleton()
    {
        node.onRenderSkeleton();
    }

    public void Add(Node node)
    {
        this.node.Add(node);
    }
}
