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
import com.a2client.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ErrorHandler
{

	private static volatile boolean initialized = false;
	private static ExceptionHandler exceptionHandler = new ExceptionHandler();

	private static File errorLog = null;

	public static void initialize()
	{
		if (initialized)
		{
			return;
		}
		System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());

		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

		initialized = true;
	}

	public static void registerExceptionHandler()
	{
		initialize();

		Thread.UncaughtExceptionHandler original = Thread.currentThread().getUncaughtExceptionHandler();

		Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(original));
	}

	private static void saveException(Thread t, Throwable e) throws IOException
	{
		System.out.println("Uncaught exception from thread:" + t);
		e.printStackTrace();

		boolean append = true;
		if (errorLog == null)
		{
			long time = System.currentTimeMillis();
			errorLog = new File("error-" + time + ".log");
			append = false;
		}

		System.out.println("Writing exception data to file:" + errorLog + "   append=" + append);
		FileWriter fOut = new FileWriter(errorLog, append);
		PrintWriter out = new PrintWriter(fOut);
		try
		{
			out.println("Uncaught exception from thread:" + t);
			e.printStackTrace(out);

			if (append)
			{
				out.println();
				out.println(Utils.getMemoryString());
				out.println();
			}
			else
			{
				out.println();
				out.println("Build: " + Main.buildVersion());
				out.println();
			}
		}
		catch (Throwable ex)
		{
			Log.error("Error writing log info" + ex);
		}
		finally
		{
			out.close();
		}
	}

	public static void handle(Throwable t)
	{
		Thread thread = Thread.currentThread();
		Thread.UncaughtExceptionHandler ueh = thread.getUncaughtExceptionHandler();
		if (!(ueh instanceof ExceptionHandler))
		{
			try
			{
				saveException(thread, t);
			}
			catch (Throwable bad)
			{
				System.err.println("Error saving exception information:" + bad);
				bad.printStackTrace();
				Log.error("Error saving exception information:" + bad);
			}
		}
		ueh.uncaughtException(thread, t);
	}

	public static class ExceptionHandler implements Thread.UncaughtExceptionHandler
	{
		private Thread.UncaughtExceptionHandler original;

		public ExceptionHandler()
		{
		}

		public ExceptionHandler(Thread.UncaughtExceptionHandler original)
		{
			this.original = original;
		}

		public void uncaughtException(Thread t, Throwable e)
		{
			try
			{
				ErrorHandler.saveException(t, e);
			}
			catch (Throwable bad)
			{
				System.err.println("Error saving exception information:" + bad);
				bad.printStackTrace();
				Log.error("Error saving exception information:" + bad);
			}

			try
			{
				if (this.original != null)
				{
					this.original.uncaughtException(t, e);
				}
			}
			catch (Throwable bad)
			{
				System.err.println("Error delegating uncaught exception:" + bad);
				bad.printStackTrace();
				Log.error("Error delegating uncaught exception:" + bad);
			}
		}

		public void handle(Throwable t)
		{
			try
			{
				ErrorHandler.saveException(Thread.currentThread(), t);
			}
			catch (Throwable bad)
			{
				System.err.println("Error saving exception information:" + bad);
				bad.printStackTrace();
				Log.error("Error saving exception information:" + bad);
			}
		}
	}
}