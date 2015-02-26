package com.a4server.loginserver;

import com.a4server.Config;
import com.a4server.Database;
import com.a4server.loginserver.network.serverpackets.LoginFail;
import com.a4server.util.Rnd;
import com.a4server.util.scrypt.SCryptUtil;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by arksu on 03.01.2015.
 */
public class LoginController
{
    private static final Logger _log = LoggerFactory.getLogger(LoginController.class.getName());

    private static LoginController _instance;

    protected FastMap<String, LoginClient> _loginServerClients = new FastMap<String, LoginClient>().shared();

    // SQL Queries
    private static final String USER_INFO_SELECT = "SELECT password, accessLevel FROM accounts WHERE login=?";
    private static final String ACCOUNT_INFO_UPDATE = "UPDATE accounts SET lastActive = ?, lastIP = ? WHERE login = ?";
    private static final String ACCOUNT_ASSIGN_SESSIONKEY = "UPDATE accounts SET key1 = ?, key2 = ? WHERE login = ?";

    /**
     * Time before kicking the client if he didn't logged yet
     */
    public static final int LOGIN_TIMEOUT = 60 * 1000;


    public static void load() throws GeneralSecurityException
    {
        synchronized (LoginController.class)
        {
            if (_instance == null)
            {
                _instance = new LoginController();
            }
            else
            {
                throw new IllegalStateException("LoginController can only be loaded a single time.");
            }
        }
    }

    public static enum AuthLoginResult
    {
        INVALID_PASSWORD,
        ACCOUNT_BANNED,
        ALREADY_ON_LS,
        ALREADY_ON_GS,
        AUTH_SUCCESS,
        USER_NOT_FOUND
    }

    /**
     * авторизация на логин сервере
     * @param account логин
     * @param hash хэш от пароля
     * @param client клиент
     * @return подробные причины отказа или успех
     */
    public AuthLoginResult tryAuthLogin(String account, String hash, LoginClient client)
    {
        AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
        // check auth
        if (loginValid(account, hash, client))
        {
            // login was successful, verify presence on Gameservers
            ret = AuthLoginResult.ALREADY_ON_GS;
            if (!isAccountInAnyGameServer(account))
            {
                // account isnt on any GS verify LS itself
                ret = AuthLoginResult.ALREADY_ON_LS;

                // если еще не авторизован
                if (_loginServerClients.putIfAbsent(account, client) == null)
                {
                    ret = AuthLoginResult.AUTH_SUCCESS;
                }
            }
        }
        else
        {
            if (client.getAccessLevel() == -200)
            {
                ret = AuthLoginResult.USER_NOT_FOUND;
            }
            else
            {
                if (client.getAccessLevel() < 0)
                {
                    ret = AuthLoginResult.ACCOUNT_BANNED;
                }
            }
        }
        return ret;
    }

    public boolean isAccountInAnyGameServer(String account)
    {
        // TODO : проверка зашел ли игрок на какой либо гейм сервер
        return false;
    }

    public boolean loginValid(String user, String hash, LoginClient client)// throws HackingException
    {
        boolean ok = false;
        InetAddress address = client.getInetAddress();

        // player disconnected meanwhile
        if ((address == null) || (user == null))
        {
            return false;
        }

        _log.info("try auth user " + user);

        try
        {
            int access = 0;
            String password = "";
            try (Connection con = Database.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(USER_INFO_SELECT))
            {
                ps.setString(1, user);
                try (ResultSet rset = ps.executeQuery())
                {
                    if (rset.next())
                    {
                        access = rset.getInt("accessLevel");
                        password = rset.getString("password");

                        if (Config.DEBUG)
                        {
                            _log.debug("account exists");
                        }
                    }
                }
            }

            // account does not exist
            if (password.isEmpty())
            {
//                if (Config.LOG_LOGIN_CONTROLLER)
//                {
//                    Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountMissing", "loginlog");
//                }

                _log.warn("Account missing for user " + user);

                client.setAccessLevel(-200);
                return false;
            }

            // is this account banned?
            if (access < 0)
            {
//                if (Config.LOG_LOGIN_CONTROLLER)
//                {
//                    Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountBanned", "loginlog");
//                }
                if (Config.DEBUG)
                {
                    _log.debug("account " + user + " access < 0");
                }
                client.setAccessLevel(access);
                return false;
            }

            // проверим хэш пароля
            long time = System.currentTimeMillis();
            ok = SCryptUtil.check(password, hash);
            _log.debug("scrypt check time: " + (System.currentTimeMillis() - time) + " ms");
            if (ok)
            {
                if (Config.DEBUG)
                {
                    _log.debug("password matched, SUCCESS");
                }
                client.setAccessLevel(access);
                long unixTime = System.currentTimeMillis() / 1000L;
                try (Connection con = Database.getInstance().getConnection();
                     PreparedStatement ps = con.prepareStatement(ACCOUNT_INFO_UPDATE))
                {
                    ps.setLong(1, unixTime);
                    ps.setString(2, address.getHostAddress());
                    ps.setString(3, user);
                    ps.execute();
                }
            }
        }
        catch (Exception e)
        {
            _log.warn("Could not check password:" + e.getMessage(), e);
            ok = false;
        }

        if (!ok)
        {
//            if (Config.LOG_LOGIN_CONTROLLER)
//            {
//                Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : LoginFailed", "loginlog");
//            }
        }
        else
        {
//            if (Config.LOG_LOGIN_CONTROLLER)
//            {
//                Log.add("'" + user + "' " + address.getHostAddress() + " - OK : LoginOk", "loginlog");
//            }
        }

        return ok;
    }

    public SessionKey assignSessionKeyToClient(String account, LoginClient client) throws SQLException
    {
        SessionKey key = new SessionKey(Rnd.nextInt(), Rnd.nextInt());

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(ACCOUNT_ASSIGN_SESSIONKEY))
        {
            ps.setInt(1, key.getId1());
            ps.setInt(2, key.getId2());
            ps.setString(3, account);
            ps.execute();
        }
        _loginServerClients.put(account, client);
        return key;
    }

    public static LoginController getInstance()
    {
        return _instance;
    }

    private LoginController() throws GeneralSecurityException
    {
        _log.info("Loading LoginController...");

        Thread _purge = new PurgeThread();
        _purge.setDaemon(true);
        _purge.start();
    }

    public void removeLoginClient(String account)
    {
        if (account == null)
        {
            return;
        }
        _loginServerClients.remove(account);
    }

    public LoginClient getAuthedClient(String account)
    {
        return _loginServerClients.get(account);
    }

    class PurgeThread extends Thread
    {
        public PurgeThread()
        {
            setName("PurgeThread");
        }

        @Override
        public void run()
        {
            while (!isInterrupted())
            {
                for (LoginClient client : _loginServerClients.values())
                {
                    if (client == null)
                    {
                        continue;
                    }
                    if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) < System.currentTimeMillis())
                    {
                        client.close(LoginFail.LoginFailReason.REASON_PURGE_TIMEOUT);
                    }
                }

                try
                {
                    Thread.sleep(LOGIN_TIMEOUT / 2);
                }
                catch (InterruptedException e)
                {
                    return;
                }
            }
        }
    }
}
