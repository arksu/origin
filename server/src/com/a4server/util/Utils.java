package com.a4server.util;

/**
 * Created by arksu on 06.09.15.
 */
public class Utils
{
	public static final int MOD_CONTROL = 1;
	public static final int MOD_SHIFT = 2;
	public static final int MOD_ALT = 4;

	public static boolean isEmpty(String val)
	{
		return val == null || val.isEmpty();
	}
}
