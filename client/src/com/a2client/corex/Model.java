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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model extends Node
{
	public Skeleton skeleton;
	public List<Mesh> meshes = new ArrayList<Mesh>();
	public Map<String, Anim> anims = new HashMap<String, Anim>();

	public Model(String name)
	{
		super(name);
		matrix = new Mat4f();
		matrix.identity();
	}

	public void load_skeleton(String name)
	{
		skeleton = new Skeleton(name);
	}

//    public void load_anim(String fname) {
//        try {
//            Anim anim = new Anim(fname, skeleton);
//            anims.put(fname, anim);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

	public void play_anim(String name)
	{
		play_anim(name, 1, 1, Anim.LOOP_MODE.lmRepeat);
	}

	public void play_anim(String name, float blendWeight, float blendTime, Anim.LOOP_MODE loop_mode)
	{
		Anim a = anims.get(name);
		if (a != null)
		{
//            a.
			skeleton.addAnim(a);
			a.play(blendWeight, blendTime, loop_mode);
		}
	}

	public void render_mesh()
	{
		for (Mesh m : meshes)
		{
			m.Render();
		}
	}

	protected void Update()
	{
		skeleton.update();
	}

	protected void Render()
	{
		Render.ModelMatrix = matrix;
		render_mesh();
	}

	public void add_mesh(Mesh mesh)
	{
		mesh.setSkeleton(this.skeleton);
		meshes.add(mesh);
	}
}

