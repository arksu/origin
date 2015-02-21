package com.a2client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * история в чате
 * Created by arksu on 21.02.15.
 */
public class ChatHistory
{
    private static final Logger _log = LoggerFactory.getLogger(ChatHistory.class.getName());

    static List<String> list = new ArrayList<>();
    static int pos = 0;

    // сбросить позицию в хистори
    static public void Reset()
    {
        pos = list.size();
    }

    static public void add(String msg)
    {
        list.add(msg);
        pos = list.size();
    }

    static public void clear()
    {
        list.clear();
        pos = 0;
    }

    static public String next()
    {
        pos++;
        if (pos >= list.size())
        {
            pos = list.size() - 1;
            return "";
        }
        if (pos >= 0)
        {
            return list.get(pos);
        }
        return "";
    }

    static public String prev()
    {
        pos--;
        if (pos < 0)
        {
            pos = 0;
        }
        if (pos < list.size() && pos >= 0)
        {
            return list.get(pos);
        }
        return "";
    }

}
