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

import com.a2client.Log;
import com.a2client.corex.Const;
import com.a2client.corex.Material;
import com.a2client.corex.Mesh;
import com.a2client.corex.Skeleton;
import com.a2client.xml.XML;
import com.a2client.xml.XMLIterator;

import java.util.ArrayList;
import java.util.List;

public class Equip
{
    private Skeleton skeleton;
    private List<Mesh> meshes = new ArrayList<Mesh>();
    private Character parent;
    private XML xml;
    public String name;
    public String binded = "";

    public Equip(String name, final Character character, XML xml)
    {
        this.parent = character;
        this.xml = xml;
        this.name = name;

        skeleton = new Skeleton(character.name + Const.PATH_DELIM + xml.getNode("skeleton").params.get("file"));

        this.xml.ProcessNodes("mesh", new XMLIterator()
        {
            public void ProcessNode(XML x)
            {
                Mesh mesh = new Mesh(character.name + Const.PATH_DELIM + x.params.get("file"));
                String mat = x.params.get("material");
                if (mat.equals("none") || mat.isEmpty())
                    mesh.setMaterial(Material.load(mat));
                else
                    mesh.setMaterial(Material.load(character.name + Const.PATH_DELIM + mat));
                mesh.setSkeleton(skeleton);
                meshes.add(mesh);
            }
        });
    }

    public void Bind(String bone)
    {
        this.binded = bone;
        Log.debug("bind equip: " + name + ", " + bone);
        skeleton.setParent(parent.getSkeleton(), parent.getSkeleton().data.getIdx(bone));
        skeleton.ResetState();
    }

    public void Unbind()
    {
        binded = "";
    }

    public void Render()
    {
        if (binded.isEmpty())
            return;

        for (Mesh m : meshes)
        {
            m.Render();
        }
    }
}
