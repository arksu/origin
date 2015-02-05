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

package com.a2client;

import com.a2client.util.INIFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Lang
{
    private static final Logger _log = LoggerFactory.getLogger(Lang.class.getName());

    static public List<LangItem> langs = new ArrayList<LangItem>();

    public static class LangItem
    {
        public String full_name;
        public String name;

        public LangItem(String name, String full_name)
        {
            this.full_name = full_name;
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name + ": " + full_name;
        }
    }

    static private INIFile lang_file;

    static
    {
        langs.add(new LangItem("ru", "Russian"));
        langs.add(new LangItem("en", "English"));
        langs.add(new LangItem("ua", "Ukrainian"));
        langs.add(new LangItem("pl", "Polish"));
    }

    static public String getTranslate(String section, String text)
    {
        if (lang_file != null)
        {
            return lang_file.getProperty(section, text, section + "_" + text);
        }
        else
        {
            return section + "_" + text;
        }
    }


    static public void LoadTranslate()
    {
        if (Config.current_lang != null && Config.current_lang.length() >= 1)
        {
            if (!LoadFromDisk() && Config.download_translate)
            {
                LoadFromSite();
            }
        }
    }

    static protected boolean LoadFromDisk()
    {
        String fname = "lang_?.txt".replace("?", Config.current_lang);
        File file = new File(fname);
        if (file.exists() && file.canRead())
        {
            try
            {
                FileInputStream fin = new FileInputStream(file);
                lang_file = new INIFile(fin);
                return true;

            }
            catch (Exception e)
            {
                _log.warn("lang file not found on disk: " + fname);
            }
        }
        return false;
    }

    static protected void LoadFromSite()
    {
        try
        {
            String path = Config.lang_path.replace("?", Config.current_lang);
            URL lang_url = new URL(new URI("http", Config.lang_remote_host, path, "").toASCIIString());
            _log.info("load translate: " + lang_url.toString());
            URLConnection c;
            c = lang_url.openConnection();
            c.addRequestProperty("User-Agent", Config.user_agent);
            InputStream in = c.getInputStream();

            String fname = "lang_?.txt".replace("?", Config.current_lang);
            File file = new File(fname);
            if (file.exists())
            {
                if (!file.delete())
                {
                    _log.warn("cant delete lang file: " + fname);
                }
            }
            // пишем получаемые данные на диск и в поток
            OutputStream outputStream = new FileOutputStream(file);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            int n = -1;
            byte[] buffer = new byte[4096];
            while ((n = in.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, n);
                bout.write(buffer, 0, n);
            }
            outputStream.close();
            in.close();

            // полученный поток читаем из памяти
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            lang_file = new INIFile(bin);
        }
        catch (Exception e)
        {
            _log.warn("failed load translate: " + Config.current_lang);
            e.printStackTrace();
        }

    }
}
