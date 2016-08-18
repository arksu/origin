package com.a2client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{

	private static final Logger _log = LoggerFactory.getLogger(Log.class);

	public static void info(String msg)
	{
		_log.info(msg);
	}

	public static void debug(String msg)
	{
		if (!Config.getInstance()._debug) return;
		_log.debug(msg);
	}

	public static void error(String msg)
	{
		_log.error(msg);
	}
}
