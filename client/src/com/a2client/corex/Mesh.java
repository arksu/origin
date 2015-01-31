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
import org.lwjgl.opengl.GL11;

public class Mesh
{
    public MeshData data;
    private Skeleton skeleton;
    private Material material;
    public DualQuat[] JQuat;
    public int[] JIndex;


    public Mesh(String name)
    {
        data = MeshData.load(name);

        JIndex = new int[data.JName.size()];
        JQuat = new DualQuat[data.JName.size()];
    }

    public void setSkeleton(Skeleton s)
    {
        if (skeleton != s)
        {
            skeleton = s;
            if (skeleton != null)
            {
                for (int i = 0; i < JIndex.length; i++)
                {
                    JIndex[i] = skeleton.JointIndex(data.JName.get(i));
                }
            }
        }
    }

    public void setMaterial(Material m)
    {
        this.material = m;
    }

    public Material getMaterial()
    {
        return material;
    }

    public void Render()
    {
        Material mat = material.ModeMat.get(Render.Mode);
        if (mat == null)
            return;
        mat.bind();

        // collect skin joints
        if (data.Attrib.contains(Const.MATERIAL_ATTRIB.maJoint) && skeleton != null)
        {
            for (int i = 0; i < JIndex.length; i++)
            {
                if (JIndex[i] > -1)
                {
                    skeleton.updateJoint(JIndex[i]);

                    if (skeleton.joint[JIndex[i]] == null)
                    {
                        Log.debug("fail1");
                        return;
                    }
                    else
                        JQuat[i] = skeleton.joint[JIndex[i]].mul(skeleton.data.base[JIndex[i]].bind);
                }
            }
            mat.uniform[Const.muJoint_idx].setValue(JQuat);
        }

        // set vertex attributes
        data.VertexBuf.bind();
        int offset = 0;
        for (Const.MATERIAL_ATTRIB ma : data.Attrib)
        {
            ShaderAttrib sa = mat.Attrib.get(ma);
            // enable attrib
            sa.enable();
            // set attrib value : data.VertexBuf.stride, data.VertexBuf.data, offset
            sa.setValue(data.VertexBuf.Stride, 0, offset);

            offset += Const.getAttribSize(ma);
        }

        if (data.IndexBuf != null)
        {
            data.IndexBuf.bind();

            //            if (test_shader.use_draw_elements)
            GL11.glDrawElements(Const.getMeshMode(data.Mode), data.IndexBuf.Count, data.IndexBuf.IndexType, 0);
            //            else
            //                GL12.glDrawRangeElements(Const.getMeshMode(data.Mode), 0, data.IndexBuf.Count, (data.IndexBuf.Count), data.IndexBuf.IndexType, 0);

        }

        // нужно для нормальной работы буферов (вывод тайлов)
        for (Const.MATERIAL_ATTRIB ma : data.Attrib)
        {
            mat.Attrib.get(ma).disable();
        }

    }

}
