package com.a4server.loginserver;

import com.a4server.Config;
import com.a4server.Database;
import com.a4server.Server;
import com.a4server.loginserver.network.LoginServerHandler;
import com.a4server.util.network.AsyncPacketReader;
import com.a4server.util.network.PacketDecoder;
import com.a4server.util.network.PacketEncoder;
import com.a4server.util.scrypt.SCryptUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

/**
 * Created by arksu on 01.01.2015.
 */
public class LoginServer
{
    private static final Logger _log = LoggerFactory.getLogger(LoginServer.class.getName());
    private static LoginServer _instance;

    private InetAddress _bindAddress = null;
    private final AsyncPacketReader<LoginClient> _reader;

    public static void main(String[] args) throws Exception
    {
        Server.serverMode = Server.MODE_LOGINSERVER;
        _instance = new LoginServer();
    }

    public LoginServer()
    {
        // Load Config
        Config.load();

        logPrintSection("SCrypt");
        long t = System.currentTimeMillis();
        String hash = SCryptUtil.scrypt("123", Config.SCRYPT_N, Config.SCRYPT_R, Config.SCRYPT_P);
        _log.debug("SCrypt: time " + (System.currentTimeMillis() - t) + " ms pass: 123 hash: " + hash);
        t = System.currentTimeMillis();
        if (SCryptUtil.check("123", hash))
        {
            _log.debug("SCrypt: time " + (System.currentTimeMillis() - t) + " ms check: OK");
        }
        else
        {
            _log.debug("SCrypt: FALSE");
        }

        logPrintSection("Database");
        Database.getInstance();

        try
        {
            LoginController.load();
        }
        catch (GeneralSecurityException e)
        {
            _log.error("FATAL: Failed initializing LoginController. Reason: " + e.getMessage(), e);
            System.exit(1);
        }

        if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
        {
            try
            {
                _bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
            }
            catch (UnknownHostException e)
            {
                _log.warn("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e.getMessage(), e);
            }
        }

        _reader = new AsyncPacketReader<>(Config.LOGIN_NET_READER_THREADS, "loginPktReader");

        try
        {
            StartListen();
        }
        catch (Exception e)
        {
            _log.error("ERROR: Start listen failed. Reason: " + e.getMessage(), e);
            System.exit(-1);
        }
    }

    public void StartListen() throws Exception
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(Config.LOGIN_NET_WORKER_THREADS);
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 256);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>()
                    {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception
                        {
                            ch.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new LoginServerHandler(_reader));
                        }
                    });

            _log.info("Login server bind to " + Config.LOGIN_BIND_ADDRESS + " : " + Config.PORT_LOGIN + "...");
            // Bind and start to accept incoming connections.

            ChannelFuture future = b.bind(_bindAddress, Config.PORT_LOGIN);
            System.gc();
            // maxMemory is the upper limit the jvm can use, totalMemory the size of
            // the current allocation pool, freeMemory the unused memory in the
            // allocation pool
            long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime()
                    .totalMemory()) + Runtime.getRuntime()
                    .freeMemory()) / 1048576;
            long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
            _log.info(getClass().getSimpleName() + ": Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");

            future.sync().channel().closeFuture().sync();
        }
        finally
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void logPrintSection(String s)
    {
        s = "=[ " + s + " ]";
        while (s.length() < 78)
        {
            s = "-" + s;
        }
        _log.info(s);
    }
}
