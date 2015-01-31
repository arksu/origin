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

package com.a2client.xml;

import com.a2client.corex.*;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XML extends ResObject
{
    private String content;
    private String tag;
    private int data_len;
    public XMLParams params;
    private int count;
    private List<XML> Node = new ArrayList<XML>();

    public void ProcessNodes(String node_tag, XMLIterator it)
    {
        for (Iterator<XML> i = getIterator(); i.hasNext(); )
        {
            XML x = i.next();
            if (x.getTag().equals(node_tag))
            {
                it.ProcessNode(x);
            }
        }
    }

    public static XML load_file(String fname)
    {
        try
        {
            byte[] buffer = new byte[(int) FileSys.getSize(fname)];
            MyInputStream in = FileSys.getStream(fname);
            BufferedInputStream f = new BufferedInputStream(in);
            f.read(buffer);
            XML xml = new XML(new String(buffer, "UTF-8"), 0);
            xml.name = fname;
            return xml;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static XML load(String name)
    {
        ResObject r = ResManager.Get(name + Const.EXT_XML);
        if (r != null && r instanceof XML)
        {
            return (XML) r;
        }

        XML a = load_file(name + Const.EXT_XML);
        ResManager.Add(a);
        return a;
    }

    public Iterator<XML> getIterator()
    {
        return Node.iterator();
    }

    public XML getNode(String tag_name)
    {
        for (XML xml : Node)
        {
            if (xml.tag.equals(tag_name))
                return xml;
        }
        return null;
    }

    public XML getNodeI(int idx)
    {
        return Node.get(idx);
    }

    public int getCount()
    {
        return count;
    }

    public String getContent()
    {
        return content;
    }

    public String getTag()
    {
        return tag;
    }

    private enum FLAG
    {
        F_BEGIN,
        F_COMMENT,
        F_COMMENT2,
        F_TAG,
        F_PARAMS,
        F_CONTENT,
        F_END
    }

    private enum FLAG_COMMENT
    {
        FC_NONE,
        FC_MINUS1,
        FC_MINUS2,
        FC_QUESTION
    }

    public XML(String text, int begin_pos)
    {
        boolean TextFlag = false;
        FLAG flag = FLAG.F_BEGIN;
        FLAG_COMMENT flag_comment = FLAG_COMMENT.FC_NONE;
        int minus_pos = -1;
        int BeginIndex = begin_pos;
        content = "";
        int len = text.length();
        int i = begin_pos - 1;
        char cc;
        loop:
        while (i < len - 1)
        {
            i++;
            cc = text.charAt(i);
            switch (flag)
            {
                case F_BEGIN:
                    switch (cc)
                    {
                        case '<':
                            flag = FLAG.F_TAG;
                            BeginIndex = i + 1;
                            break;

                        case '>':
                            flag = FLAG.F_END;
                            i--;
                            break;
                    }
                    break;

                case F_COMMENT:
                    switch (flag_comment)
                    {
                        case FC_NONE:
                            if (cc == '-')
                            {
                                flag_comment = FLAG_COMMENT.FC_MINUS1;
                                minus_pos = i;
                            }
                            break;
                        case FC_MINUS1:
                            if (cc == '-')
                            {
                                if (i == minus_pos + 1)
                                {
                                    flag_comment = FLAG_COMMENT.FC_MINUS2;
                                    minus_pos = i;
                                }
                                else
                                {
                                    flag_comment = FLAG_COMMENT.FC_NONE;
                                }
                            }
                            break;
                        case FC_MINUS2:
                            if (cc == '>')
                            {
                                if (i == minus_pos + 1)
                                {
                                    flag = FLAG.F_BEGIN;
                                }
                                else
                                {
                                    flag_comment = FLAG_COMMENT.FC_NONE;
                                }
                            }
                            break;
                    }
                    break;

                case F_COMMENT2:
                    switch (flag_comment)
                    {
                        case FC_NONE:
                            if (cc == '?')
                            {
                                flag_comment = FLAG_COMMENT.FC_QUESTION;
                                minus_pos = i;
                            }
                            break;
                        case FC_QUESTION:
                            if (cc == '>')
                            {
                                if (i == minus_pos + 1)
                                {
                                    flag = FLAG.F_BEGIN;
                                }
                                else
                                {
                                    flag_comment = FLAG_COMMENT.FC_NONE;
                                }
                            }
                            break;
                    }
                    break;

                case F_TAG:
                    switch (cc)
                    {
                        case '>':
                            flag = FLAG.F_CONTENT;
                            break;
                        case '/':
                            flag = FLAG.F_END;
                            break;
                        case ' ':
                            flag = FLAG.F_PARAMS;
                            break;
                        case '?':
                            flag = FLAG.F_COMMENT2;
                            flag_comment = FLAG_COMMENT.FC_NONE;
                            continue;
                        case '!':
                            flag = FLAG.F_COMMENT;
                            flag_comment = FLAG_COMMENT.FC_NONE;
                            continue;
                        default:
                            continue;
                    }
                    tag = trim(text.substring(BeginIndex, i));
                    BeginIndex = i + 1;
                    break;

                case F_PARAMS:
                    if (cc == '"')
                        TextFlag = !TextFlag;
                    if (!TextFlag)
                    {
                        switch (cc)
                        {
                            case '>':
                                flag = FLAG.F_CONTENT;
                                break;
                            case '/':
                                flag = FLAG.F_END;
                                break;
                            default:
                                continue;
                        }
                        params = new XMLParams(trim(text.substring(BeginIndex, i)));
                        BeginIndex = i + 1;
                    }
                    break;

                case F_CONTENT:
                    switch (cc)
                    {
                        case '<':
                            content = trim(text.substring(BeginIndex, i));
                            for (int j = i + 1; j < text.length(); j++)
                            {
                                if (text.charAt(j) == '>')
                                {
                                    if (!trim(text.substring(i + 1, j)).equals("/" + tag))
                                    {
                                        XML n = new XML(text, i);
                                        Node.add(n);
                                        if (n.data_len == 0)
                                            break;
                                        i = i + n.data_len - 1;
                                        BeginIndex = i + 1;
                                    }
                                    else
                                    {
                                        i = j - 1;
                                        flag = FLAG.F_END;
                                    }
                                    break;
                                }
                            }
                            break;
                    }
                    break;

                case F_END:
                    if (cc == '>')
                    {
                        data_len = i - begin_pos + 1;
                        break loop;
                    }
                    break;
            }
        }
        if (params == null)
            params = new XMLParams("");
        count = Node.size();
    }

    public static String trim(String s)
    {
        int j = s.length() - 1;
        int i = 0;

        while (i <= j)
        {
            char c = s.charAt(i);
            if (c < ' ' || c == '"')
                i++;
            else
                break;
        }
        if (i <= j)
        {
            while (true)
            {
                char c = s.charAt(j);
                if (c < ' ' || c == '"')
                    j--;
                else
                    break;
            }
            return s.substring(i, j + 1);
        }
        else
            return "";
    }

    @Override
    public String toString()
    {
        return "(" + tag + " <" + params.getCount() + "> : " + count + ")";
    }
}
