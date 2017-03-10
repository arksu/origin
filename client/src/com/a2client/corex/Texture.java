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

import org.lwjgl.opengl.GL11;

public class Texture extends ResObject
{
    //    public org.newdawn.slick.opengl.Texture texture;
    public int sampler = GL11.GL_TEXTURE_2D;

	public static Texture load(String name)
	{
		// имя идет уже с расширением файла (.png)
		ResObject r = ResManager.Get(name);
		if (r != null && r instanceof Texture)
		{
			return (Texture) r;
		}

		Texture a = new Texture(name);
		ResManager.Add(a);
		return a;
	}

    public Texture(String fname)
    {
        //        try
        //        {
        //            InputStream in = new FileInputStream(Const.DIR_MEDIA + File.separator + fname);
        //            Log.debug("load texture: " + fname);
        //            texture = TextureLoader.getTexture("PNG", in, GL11.GL_LINEAR);
        //        }
        //        catch (Exception e)s
        //        {
        //            e.printStackTrace();
        //        }
    }

    public void bind(int idx)
    {
        //        if (Render.multi_texture)
        //        {
        //            ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB + idx);
        //        }
        //        GL11.glBindTexture(sampler, texture.getTextureID());
    }
}
