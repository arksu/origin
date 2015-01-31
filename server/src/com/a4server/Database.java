package com.a4server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by arksu on 01.01.2015.
 */
public class Database
{
    private static final Logger _log = LoggerFactory.getLogger(Database.class);

    private static Database _instance;
    private static volatile ScheduledExecutorService _executor;
    private static DataSource _source;

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
                            .scheduleGeneral(new ConnectionCloser(con, new RuntimeException()), Config.DATABASE_CONNECTION_CLOSE_TIME);
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
         *
         * @param con the con
         * @param e   the e
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
}
