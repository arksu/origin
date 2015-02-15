package com.a4server.gameserver.model.objects;

import com.a4server.gameserver.model.GameObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * фарбрика объектов, создание объекта по его тип ид
 * Created by arksu on 15.02.15.
 */
public class ObjectsFactory
{
    private static final Logger _log = LoggerFactory.getLogger(ObjectsFactory.class.getName());

    private static final String CONFIG_NAME = "/objects.json";

    Map<String, ObjectTemplate> _templates = new HashMap<>();

    public ObjectsFactory()
    {
        LoadInternalConfig();
    }

    public void LoadInternalConfig()
    {
        InputStream ins = this.getClass().getResourceAsStream(CONFIG_NAME);
        try
        {
            JsonReader in = new JsonReader(new InputStreamReader(ins, "UTF-8"));
            in.beginObject();
            while (in.hasNext())
            {
                JsonToken tkn = in.peek();
                switch (tkn)
                {
                    case NAME:
                        String name = in.nextName();
                        _log.debug("object: " + name);
                        _templates.put(name, readObjectTemplate(name, in));
                        break;
                    default:
                        _log.warn("LoadInternalConfig: wrong token " + tkn.name());
                        break;
                }
            }
            in.endObject();
            in.close();
        }
        catch (UnsupportedEncodingException e)
        {
            _log.warn("LoadInternalConfig: unsupported encoding");
        }
        catch (IOException e)
        {
            _log.warn("LoadInternalConfig: io error");
        }
        catch (Exception e) {
            _log.error("LoadInternalConfig unexcepted error: "+e.getMessage());
            e.printStackTrace();
        }
    }

    protected ObjectTemplate readObjectTemplate(String name, JsonReader in) throws IOException
    {
        ObjectTemplate template = new ObjectTemplate(name);
        in.beginObject();
        template.read(in);
        in.endObject();
        return template;
    }

    public GameObject makeObject(int typeId)
    {
        return null;
    }

    public static ObjectsFactory getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final ObjectsFactory _instance = new ObjectsFactory();
    }
}
