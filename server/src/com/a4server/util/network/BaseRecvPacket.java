package com.a4server.util.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 03.01.2015.
 */
public abstract class BaseRecvPacket
{
	private static final Logger _log = LoggerFactory.getLogger(BaseRecvPacket.class.getName());

	private byte[] _buf;
	private int _off;

	public BaseRecvPacket()
	{
		_buf = null;
	}

	public abstract void setClient(NetClient client);

	public void setData(byte[] buf)
	{
		_buf = buf;
		_off = 1; // skip packet id
	}

	public abstract void readImpl();

	public abstract void run();

	public int readC()
	{
		int result = _buf[_off++] & 0xff;
		return result;
	}

	public int readH()
	{
		int result = _buf[_off++] & 0xff;
		result |= (_buf[_off++] << 8) & 0xff00;
		return result;
	}

	public int readD()
	{
		int result = _buf[_off++] & 0xff;
		result |= (_buf[_off++] << 8) & 0xff00;
		result |= (_buf[_off++] << 0x10) & 0xff0000;
		result |= (_buf[_off++] << 0x18) & 0xff000000;
		return result;
	}

	public String readS()
	{
		int len = readH();
		String result = null;
		try
		{
			result = new String(_buf, _off, len, "UTF-8");
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
		}
		_off += len;

		return result;
	}

	public final byte[] readB(int length)
	{
		byte[] result = new byte[length];
		System.arraycopy(_buf, _off, result, 0, length);
		_off += length;
		return result;
	}

	public long readQ()
	{
		long result = _buf[_off++] & 0xff;
		result |= (_buf[_off++] & 0xffL) << 8L;
		result |= (_buf[_off++] & 0xffL) << 16L;
		result |= (_buf[_off++] & 0xffL) << 24L;
		result |= (_buf[_off++] & 0xffL) << 32L;
		result |= (_buf[_off++] & 0xffL) << 40L;
		result |= (_buf[_off++] & 0xffL) << 48L;
		result |= (_buf[_off++] & 0xffL) << 56L;
		return result;
	}

	public double readF()
	{
		long result = _buf[_off++] & 0xff;
		result |= (_buf[_off++] & 0xffL) << 8L;
		result |= (_buf[_off++] & 0xffL) << 16L;
		result |= (_buf[_off++] & 0xffL) << 24L;
		result |= (_buf[_off++] & 0xffL) << 32L;
		result |= (_buf[_off++] & 0xffL) << 40L;
		result |= (_buf[_off++] & 0xffL) << 48L;
		result |= (_buf[_off++] & 0xffL) << 56L;
		return Double.longBitsToDouble(result);
	}
}
