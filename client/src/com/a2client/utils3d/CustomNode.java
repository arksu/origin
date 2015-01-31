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

import com.a2client.corex.Mat4f;
import com.a2client.corex.Node;
import com.a2client.corex.Render;
import com.a2client.corex.Vec3f;
import com.a2client.xml.XML;

public class CustomNode extends Node
{
    /**
     * загруженный конфиг
     */
    protected XML xml;

    /**
     * параметры для камеры
     */
    public float camera_fov;
    public float camera_dist;
    public float camera_angle_x;
    public float camera_angle_y;

    /**
     * угол поворота модели вокруг своей оси
     */
    public float angle;
    /**
     * текущий угол поворота (сглаживание)
     */
    protected float current_angle;

    /**
     * ускорение повотора
     */
    public float RotateAcc = 0.005f;

    /**
     * ширина и высота. для учета в 2д окружении
     */
    public int width;
    public int height;

    /**
     * отступ
     */
    public int offx;
    public int offy;

    /**
     * порядок в Z сортировке
     */
    public int zorder;

    /**
     * текущая высота воды (передается в шейдер и на основе ее затемняется)
     */
    public float water_height = -1;
    /**
     * высота воды для тайлов
     */
    public float water_low;
    public float water_deep;


    /**
     * конструктор
     *
     * @param name
     */
    public CustomNode(String name)
    {
        super(name);
        xml = XML.load(name);
        LoadSettings();
    }

    /**
     * загрузить параметры из xml конфига
     */
    public void LoadSettings()
    {
        camera_fov = getFloatParam("camera", "fov", 45);
        camera_dist = getFloatParam("camera", "dist", 35);
        camera_angle_x = getFloatParam("camera", "angle_x", 0);
        camera_angle_y = getFloatParam("camera", "angle_y", 0.5f);

        width = getIntParam("layer", "w", 20);
        height = getIntParam("layer", "h", 40);
        offx = getIntParam("layer", "offx", 10);
        offy = getIntParam("layer", "offy", 40);
        zorder = getIntParam("layer", "z", 10);

        water_low = getFloatParam("water", "low", 1);
        water_deep = getFloatParam("water", "deep", 1.5f);

        RotateAcc = getFloatParam("rotate", "accel", 0.005f);
    }

    private float getFloatParam(String tag, String param, float def)
    {
        XML n = xml.getNode(tag);
        if (n == null)
            return def;
        String s = n.params.get(param);
        return s.isEmpty() ? def : Float.parseFloat(s);
    }

    private int getIntParam(String tag, String param, int def)
    {
        XML n = xml.getNode(tag);
        if (n == null)
            return def;
        String s = n.params.get(param);
        return s.isEmpty() ? def : Integer.parseInt(s);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        UpdateRotate();
    }

    protected void UpdateRotate()
    {
        float pi2 = (float) Math.PI / 2;
        float a = angle;

        if (current_angle < -pi2 && a > pi2)
        {
            a -= 2 * Math.PI;
        }

        if (current_angle > pi2 && a < -pi2)
        {
            a += 2 * Math.PI;
        }

        float dx = a - current_angle;
        if (Math.abs(dx) < 0.01f)
            current_angle = a;
        else
            current_angle += dx * (Render.dt * RotateAcc); // чем меньше - тем медленнее поворот

        while (current_angle > Math.PI)
            current_angle -= 2 * Math.PI;
        while (current_angle < -Math.PI)
            current_angle += 2 * Math.PI;
    }

    public void setCurrent_angle()
    {
        current_angle = angle;
    }

    protected void SetAngle()
    {
        setMatrix(new Mat4f().identity().rotate(current_angle, new Vec3f(0f, 1f, 0f)));
    }
}
