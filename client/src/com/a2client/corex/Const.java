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

public class Const
{
    public static final double EPS = 1.E-05;
    public static final float NAN = 0.0f / 0.0f;
    public static final float INF = Float.POSITIVE_INFINITY;
    public static final double deg2rad = Math.PI / 180;
    public static final int MAX_LIGHTS = 3;
    public static final int MAX_JOINTS = 64;

    public static final String EXT_SKELETON = ".ask";
    public static final String EXT_MESH = ".ams";
    public static final String EXT_MATERIAL = ".amt";
    public static final String EXT_ANIMATION = ".aan";
    public static final String EXT_SHADER = ".ash";
    public static final String EXT_XML = ".xml";
    public static final String DIR_MEDIA = "media";
    public static final String PATH_DELIM = "/";

    public enum SHADER_ATTRIB_TYPE
    {
        atVec1b,
        atVec2b,
        atVec3b,
        atVec4b,
        atVec1s,
        atVec2s,
        atVec3s,
        atVec4s,
        atVec1f,
        atVec2f,
        atVec3f,
        atVec4f
    }

    public enum SHADER_UNIFORM_TYPE
    {
        utInt,
        utVec1,
        utVec2,
        utVec3,
        utVec4,
        utMat3,
        utMat4
    }

    public enum MATERIAL_SAMPLER
    {
        msDiffuse,
        msNormal,
        msSpecular,
        msAmbient,
        msEmission,
        msAlphaMask,
        msReflect,
        msShadow,
        msMask
        //msMap0, msMap1, msMap2, msMap3
    }

    public static final int msDiffuse_idx = 0;
    public static final int msNormal_idx = 1;
    public static final int msSpecular_idx = 2;
    public static final int msAmbient_idx = 3;
    public static final int msEmission_idx = 4;
    public static final int msAlphaMask_idx = 5;
    public static final int msReflect_idx = 6;
    public static final int msShadow_idx = 7;
    public static final int msMask_idx = 8;

    public static class SamplerIDObject
    {
        public int ID;
        public String Name;

        public SamplerIDObject(int id, String name)
        {
            this.ID = id;
            this.Name = name;
        }
    }

    public static SamplerIDObject[] SamplerID = new SamplerIDObject[MATERIAL_SAMPLER.values().length];

    static
    {
        SamplerID[0] = new SamplerIDObject(0, "sDiffuse");
        SamplerID[1] = new SamplerIDObject(1, "sNormal");
        SamplerID[2] = new SamplerIDObject(2, "sSpecular");
        SamplerID[3] = new SamplerIDObject(3, "sAmbient");
        SamplerID[4] = new SamplerIDObject(4, "sEmission");
        SamplerID[5] = new SamplerIDObject(5, "sAlphaMask");
        SamplerID[6] = new SamplerIDObject(6, "sReflect");
        SamplerID[7] = new SamplerIDObject(7, "sShadow");
        SamplerID[8] = new SamplerIDObject(8, "sMask");
        //            put(MATERIAL_SAMPLER.msMap0, new SamplerIDObject(9, "sMap0"));
        //            put(MATERIAL_SAMPLER.msMap1, new SamplerIDObject(10, "sMap1"));
        //            put(MATERIAL_SAMPLER.msMap2, new SamplerIDObject(11, "sMap2"));
        //            put(MATERIAL_SAMPLER.msMap3, new SamplerIDObject(12, "sMap3"));
    }

    public enum MATERIAL_ATTRIB
    {
        maCoord,
        maBinormal,
        maNormal,
        maTexCoord0,
        maTexCoord1,
        maColor,
        maJoint
    }

    public static final int maCoord_idx = 0;
    public static final int maBinormal_idx = 1;
    public static final int maNormal_idx = 2;
    public static final int maTexCoord0_idx = 3;
    public static final int maTexCoord1_idx = 4;
    public static final int maColor_idx = 5;
    public static final int maJoint_idx = 6;

    public static class AttribIDObject
    {
        public SHADER_ATTRIB_TYPE Type;
        public boolean Norm;
        public String Name;

        public AttribIDObject(SHADER_ATTRIB_TYPE type, boolean norm, String name)
        {
            this.Name = name;
            this.Norm = norm;
            this.Type = type;
        }
    }

    /**
     * атрибуты для шейдера и их описание
     */
    public static AttribIDObject[] AttribID = new AttribIDObject[MATERIAL_ATTRIB.values().length];

    static
    {
        AttribID[0] = new AttribIDObject(SHADER_ATTRIB_TYPE.atVec3f, false, "aCoord");
        AttribID[1] = new AttribIDObject(SHADER_ATTRIB_TYPE.atVec4b, true, "aBinormal");
        AttribID[2] = new AttribIDObject(SHADER_ATTRIB_TYPE.atVec4b, true, "aNormal");
        AttribID[3] = new AttribIDObject(SHADER_ATTRIB_TYPE.atVec2f, false, "aTexCoord0");
        AttribID[4] = new AttribIDObject(SHADER_ATTRIB_TYPE.atVec2f, false, "aTexCoord1");
        AttribID[5] = new AttribIDObject(SHADER_ATTRIB_TYPE.atVec4b, true, "aColor");
        AttribID[6] = new AttribIDObject(SHADER_ATTRIB_TYPE.atVec4b, false, "aJoint");
    }

    public static int getAttribSize(MATERIAL_ATTRIB a)
    {
        switch (a)
        {
            case maCoord:
                return 12;
            case maBinormal:
                return 4;
            case maNormal:
                return 4;
            case maTexCoord0:
                return 8;
            case maTexCoord1:
                return 8;
            case maColor:
                return 4;
            case maJoint:
                return 4;
            default:
                return 0;
        }
    }

    public enum MATERIAL_UNIFORM
    {
        muModelMatrix,
        muLightMatrix,
        muViewPos,
        muJoint,
        muLightPos,
        muLightParam,
        muAmbient,
        muMaterial,
        muTexOffset,
        muFog
    }

    public static final int muModelMatrix_idx = 0;
    public static final int muLightMatrix_idx = 1;
    public static final int muViewPos_idx = 2;
    public static final int muJoint_idx = 3;
    public static final int muLightPos_idx = 4;
    public static final int muLightParam_idx = 5;
    public static final int muAmbient_idx = 6;
    public static final int muMaterial_idx = 7;
    public static final int muTexOffset_idx = 8;
    public static final int muFog_idx = 9;

    public static class UniformIDObject
    {
        public SHADER_UNIFORM_TYPE UType;
        public String Name;

        public UniformIDObject(SHADER_UNIFORM_TYPE utype, String name)
        {
            this.UType = utype;
            this.Name = name;
        }
    }

    public static UniformIDObject[] UniformID = new UniformIDObject[MATERIAL_UNIFORM.values().length];

    static
    {
        UniformID[0] = new UniformIDObject(SHADER_UNIFORM_TYPE.utMat4, "uModelMatrix");
        UniformID[1] = new UniformIDObject(SHADER_UNIFORM_TYPE.utMat4, "uLightMatrix");
        UniformID[2] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec3, "uViewPos");
        UniformID[3] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec4, "uJoint");
        UniformID[4] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec3, "uLightPos");
        UniformID[5] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec4, "uLightParam");
        UniformID[6] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec3, "uAmbient");
        UniformID[7] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec4, "uMaterial");
        UniformID[8] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec2, "uTexOffset");
        UniformID[9] = new UniformIDObject(SHADER_UNIFORM_TYPE.utVec3, "uFog");
    }

    public enum MESH_MODE
    {
        mmTriList,
        mmTriStrip,
        mmLine
    }

    public static int getMeshMode(MESH_MODE m)
    {
        switch (m)
        {
            case mmTriList:
                return GL11.GL_TRIANGLES;
            case mmLine:
                return GL11.GL_LINES;
            case mmTriStrip:
                return GL11.GL_TRIANGLE_STRIP;
            default:
                return GL11.GL_TRIANGLES;
        }
    }

    public enum BUFFER_TYPE
    {
        btIndex,
        btVertex
    }

    public enum RES_TYPE
    {
        rtTexture,
        rtTexture1,
        rtTexture2,
        rtTexture3,
        rtTexture4,
        rtTexture5,
        rtTexture6,
        rtTexture7,
        rtTexture8,
        rtTexture9,
        rtTexture10,
        rtTexture11,
        rtTexture12,
        rtTexture13,
        rtTexture14,
        rtTexture15,
        rtShader,
        rtMeshIdex,
        rtMeshVertex,
        rtSound
    }

    public enum RENDER_MODE
    {
        rmOpaque,
        rmOpacity,
        rmShadow
    }

    public enum RENDER_TARGET_TYPE
    {
        rtColor,
        rtDepth
    }

    public enum RENDER_CHANNEL
    {
        rcDepth,
        rcColor0,
        rcColor1,
        rcColor2,
        rcColor3,
        rcColor4,
        rcColor5,
        rcColor6,
        rcColor7
    }

    public enum TEXTURE_TARGET
    {
        ttTex2D,
        ttTexCubeXp,
        ttTexCubeXn,
        ttTexCubeYp,
        ttTexCubeYn,
        ttTexCubeZp,
        ttTexCubeZn
    }

    public enum BLEND_TYPE
    {
        btNone,
        btNormal,
        btAdd,
        btMult
    }

    public enum CULL_FACE
    {
        cfNone,
        cfFront,
        cfBack
    }

}
