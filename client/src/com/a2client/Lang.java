package com.a2client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Lang
{
	private static final Logger _log = LoggerFactory.getLogger(Lang.class.getName());

	static private Set<Properties> _properties = new HashSet<>();

	static private String _locale = "en_US";

	static void setLocale(String locale)
	{
		_locale = locale;
	}

	public static String getTranslate(String key)
	{
		String msg = "";
		for (Properties p : _properties)
		{
			msg = p.getProperty(key);
			if (msg != null && !msg.isEmpty())
			{
				return msg;
			}
		}
		if (msg == null || msg.isEmpty())
		{
			msg = key;
		}
		return msg;
	}

	static void loadTranslate()
	{
		try
		{
			Properties p = new Properties();
			p.load(getStream("login"));
			_properties.add(p);

			p = new Properties();
			p.load(getStream("game"));
			_properties.add(p);
		}
		catch (IOException e)
		{
			_log.warn("loadTranslate error " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static InputStream getStream(String type)
	{
		InputStream is = Main.class.getResourceAsStream("/translate/" + type + "." + _locale + ".properties");
		if (is != null) return is;
		String[] split = _locale.split("_");
		is = Main.class.getResourceAsStream("/translate/" + split[0] + "/" + type + "." + _locale + ".properties");

		return is;
	}
}
