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

package com.a2client.util;

import com.a2client.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class INIFile
{
    HashMap<String, String> map = new HashMap<String, String>();
    private static String SECTION_DELIM = ">>";

    public INIFile()
    {
        map.clear();
    }

    public INIFile(String fname) throws IOException
    {
        FileInputStream fs = new FileInputStream(fname);
        try
        {
            loadFile(fs);
        }
        finally
        {
            fs.close();
        }
    }

    public INIFile(InputStream in) throws IOException
    {
        try
        {
            loadFile(in);
        }
        catch (Exception e)
        {
            Log.info("Error read lang data!");
        }
    }

    private void loadFile(InputStream in) throws IOException
    {
        String section = "";
        String line;
        boolean ended = false;

        int c;
        List<Integer> buf = new ArrayList<Integer>();
        while (!ended)
        {
            buf.clear();
            while (true)
            {
                c = in.read();

                if (c == -1)
                {
                    ended = true;
                    break;
                }
                if (c == 13 || c == 10)
                    break;
                else
                    buf.add(c);
            }
            if (buf.size() < 1)
                continue;

            byte[] arr = new byte[buf.size()];
            for (int i = 0; i < buf.size(); i++)
                arr[i] = buf.get(i).byteValue();
            line = new String(arr, "utf-8");

            if (line.startsWith(";"))
                continue;
            if (line.startsWith("["))
            {
                section = line.substring(1, line.lastIndexOf("]")).trim();
                continue;
            }
            if (line.length() < 1)
                continue;
            if (section.length() > 0)
                addProperty(section, line);
        }

    }

    public void saveFile(File file)
    {
        try
        {
            List<String> sections_list = new ArrayList<String>();
            for (String key : map.keySet())
            {
                String section = key.split(SECTION_DELIM)[0];
                if (!sections_list.contains(section))
                    sections_list.add(section);
            }
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            for (String sect : sections_list)
            {
                String sep = System.getProperty("line.separator");
                writer.write("[" + sect + "]" + sep);
                for (String key : map.keySet())
                {
                    String[] keys = key.split(SECTION_DELIM);
                    if (sect.equals(keys[0]))
                    {
                        writer.write(keys[1] + "=" + map.get(key) + sep);
                    }
                }
            }
            writer.close();
        }
        catch (Exception e)
        {
            Log.info("Error while saving ini file: " + file.getName());
        }
    }

    public void addProperty(String section, String line)
    {
        int equalIndex = line.indexOf("=");

        if (equalIndex > 0)
        {
            String name = section + SECTION_DELIM + line.substring(0, equalIndex).trim();
            String value = line.substring(equalIndex + 1).trim();
            map.put(name, value);
        }
    }

    public String getProperty(String section, String var, String def)
    {
        String s = map.get(section + SECTION_DELIM + var);
        if (s == null)
            return def;
        else
            return s;
    }

    public String getProperty(String section, String var)
    {
        String s = map.get(section + SECTION_DELIM + var);
        if (s == null)
            return "";
        else
            return s;
    }

    public void deleteProperty(String section, String var)
    {
        if (map.containsKey(section + SECTION_DELIM + var))
            map.remove(section + SECTION_DELIM + var);
    }

    public int getProperty(String section, String var, int def)
    {
        return Integer.decode(getProperty(section, var, Integer.toString(def)));
    }

    public boolean getProperty(String section, String var, boolean def)
    {
        String sval = getProperty(section, var, def ? "1" : "0");
        return sval.equalsIgnoreCase("1") || sval.equalsIgnoreCase("True") || sval.equalsIgnoreCase("Yes");
    }

    public void putProperty(String section, String var, String val)
    {
        map.put(section + SECTION_DELIM + var.trim(), val.trim());
    }

    public void putProperty(String section, String var, int val)
    {
        map.put(section + SECTION_DELIM + var.trim(), String.valueOf(val));
    }

    public void putProperty(String section, String var, boolean val)
    {
        map.put(section + SECTION_DELIM + var.trim(), val ? "1" : "0");
    }
}