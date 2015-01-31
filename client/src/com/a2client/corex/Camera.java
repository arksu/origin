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

import com.a2client.util.Vec2i;
import org.lwjgl.opengl.GL11;

public class Camera
{
    public enum CAMERA_MODE
    {
        cmFree,
        cmTarget,
        cmLookAt
    }

    /**
     * режим камеры
     */
    public CAMERA_MODE mode = CAMERA_MODE.cmFree;

    /**
     * проеция перспектива? если ложь - ортогональная
     */
    public boolean is_projection_persp = true;

    /**
     * позиция
     */
    public Vec3f pos = new Vec3f();

    /**
     * цель куда смотрит камера
     */
    public Vec3f target = new Vec3f();

    /**
     * угол
     */
    public Vec3f angle = new Vec3f();

    /**
     * дистанция
     */
    public float dist = 0;

    /**
     * отступ центра вьюпорта от верхнего левого угла экрана в пикселах
     * (вывод модели в указанное место на экране)
     */
    public Vec2i offset_2d = new Vec2i(-1, -1);

    public float FOV = 0;

    public float ZNear = 0;

    public float ZFar = 0;

    /**
     * ограничение обзора
     */
    public Vec4f[] planes = new Vec4f[6];

    public Mat4f ViewMatrix = new Mat4f();

    public Mat4f ProjMatrix = new Mat4f();

    public Mat4f ViewProjMatrix = new Mat4f();

    public void Init()
    {
        Init(CAMERA_MODE.cmFree, true);
    }

    public void Init(CAMERA_MODE mode, boolean is_persp)
    {
        this.mode = mode;
        this.is_projection_persp = is_persp;
        FOV = 45;
        ZNear = 0.1f;
        ZFar = 100f;
        dist = 0;

        for (int i = 0; i < planes.length; i++)
        {
            planes[i] = new Vec4f();
        }
    }

    public void UpdateMatrix()
    {
        ViewMatrix.identity();
        switch (mode)
        {
            case cmFree:
                ViewMatrix = ViewMatrix.rotate(angle.z, new Vec3f(0, 0, 1));
                ViewMatrix = ViewMatrix.rotate(angle.x, new Vec3f(1, 0, 0));
                ViewMatrix = ViewMatrix.rotate(angle.y, new Vec3f(0, 1, 0));
                ViewMatrix = ViewMatrix.translate(pos.mul(-1));
                break;
            case cmTarget:
                ViewMatrix.setPos(new Vec3f(0, 0, -dist));
                ViewMatrix = ViewMatrix.rotate(angle.z, new Vec3f(0, 0, 1));
                ViewMatrix = ViewMatrix.rotate(angle.x, new Vec3f(1, 0, 0));
                ViewMatrix = ViewMatrix.rotate(angle.y, new Vec3f(0, 1, 0));
                ViewMatrix = ViewMatrix.translate(pos.mul(-1));
                break;
            case cmLookAt:
                ViewMatrix.lookat(pos, target, new Vec3f(0, 1, 0));
        }

        Rect vp = Render.getViewport();
        if (is_projection_persp)
        {
            ProjMatrix.perspective2(FOV, (float) vp.left, (float) vp.right, (float) vp.top, (float) vp.bottom, ZNear,
                                    ZFar, offset_2d.x, offset_2d.y);
        }
        else
        {
            if (offset_2d.x < 0)
            {
                //                ProjMatrix.ortho(-1, 1, -1, 1, ZNear, ZFar); // original
                ProjMatrix.ortho(-1 * dist, 1 * dist, -1 * dist, 1 * dist, ZNear, ZFar);
            }
            else
            {
                float dx = -2 * ((float) (offset_2d.x) / (vp.right - vp.left));
                float dy = -2 * (1 - ((float) offset_2d.y) / (vp.bottom - vp.top));
                ProjMatrix.ortho(dx * dist, (2 + dx) * dist, dy * dist, (2 + dy) * dist, ZNear, ZFar);
            }
        }

        ViewProjMatrix = ProjMatrix.mul(ViewMatrix);
    }

    public void UpdatePlanes()
    {
        Mat4f m = ViewProjMatrix;
        Plane(planes[0], m.e30 - m.e00, m.e31 - m.e01, m.e32 - m.e02, m.e33 - m.e03); // right
        Plane(planes[1], m.e30 + m.e00, m.e31 + m.e01, m.e32 + m.e02, m.e33 + m.e03); // left
        Plane(planes[2], m.e30 - m.e10, m.e31 - m.e11, m.e32 - m.e12, m.e33 - m.e13); // top
        Plane(planes[3], m.e30 + m.e10, m.e31 + m.e11, m.e32 + m.e12, m.e33 + m.e13); // bottom
        Plane(planes[4], m.e30 - m.e20, m.e31 - m.e21, m.e32 - m.e22, m.e33 - m.e23); // near
        Plane(planes[5], m.e30 + m.e20, m.e31 + m.e21, m.e32 + m.e22, m.e33 + m.e23); // far
    }

    private void Plane(Vec4f p, float x, float y, float z, float w)
    {
        float len = (float) (1 / Math.sqrt(x * x + y * y + z * z));
        p.x = x * len;
        p.y = y * len;
        p.z = z * len;
        p.w = w * len;
    }

    public void Setup()
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadMatrix(ProjMatrix.getBuf());

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(ViewMatrix.getBuf());

        Mat4f m = ViewMatrix.inverse();
        Render.view_pos = m.getPos();
    }

    /**
     * видим ли объект в камере?
     *
     * @param b размеры объекта
     * @return
     */
    public boolean Visible(Box b)
    {
        for (Vec4f p : planes)
        {
            if (p.dot(b.max) < 0 &&
                    p.dot(new Vec3f(b.min.x, b.max.y, b.max.z)) < 0 &&
                    p.dot(new Vec3f(b.max.x, b.min.y, b.max.z)) < 0 &&
                    p.dot(new Vec3f(b.min.x, b.min.y, b.max.z)) < 0 &&
                    p.dot(new Vec3f(b.max.x, b.max.y, b.min.z)) < 0 &&
                    p.dot(new Vec3f(b.min.x, b.max.y, b.min.z)) < 0 &&
                    p.dot(new Vec3f(b.max.x, b.min.y, b.min.z)) < 0 &&
                    p.dot(b.min) < 0)
                return false;
        }

        return true;
    }

    public boolean Visible(TSphere s)
    {
        for (Vec4f p : planes)
        {
            if (p.dot(s.center) < -s.radius)
                return false;
        }
        return true;
    }


}
