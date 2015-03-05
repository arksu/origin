package com.a2client.network.login;

/**
 * Created by arksu on 21.02.15.
 */
public class Crypt
{
	/**
	 * параметры хэширования которые передает нам сервер
	 */
	public static int SCRYPT_N = 0;
	public static int SCRYPT_P = 0;
	public static int SCRYPT_R = 0;

	public static boolean initialized()
	{
		return SCRYPT_N != 0 && SCRYPT_P != 0 && SCRYPT_R != 0;
	}

	/**
	 * это хэш или пароль?
	 * @param some строка с хешем или паролем
	 * @return истина если это хэш от пароля
	 */
	public static boolean isPassowrdHash(String some)
	{
		return some.startsWith("$s0$") && some.length() > 60;
	}
}
