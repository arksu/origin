package com.a4server.gameserver.model.objects;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * шаблон объекта по которому создаются конечные экземпляры
 * Created by arksu on 15.02.15.
 */
public class ObjectTemplate
{
    private static final Logger _log = LoggerFactory.getLogger(ObjectTemplate.class.getName());

    private final String _name;
    private int _typeId;
    private int _width = 10;
    private int _height = 10;
    private Collision _collision = null;

    public ObjectTemplate(String name)
    {
        _name = name;
    }

    public int getTypeId()
    {
        return _typeId;
    }

    public int getWidth()
    {
        return _width;
    }

    public int getHeight()
    {
        return _height;
    }

    public String getName()
    {
        return _name;
    }

    public Collision getCollision()
    {
        return _collision;
    }

    public void read(JsonReader in) throws IOException
    {
        while (in.hasNext())
        {
            JsonToken tkn = in.peek();
            switch (tkn)
            {
                case NAME:
                    readParam(in);
                    break;
                case END_OBJECT:
                    return;
                default:
                    _log.warn(getClass().getSimpleName() + ": wrong token " + tkn);
                    return;
            }
        }
    }

    protected void readParam(JsonReader in) throws IOException
    {
        String paramName = in.nextName();
        if ("typeid".equalsIgnoreCase(paramName))
        {
            _typeId = in.nextInt();
        }
        else if ("size".equalsIgnoreCase(paramName))
        {
            int sz = in.nextInt();
            _width = sz;
            _height = sz;
        }
        else if ("collision".equalsIgnoreCase(paramName))
        {
            Gson gson = new Gson();
            _collision = gson.fromJson(in, Collision.class);
        }
    }

    public class Collision
    {
        @SerializedName ("all")
        private boolean _allYes = true;

        @SerializedName ("exclude")
        private List<String> _exclude = new ArrayList<>();
    }
}