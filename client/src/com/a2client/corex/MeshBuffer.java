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

import com.a2client.Log;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MeshBuffer
{
    public int Count;
    public int Stride;
    public Const.RES_TYPE RType;
    public int DType;
    public int ID;
    public ByteBuffer Data;
    public long DataPtr;
    public int IndexType = 0;

    public void load(Const.BUFFER_TYPE buffer_type, MyInputStream in)
    {
        try
        {
            Count = in.readInt();
            Stride = in.readInt();
            int total = Count * Stride;
            byte[] bytes = new byte[total];
            int readed = in.read(bytes);
            if (readed != total)
            {
                Log.error("MeshBuffer load wrong bytes, total=" + total + " readed=" + readed);
                return;
            }
            Data = ByteBuffer.allocateDirect(total);
            Data.put(bytes);
            Data.flip();
            DataPtr = MemoryUtil.getAddress(Data);

            if (buffer_type == Const.BUFFER_TYPE.btIndex)
            {
                DType = ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB;
                RType = Const.RES_TYPE.rtMeshIdex;

                switch (Stride)
                {
                    case 1:
                        IndexType = GL11.GL_UNSIGNED_BYTE;
                        break;
                    case 2:
                        IndexType = GL11.GL_UNSIGNED_SHORT;
                        break;
                    case 3:
                        IndexType = GL11.GL_FALSE;
                        break;
                    case 4:
                        IndexType = GL11.GL_UNSIGNED_INT;
                        break;
                    default:
                        IndexType = GL11.GL_FALSE;
                }
            }
            else
            {
                DType = ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB;
                RType = Const.RES_TYPE.rtMeshVertex;
            }

            // только если есть поддержка VBO
            ID = ARBVertexBufferObject.glGenBuffersARB();
            ARBVertexBufferObject.glBindBufferARB(DType, ID);
            ARBVertexBufferObject.glBufferDataARB(DType, Data, ARBBufferObject.GL_STATIC_DRAW_ARB);
            ResManager.Active.put(RType, this);
            //            Data = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void finit()
    {
        ARBVertexBufferObject.glDeleteBuffersARB(ID);
    }

    public void bind()
    {
        if (ResManager.Active.get(RType) != this)
        {
            ARBVertexBufferObject.glBindBufferARB(DType, ID);
            ResManager.Active.put(RType, this);
        }
    }
}
