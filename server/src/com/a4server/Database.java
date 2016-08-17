package com.a4server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * работа с базой данных
 * Created by arksu on 01.01.2015.
 */
public class Database
{
	private static final Logger _log = LoggerFactory.getLogger(Database.class);

	private static Database _instance;
	private static volatile ScheduledExecutorService _executor;
	private static DataSource _source;

	/**
	 * нужна полная инициализация базы
	 */
	public static boolean _needInitialize = false;

	public static Database getInstance()
	{
		synchronized (Database.class)
		{
			if (_instance == null)
			{
				try
				{
					_instance = new Database();
				}
				catch (Exception e)
				{
					_log.error("FATAL: cant init database!");
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
		return _instance;
	}

	public Database() throws SQLException
	{
		HikariConfig config = new HikariConfig(Config.HIKARI_CONFIG_FILE);
		_source = new HikariDataSource(config);
		_source.getConnection().close();

		if (_needInitialize)
		{
			initDB();
		}
	}

	public Connection getConnection()
	{
		Connection con = null;
		while (con == null)
		{
			try
			{
				con = _source.getConnection();
				if (Server.serverMode == Server.MODE_GAMESERVER)
				{
					ThreadPoolManager.getInstance()
									 .scheduleGeneral(new ConnectionCloser(con, new RuntimeException()),
													  Config.DATABASE_CONNECTION_CLOSE_TIME);
				}
				else
				{
					getExecutor().schedule(new ConnectionCloser(con, new RuntimeException()), 60, TimeUnit.SECONDS);
				}
			}
			catch (SQLException e)
			{
				_log.warn("DatabaseFactory: getConnection() failed, trying again " + e.getMessage(), e);
			}
		}
		return con;
	}

	private static ScheduledExecutorService getExecutor()
	{
		if (_executor == null)
		{
			synchronized (Database.class)
			{
				if (_executor == null)
				{
					_executor = Executors.newSingleThreadScheduledExecutor();
				}
			}
		}
		return _executor;
	}

	/**
	 * закрываем соединение с базой по таймауту
	 */
	private static class ConnectionCloser implements Runnable
	{
		private static final Logger _log = LoggerFactory.getLogger(ConnectionCloser.class.getName());

		/**
		 * The connection.
		 */
		private final Connection c;

		/**
		 * The exception.
		 */
		private final RuntimeException exp;

		/**
		 * Instantiates a new connection closer.
		 * @param con the con
		 * @param e the e
		 */
		public ConnectionCloser(Connection con, RuntimeException e)
		{
			c = con;
			exp = e;
		}

		@Override
		public void run()
		{
			try
			{
				if (!c.isClosed())
				{
					_log.warn("Unclosed connection! Trace: " + exp.getStackTrace()[1], exp);
				}
			}
			catch (SQLException e)
			{
				_log.warn("", e);
			}
		}
	}

	public boolean isTableExist(String tname)
	{
		String query = "SHOW TABLES LIKE ?";
		try (Connection con = getConnection();
			 PreparedStatement st = con.prepareStatement(query))
		{
			st.setString(1, tname);
			try (ResultSet rset = st.executeQuery())
			{
				return rset.next();
			}

		}
		catch (SQLException e)
		{
			_log.error("Error isTableExist: " + tname);
			return false;
		}
	}

	public boolean executeQueryQuite(String query)
	{
		try (Connection con = getConnection();
			 PreparedStatement st = con.prepareStatement(query))
		{
			st.executeUpdate();
			return true;
		}
		catch (SQLException e)
		{
			_log.error("Error execute query: " + query + " " + e.getMessage());
			return false;
		}
	}

	/**
	 * инициализировать базу данных
	 * создать нужные таблицы и заполнить тестовыми данными
	 */
	private void initDB()
	{
		// проверим есть ли таблица с супергридом. если нет - ее надо будет заполнить
		boolean needSg = !isTableExist("sg_0");
		List<String> sql = new ArrayList<>();
		final String[] files = new String[]{"/sql/struct.sql", "/sql/test_data.sql"};
		try
		{
			for (String fileName : files)
			{
				sql.addAll(readSQLFile(
						new BufferedReader(new InputStreamReader(Database.class.getResourceAsStream(fileName)))));
			}
		}
		catch (IOException e)
		{
			_log.error("failed load sql file " + e.getMessage());
		}
		for (String q : sql)
		{
			executeQueryQuite(q);
		}
	}

	/**
	 * прочитать файл с запросами
	 * @return список полных запросов
	 * @throws IOException
	 */
	private List<String> readSQLFile(BufferedReader reader) throws IOException
	{
		List<String> result = new ArrayList<>();
		String query = "";
		String line = reader.readLine();
		while (line != null)
		{
			if (line.matches("^\\s*--.*"))
			{
				continue;
			}
			query += " " + line;
			if (query.matches(".*;\\s*-?-?.*"))
			{
				_log.debug("query: " + query.substring(0, query.length() > 70 ? 70 : query.length()));
				result.add(query);
				query = "";
			}
			line = reader.readLine();
		}
		if (!query.trim().isEmpty())
		{
			result.add(query);
		}
		return result;
	}
}
