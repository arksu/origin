package com.a4server.gameserver.model.objects;

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
            in.beginObject();
            _collision = new Collision(in);
            in.endObject();
        }
    }

    public class Collision {
        private boolean _allYes = true;
        private List<String> _exclude = new ArrayList<>();

        public Collision(JsonReader in) throws IOException {
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
            if ("all".equalsIgnoreCase(paramName))
            {
                _allYes = in.nextString().equalsIgnoreCase("yes");
            } else if ("exclude".equalsIgnoreCase(paramName)) {
                in.beginArray();
                while (in.hasNext()) {
                    JsonToken tkn = in.peek();
                    switch (tkn)
                    {
                        case STRING:
                            _exclude.add(in.nextString());
                    }
                }
                in.endArray();
            }
        }
    }
}