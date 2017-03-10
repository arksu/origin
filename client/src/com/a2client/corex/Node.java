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

public class Node
{
	protected Node parent;
	protected Box rbbox;
	protected Box bbox;
	protected Mat4f matrix;
	protected Mat4f rmatrix;
	public String name;
	public List<Node> nodes = new ArrayList<Node>();

	public Node(String name)
	{
		rbbox = Box.inf_box;
		bbox = new Box(new Vec3f(), new Vec3f());
		this.name = name;
	}

	public void setParent(Node p)
	{
		if (parent != null)
		{
			if (parent.nodes.contains(this))
			{
				parent.nodes.remove(this);
				parent.UpdateBounds();
			}
		}

		if (p != null)
		{
			p.nodes.add(this);
		}

		parent = p;
	}

	public void setRMatrix(Mat4f m)
	{
		rmatrix = m;
		if (parent != null)
		{
			setMatrix(parent.matrix.mul(rmatrix));
			parent.UpdateBounds();
		}
		else
		{
			setMatrix(rmatrix);
		}
	}

	public void setMatrix(Mat4f m)
	{
		matrix = m;
		for (Node n : nodes)
		{
			n.setMatrix(matrix.mul(n.rmatrix));
		}

		Vec3f[] v = new Vec3f[8];
		v[0] = matrix.mul(new Vec3f(rbbox.min.x, rbbox.max.y, rbbox.max.z));
		v[1] = matrix.mul(new Vec3f(rbbox.max.x, rbbox.min.y, rbbox.max.z));
		v[2] = matrix.mul(new Vec3f(rbbox.min.x, rbbox.min.y, rbbox.max.z));
		v[3] = matrix.mul(new Vec3f(rbbox.max.x, rbbox.max.y, rbbox.min.z));
		v[4] = matrix.mul(new Vec3f(rbbox.min.x, rbbox.max.y, rbbox.min.z));
		v[5] = matrix.mul(new Vec3f(rbbox.max.x, rbbox.min.y, rbbox.min.z));
		v[6] = matrix.mul(rbbox.min);
		v[7] = matrix.mul(rbbox.max);

		bbox.min = v[0].clone();
		bbox.max = v[0].clone();
		for (int i = 1; i < v.length; i++)
		{
			bbox.min = v[i].min(bbox.min);
			bbox.max = v[i].max(bbox.max);
		}

		if (parent != null)
		{
			parent.UpdateBounds();
		}
	}

	public Mat4f getMatrix()
	{
		return matrix;
	}

	public void UpdateBounds()
	{
		Box b = bbox.clone();
		for (Node n : nodes)
		{
			b.min = b.min.min(n.bbox.min);
			b.max = b.max.max(n.bbox.max);
		}

		if (b.min.equals(bbox.min) && b.max.equals(bbox.max))
		{
			return;
		}

		bbox = b;
		if (parent != null)
		{
			parent.UpdateBounds();
		}
	}

	public Node Add(Node n)
	{
		n.setParent(this);
		return n;
	}

	public boolean Remove(Node n)
	{
		n.parent = null;
		return true;
	}

	public int Count()
	{
		return nodes.size();
	}

	protected void Render()
	{

	}

	public void onRender()
	{
		Render();
		for (Node n : nodes)
		{
			n.onRender();
		}
	}

	protected void Update()
	{

	}

	public void onUpdate()
	{
		Update();
		for (Node n : nodes)
		{
			n.onUpdate();
		}
	}

	public void onRenderSkeleton()
	{
		RenderSkeleton();
		for (Node n : nodes)
		{
			n.onRenderSkeleton();
		}
	}

	protected void RenderSkeleton()
	{

	}
}
