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

package com.a2client.utils3d;


import com.a2client.corex.*;
import com.badlogic.gdx.Gdx;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

public class SceneCombi
{
    public Node node;
    public Camera camera;
    public boolean show_skeleton = false;
    Material skeleton_material;
    public float scale = 1f;
    public boolean is_2d_switched = false;

    public SceneCombi()
    {
        camera = new Camera();
        camera.Init(Camera.CAMERA_MODE.cmTarget, true);
        camera.pos = new Vec3f(0, 0, 0);
        camera.dist = 25;
        camera.FOV = 45;
        camera.ZNear = 0.1f;
        camera.ZFar = 100;
        camera.angle.y = 0;
        camera.angle.x = 0.5f;
    }

    /**
     * установить ноду для текущего рендера
     *
     * @param node нода
     */
    public void SetCurrentNode(Node node)
    {
        this.node = node;
        if (node instanceof CustomNode)
        {
            CustomNode c = (CustomNode) node;
            camera.FOV = c.camera_fov;
            camera.dist = c.camera_dist;
            camera.angle.x = c.camera_angle_x;
            camera.angle.y = c.camera_angle_y;
            Render.fog.y = c.water_height;
        }
    }

    /**
     * загрузить материал для отображения скелета (дебаг)
     *
     * @param fname имя файла материала
     */
    public void LoadSkeletonMaterial(String fname)
    {
        skeleton_material = Material.load(fname);
    }

    /**
     * установить отступ для отрисовки модели
     *
     * @param dx y
     * @param dy x
     */
    public void Set2DOffset(int dx, int dy)
    {
        camera.offset_2d.x = dx;
        camera.offset_2d.y = dy;
    }

    /**
     * переключится в 2д режим
     */
    public void Switch2D()
    {
        Switch2D(false, true);
    }

    public void Switch2D(boolean forced, boolean need_rescale)
    {
        if (forced || !is_2d_switched)
        {
            Render.set2D(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            GL11.glLoadIdentity();
            if (need_rescale)
                GL11.glScalef(scale, scale, 1f);
            // сбросим ноду
            node = null;
            is_2d_switched = true;
        }
    }

    /**
     * отрисовать текущую ноду которая была установлена
     */
    public void Render()
    {
        if (node == null)
            return;
        is_2d_switched = false;
        camera.UpdateMatrix();
        camera.UpdatePlanes();

        Render.setDepthTest(true);
        camera.Setup();

        GL11.glScalef(scale, scale, scale);

        Render.Mode = Const.RENDER_MODE.rmOpaque;
        node.onRender();

        //        Render.Mode = Const.RENDER_MODE.rmOpacity;
        //        node.Render();

        if (show_skeleton)
            RenderSkeleton();

        Render.lights[0].shadow_map = null;
    }


    // for debug
    public void RenderSkeleton()
    {
        if (node == null)
            return;

        GL11.glClear(GL_DEPTH_BUFFER_BIT);
        Render.Mode = Const.RENDER_MODE.rmOpaque;
        ARBShaderObjects.glUseProgramObjectARB(0);
        skeleton_material.bind();

        node.onRenderSkeleton();
    }

    /**
     * загружаем необходимые ресурсы для отображения игроков
     */
    public void LoadResources()
    {

    }
}
