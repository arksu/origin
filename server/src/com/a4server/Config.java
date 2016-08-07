package com.a4server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 01.01.2015.
 */
public class Config
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class.getName());

	public static final int LOGIN_PROTO_VERSION = 3;
	public static final int GAME_PROTO_VERSION = 3;

	public static final String CONFIGURATION_FILE = "./config/server.properties";
	public static final String HIKARI_CONFIG_FILE = "./config/HikariDB.properties";

	public static boolean DEBUG;

	public static String LOGIN_BIND_ADDRESS;
	// куда коннектится клиенту
	public static String GAME_SERVER_HOST;
	public static int GAME_SERVER_PORT;
	public static int PORT_LOGIN;
	// сколько потоков обслуживают сеть
	public static int LOGIN_NET_WORKER_THREADS;
	// сколько потоков разбирают пакеты
	public static int LOGIN_NET_READER_THREADS;
	// сколько потоков обслуживают сеть
	public static int GAME_NET_WORKER_THREADS;
	// сколько потоков разбирают пакеты
	public static int GAME_NET_READER_THREADS;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
	// таймаут на закрытие соединения с базой.
	public static int DATABASE_CONNECTION_CLOSE_TIME;
	// размер очереди для чтения пакетов
	public static int NET_PACKET_RECV_QUEUE_SIZE;
	// параметры для хэширования scrypt
	public static int SCRYPT_N = 2048;
	public static int SCRYPT_R = 8;
	public static int SCRYPT_P = 1;

	/**
	 * размер пула потоков для general заданий
	 */
	public static int THREAD_P_GENERAL;

	/**
	 * размеры мира
	 */
	public static int WORLD_SG_WIDTH;
	public static int WORLD_SG_HEIGHT;
	public static int WORLD_LEVELS;

	public static void loadGameServer()
	{
		DATABASE_CONNECTION_CLOSE_TIME = 1000;

		GAME_NET_WORKER_THREADS = 2;
		GAME_NET_READER_THREADS = 2;
		GAME_SERVER_HOST = "*";
		GAME_SERVER_PORT = 2041;
		WORLD_SG_WIDTH = 1;
		WORLD_SG_HEIGHT = 1;
		WORLD_LEVELS = 5;
	}

	public static void loadLoginServer()
	{
		LOGIN_BIND_ADDRESS = "*";
		PORT_LOGIN = 2040;
		LOGIN_NET_WORKER_THREADS = 2;
		LOGIN_NET_READER_THREADS = 2;
		GAME_SERVER_HOST = "31.25.28.248";
		GAME_SERVER_PORT = 2041;
		SCRYPT_N = 2048;
	}

	public static void load()
	{
		NET_PACKET_RECV_QUEUE_SIZE = 16;
		DEBUG = true;
		DATABASE_CONNECTION_CLOSE_TIME = 500;
		THREAD_P_GENERAL = 2;

		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			loadGameServer();
		}
		if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			loadLoginServer();
		}
	}
}
