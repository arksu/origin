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

import java.util.ArrayList;
import java.util.List;

public class MeshData extends ResObject
{
    public Box BBox;
    public List<Const.MATERIAL_ATTRIB> Attrib = new ArrayList<Const.MATERIAL_ATTRIB>();
    public List<String> JName = new ArrayList<String>();
    public Const.MESH_MODE Mode;
    public MeshBuffer IndexBuf;
    public MeshBuffer VertexBuf;

    static public MeshData load(String name)
    {
        ResObject r = ResManager.Get(name + Const.EXT_MESH);
        if (r != null && r instanceof MeshData)
        {
            return (MeshData) r;
        }

        MeshData a = new MeshData(name + Const.EXT_MESH);
        ResManager.Add(a);
        return a;
    }

    public MeshData(String name)
    {
        this.name = name;
        MyInputStream in = FileSys.getStream(name);
        BBox = new Box(in);
        try
        {
            // mode
            byte bmode = in.readByte();
            switch (bmode)
            {
                case 0:
                    Mode = Const.MESH_MODE.mmTriList;
                    break;
                case 1:
                    Mode = Const.MESH_MODE.mmTriStrip;
                    break;
                case 2:
                    Mode = Const.MESH_MODE.mmLine;
                    break;
                default:
                    throw new Exception("wrong mesh mode! " + bmode);
            }

            // attribs
            Attrib.clear();
            Const.MATERIAL_ATTRIB[] attrs = Const.MATERIAL_ATTRIB.values();
            int atr_count = in.readInt();
            if (atr_count != attrs.length)
            {
                throw new Exception("wrong attrs len!" + atr_count);
            }
            int i;
            for (i = 0; i < atr_count; i++)
            {
                if (in.readByte() == 1)
                {
                    Attrib.add(attrs[i]);
                }
            }

            // buffers
            IndexBuf = new MeshBuffer();
            IndexBuf.load(Const.BUFFER_TYPE.btIndex, in);

            VertexBuf = new MeshBuffer();
            VertexBuf.load(Const.BUFFER_TYPE.btVertex, in);

            // joint names
            int jcount = in.readInt();
            JName.clear();
            for (i = 0; i < jcount; i++)
            {
                JName.add(in.readAnsiString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
