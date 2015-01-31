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

import java.io.IOException;

public class Quat
{
    public float x, y, z, w;

    public Quat(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quat(MyInputStream in)
    {
        try
        {
            this.x = in.readFloat();
            this.y = in.readFloat();
            this.z = in.readFloat();
            this.w = in.readFloat();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Quat clone()
    {
        return new Quat(this.x, this.y, this.z, this.w);
    }

    public boolean equals(Quat q)
    {
        return ((Math.abs(this.x - q.x) <= Const.EPS) &&
                (Math.abs(this.y - q.y) <= Const.EPS) &&
                (Math.abs(this.z - q.z) <= Const.EPS) &&
                (Math.abs(this.w - q.w) <= Const.EPS));
    }

    public Quat sub(Quat q)
    {
        return new Quat(this.x - q.x, this.y - q.y, this.z - q.z, this.w - q.w);
    }

    public Quat add(Quat q)
    {
        return new Quat(this.x + q.x, this.y + q.y, this.z + q.z, this.w + q.w);
    }

    public Quat mul(Quat q)
    {
        return new Quat(this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y,
                        this.w * q.y + this.y * q.w + this.z * q.x - this.x * q.z,
                        this.w * q.z + this.z * q.w + this.x * q.y - this.y * q.x,
                        this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z);
    }

    public Quat mul(float t)
    {
        return new Quat(this.x * t, this.y * t, this.z * t, this.w * t);
    }

    public Vec3f mul(Vec3f v)
    {
        //with q * Quat(v.x, v.y, v.z, 0) * q.Invert do
        Quat q = mul(new Quat(v.x, v.y, v.z, 0)).mul(invert());
        return new Vec3f(q.x, q.y, q.z);
    }

    public Quat invert()
    {
        return new Quat(-x, -y, -z, w);
    }

    public Quat lerp(Quat q, float t)
    {
        if (dot(q) < 0)
            return sub(add(q).mul(t));
        else
            return add(sub(q).mul(t));
    }

    public float dot(Quat q)
    {
        return x * q.x + y * q.y + z * q.z + w * q.w;
    }

    public Quat normal()
    {
        float Len = (float) Math.sqrt(x * x + y * y + z * z + w * w);
        if (Len > 0)
        {
            Len = 1 / Len;
            return new Quat(x * Len, y * Len, z * Len, w * Len);
        }
        else
        {
            return new Quat(0, 0, 0, 0);
        }
    }

    public Vec3f euler()
    {
        Vec3f result = new Vec3f();
        float D = 2 * x * z + y * w;
        if (Math.abs(D) > 1 - Const.EPS)
        {
            result.x = 0;
            if (D > 0)
                result.y = (float) (-Math.PI * 0.5);
            else
                result.y = (float) (Math.PI * 0.5);
            result.z = (float) Math.atan2(-2 * (y * z - w * x), 2 * (w * w + y * y));
        }
        else
        {
            result.x = -(float) Math.atan2(2 * (y * z + w * x), 2 * (w * w + z * z) - 1);
            result.y = (float) Math.asin(-D);
            result.z = -(float) Math.atan2(2 * (x * y + w * z), 2 * (w * w + x * x) - 1);
        }
        return result;
    }

    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ", " + w + ")";
    }
}
