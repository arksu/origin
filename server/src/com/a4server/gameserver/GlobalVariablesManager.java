package com.a4server.gameserver;

import com.a4server.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by arksu on 08.01.2015.
 */
public class GlobalVariablesManager
{
	private static final Logger _log = LoggerFactory.getLogger(GlobalVariablesManager.class.getName());

	private static final String LOAD_VARS = "SELECT var,value_str,value_int FROM global_variables";
	private static final String LOAD_VAR_INT = "SELECT value_int FROM global_variables WHERE var=?";
	private static final String SAVE_VAR_INT = "INSERT INTO global_variables (var,value_int) VALUES (?,?) ON DUPLICATE KEY UPDATE value_int=?";
	private static final String SAVE_VAR_STR = "INSERT INTO global_variables (var,value_str) VALUES (?,?) ON DUPLICATE KEY UPDATE value_str=?";

	public final void saveVarInt(String var, int val)
	{
		try (Connection con = Database.getInstance().getConnection();
		     PreparedStatement statement = con.prepareStatement(SAVE_VAR_INT))
		{
			statement.setString(1, var);
			statement.setInt(2, val);
			statement.setInt(3, val);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": problem while saving variable: " + e);
		}
	}

	public final int getVarInt(String var)
	{
		try (Connection con = Database.getInstance().getConnection();
		     PreparedStatement statement = con.prepareStatement(LOAD_VAR_INT))
		{
			statement.setString(1, var);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					return rset.getInt("value_int");
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": problem while loading variable: " + e);
		}
		return -1;
	}

	public static GlobalVariablesManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final GlobalVariablesManager _instance = new GlobalVariablesManager();
	}

}
