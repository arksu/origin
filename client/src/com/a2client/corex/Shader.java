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
import org.lwjgl.opengl.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Shader extends ResObject
{
	public int ID;
	public List<ShaderUniform> uniform = new ArrayList<ShaderUniform>();
	public List<ShaderAttrib> attrib = new ArrayList<ShaderAttrib>();
	private static final char CRLF = '\n';
	private List<Integer> shaders = new ArrayList<Integer>();

	public static Shader load(String name, List<String> defines)
	{
		String defstr = "";
		for (String s : defines)
		{
			defstr += s;
		}

		String rname = name + Const.EXT_SHADER + "*" + defstr;
		ResObject r = ResManager.Get(rname);
		if (r != null && r instanceof Shader)
		{
			return (Shader) r;
		}

		Shader a = new Shader(name, defines);
		ResManager.Add(a);
		return a;
	}

	public Shader(String name, List<String> defines)
	{
		String defstr = "";
		for (String s : defines)
		{
			defstr += s;
		}
		this.name = name + Const.EXT_SHADER + "*" + defstr;
		MyInputStream in = FileSys.getStream(name + Const.EXT_SHADER);
		Log.debug("load shader: " + name);
		Log.debug("defines: " + defines.toString());
		String DEFINE = "#define";

		String define_str = DEFINE;
		for (String s : defines)
		{
			define_str += " " + s + CRLF + DEFINE;
		}

		try
		{
			shaders.clear();
			String source = readStreamAsString(in);
			// vertex
			String csource = define_str + " VERTEX" + CRLF + source;
			attach(ARBVertexShader.GL_VERTEX_SHADER_ARB, csource);
			// fragment
			csource = define_str + " FRAGMENT" + CRLF + source;
			attach(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB, csource);
			// geometry
			if (source.contains("#ifdef GEOMETRY"))
			{
				csource = define_str + " GEOMETRY" + CRLF + source;
				attach(ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB, csource);
				// may be need fix
				ARBGetProgramBinary.glProgramParameteri(ID, ARBGeometryShader4.GL_GEOMETRY_VERTICES_OUT_ARB, 3);
				ARBGetProgramBinary.glProgramParameteri(ID, ARBGeometryShader4.GL_GEOMETRY_INPUT_TYPE_ARB, GL11.GL_TRIANGLES);
				ARBGetProgramBinary.glProgramParameteri(ID, ARBGeometryShader4.GL_GEOMETRY_VERTICES_OUT_ARB, GL11.GL_TRIANGLE_STRIP);
			}

			ID = ARBShaderObjects.glCreateProgramObjectARB();
			Log.debug("shader id: " + ID);
			for (int obj : shaders)
			{
				ARBShaderObjects.glAttachObjectARB(ID, obj);
			}

			ARBShaderObjects.glLinkProgramARB(ID);
			if (ARBShaderObjects.glGetObjectParameteriARB(ID, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE)
			{
				Log.error(getLogInfo(ID));
				ID = 0;
				return;
			}

			ARBShaderObjects.glValidateProgramARB(ID);
			if (ARBShaderObjects.glGetObjectParameteriARB(ID, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE)
			{
				Log.error(getLogInfo(ID));
				ID = 0;
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void attach(int shader_type, String src)
	{
		int obj = ARBShaderObjects.glCreateShaderObjectARB(shader_type);
		ARBShaderObjects.glShaderSourceARB(obj, src);
		ARBShaderObjects.glCompileShaderARB(obj);

		if (ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
		{
			throw new RuntimeException("Error creating shader: " + getLogInfo(obj));
		}

		shaders.add(obj);
	}

	public ShaderAttrib getAttrib(Const.SHADER_ATTRIB_TYPE attrib_type, String aname, boolean norm)
	{
		for (ShaderAttrib a : attrib)
		{
			if (a.Name.equals(aname))
			{
				return a;
			}
		}
		ShaderAttrib sa = new ShaderAttrib();
		sa.init(ID, aname, attrib_type, norm);
		attrib.add(sa);
		return sa;
	}

	public ShaderUniform getUniform(Const.SHADER_UNIFORM_TYPE uniform_type, String uname)
	{
		for (ShaderUniform u : uniform)
		{
			if (u.Name.equals(uname))
			{
				return u;
			}
		}
		ShaderUniform su = new ShaderUniform();
		su.init(ID, uname, uniform_type);
		uniform.add(su);
		return su;
	}

	public void bind()
	{
		if (ResManager.Active.get(Const.RES_TYPE.rtShader) != this)
		{
			ARBShaderObjects.glUseProgramObjectARB(ID);
			ResManager.Active.put(Const.RES_TYPE.rtShader, this);
		}
	}

	public void finit()
	{
		ARBShaderObjects.glDeleteObjectARB(ID);
	}

	private String readStreamAsString(InputStream in) throws Exception
	{
		StringBuilder source = new StringBuilder();

		BufferedReader reader;

		reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

		String line;
		while ((line = reader.readLine()) != null)
		{
			source.append(line).append(CRLF);
		}

		reader.close();
		in.close();

		return source.toString();
	}

	private static String getLogInfo(int obj)
	{
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}
}
