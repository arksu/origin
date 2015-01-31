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

import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

public class ShaderAttrib
{
    public int ID;
    public Const.SHADER_ATTRIB_TYPE Type;
    public int DType;
    public boolean Norm;
    public int Size;
    public String Name;

    public void init(int ShaderID, String name, Const.SHADER_ATTRIB_TYPE attrib_type, boolean norm)
    {
        //        ARBShaderObjects.glUseProgramObjectARB(ShaderID);
        this.Name = name;
        this.ID = ARBVertexShader.glGetAttribLocationARB(ShaderID, name);
        this.Type = attrib_type;
        int type_ord = attrib_type.ordinal();
        this.Size = type_ord % 4 + 1;
        this.Norm = norm;
        switch (this.Type)
        {
            case atVec1b:
                this.DType = GL11.GL_UNSIGNED_BYTE;
                break;
            case atVec2b:
                this.DType = GL11.GL_UNSIGNED_BYTE;
                break;
            case atVec3b:
                this.DType = GL11.GL_UNSIGNED_BYTE;
                break;
            case atVec4b:
                this.DType = GL11.GL_UNSIGNED_BYTE;
                break;

            case atVec1s:
                this.DType = GL11.GL_SHORT;
                break;
            case atVec2s:
                this.DType = GL11.GL_SHORT;
                break;
            case atVec3s:
                this.DType = GL11.GL_SHORT;
                break;
            case atVec4s:
                this.DType = GL11.GL_SHORT;

            case atVec1f:
                this.DType = GL11.GL_FLOAT;
                break;
            case atVec2f:
                this.DType = GL11.GL_FLOAT;
                break;
            case atVec3f:
                this.DType = GL11.GL_FLOAT;
                break;
            case atVec4f:
                this.DType = GL11.GL_FLOAT;
                break;
        }
    }

    /**
     * устанавливаем атрибуты вершинам, передаем данные о вершинах
     */
    public void setValue(int stride, long buffer, int offset)
    {
        if (ID != -1)
        {
            ARBVertexShader.glVertexAttribPointerARB(ID, Size, DType, Norm, stride, buffer + offset);
            //            System.out.println("set value <"+Name+"> size="+Size+" stride="+stride+ " offset="+offset+" norm="+(Norm?"true":"false") );
        }
    }

    public void enable()
    {
        if (ID != -1)
            ARBVertexShader.glEnableVertexAttribArrayARB(ID);
    }

    public void disable()
    {
        if (ID != -1)
            ARBVertexShader.glDisableVertexAttribArrayARB(ID);
    }
}
