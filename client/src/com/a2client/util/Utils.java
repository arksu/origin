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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.io.File;

public class Utils
{
/*
	public static byte signed_byte(int b)
	{
		if (b > 127)
		{
			return ((byte) (-256 + b));
		}
		else
		{
			return ((byte) b);
		}
	}

	// вернуть байт к целому
	public static int unsigned_byte(byte b)
	{
		return b & 0xff;
	}

	// unsigned int16 encode
	public static void uint16e(int num, byte[] buf, int off)
	{
		buf[off] = signed_byte(num & 0xff);
		buf[off + 1] = signed_byte((num & 0xff00) >> 8);
	}

	// unsigned int32 encode
	public static void uint32e(long num, byte[] buf, int off)
	{
		buf[off + 3] = signed_byte((int) (num & 0xff));
		buf[off + 2] = signed_byte((int) ((num & 0xff00) >> 8));
		buf[off + 1] = signed_byte((int) ((num & 0xff0000) >> 16));
		buf[off] = signed_byte((int) ((num & 0xff000000) >> 24));
	}

	// int32 decode
	public static int int32d(byte[] buf, int off)
	{
		long u = uint32d(buf, off);
		if (u > Integer.MAX_VALUE)
		{
			return ((int) ((((long) Integer.MIN_VALUE) * 2) - u));
		}
		else
		{
			return ((int) u);
		}
	}

	// кодируем инт для передачи на сервер
	public static void int32e(int num, byte[] buf, int off)
	{
		if (num < 0)
		{
			uint32e(0x100000000L + ((long) num), buf, off);
		}
		else
		{
			uint32e(num, buf, off);
		}
	}

	// unsigned int16 decode
	public static int uint16d(byte[] buf, int off)
	{
		return (unsigned_byte(buf[off]) + (unsigned_byte(buf[off + 1]) * 256));
	}

	public static int int16d(byte[] buf, int off)
	{
		int u = uint16d(buf, off);
		if (u > 32767)
		{
			return (-65536 + u);
		}
		else
		{
			return (u);
		}
	}

	// unsigned int32 decode
	public static long uint32d(byte[] buf, int off)
	{
		return (unsigned_byte(buf[off + 3]) +
				(unsigned_byte(buf[off + 2]) * 256) +
				(unsigned_byte(buf[off + 1]) * 65536) +
				(unsigned_byte(buf[off + 0]) * 16777216));
	}
*/

	//    public static String strd(byte[] buf, int[] off) {
	//    	int i;
	//    	for(i = off[0]; buf[i] != 0; i++);
	//    	String ret;
	//    	try {
	//    	    ret = new String(buf, off[0], i - off[0], "utf-8");
	//    	} catch(UnsupportedEncodingException e) {
	//    	    throw(new RuntimeException(e));
	//    	}
	//    	off[0] = i + 1;
	//    	return(ret);
	//    }
	public static int max(int a, int b)
	{
		return a > b ? a : b;
	}
	public static float max(float a, float b)
	{
		return a > b ? a : b;
	}

	public static int min(int a, int b)
	{
		return a < b ? a : b;
	}
	public static float min(float a, float b)
	{
		return a < b ? a : b;
	}

	public static float max(float a, float b, float c, float d, float e)
	{
		float m = a;
		m = b > m ? b : m;
		m = c > m ? c : m;
		m = e > m ? e : m;
		return d > m ? d : m;
	}

	public static float min(float a, float b, float c, float d, float e)
	{
		float m = a;
		m = b < m ? b : m;
		m = c < m ? c : m;
		m = e < m ? e : m;
		return d < m ? d : m;
	}

	public static String data2string(long data)
	{
		long bufd = data;
		StringBuilder buf = new StringBuilder();
		//		if (bufd > 1024 * 1024 * 1024) {
		//			buf.append(bufd / (1024 * 1024 * 1024));
		//			buf.append("Gb ");
		//			bufd /= 1024;
		//		}
		//		if (bufd > 1024 * 1024) {
		//			buf.append(bufd / (1024 * 1024));
		//			buf.append("M ");
		//			bufd /= 1024;
		//		}
		if (bufd > 1024)
		{
			buf.append(bufd / (1024));
			buf.append(" k ");
			bufd /= 1024;
		}
		else
		{
			buf.append(bufd);
			buf.append(" b");
		}
		return buf.toString();
	}

	public static String getHexString(byte[] b)
	{
		String result = "";
		for (byte aB : b)
		{
			result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1) + " ";
		}
		return result;
	}

	public static String getMemoryString()
	{
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory();
		long total = rt.totalMemory();
		long max = rt.maxMemory();

		long used = total - free;

		long percent1 = used * 100L / total;
		long percent2 = used * 100L / max;

		return "Working memory: " + percent1 + "% (" + used + "/" + total + ")" + "  VM Max: " + percent2 + "% (" + used + "/" + max + ")";
	}

	public static void rotateLog()
	{
		int maxLogs = 2;

		File last = new File("client-" + maxLogs + ".log");
		if (last.exists())
		{
			if (!last.delete())
			{
				System.err.println("Error removing old log file:" + last);
			}
		}
		for (int i = maxLogs - 1; i > 0; i--)
		{
			File current = new File("client-" + i + ".log");
			if (current.exists())
			{
				if (!current.renameTo(last))
				{
					System.err.println("Error renaming:" + current + " to:" + last);
				}
			}
			last = current;
		}

		File current = new File("client.log");
		if (current.exists())
		{
			if (!current.renameTo(last))
			{
				System.err.println("Error renaming:" + current + " to:" + last);
			}
		}
	}

	public static boolean isEmpty(String s)
	{
		return s == null || s.isEmpty();
	}

	public static float baryCentric(Vector3 p1, Vector3 p2, Vector3 p3, Vector2 pos)
	{
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}
}
