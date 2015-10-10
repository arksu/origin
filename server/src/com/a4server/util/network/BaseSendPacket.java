package com.a4server.util.network;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by arksu on 03.01.2015.
 */
public abstract class BaseSendPacket
{
	private static final Logger _log = LoggerFactory.getLogger(BaseSendPacket.class.getName());

	/**
	 * буфер для записи данных пакета
	 */
	private final ByteArrayOutputStream _bao;

	/**
	 * следующий пакет, можно объединять в цепочку
	 * односвязный список
	 */
	protected BaseSendPacket _next;

	/**
	 * пакет записал себя в буфер
	 */
	private boolean encoded = false;

	protected BaseSendPacket()
	{
		_bao = new ByteArrayOutputStream(32);
	}

	synchronized private void doEncode()
	{
		if (encoded)
		{
			return;
		}
		write();
		encoded = true;
	}

	public void EncodePacket(ByteBuf out)
	{
		if (!encoded)
		{
			doEncode();
		}
		// len
		int len = _bao.size();
		out.writeByte(len & 0xff);
		out.writeByte((len >> 8) & 0xff);
		// data
		out.writeBytes(_bao.toByteArray());

		// если есть следующий прикрепленный пакет - отправим и его
		if (_next != null)
		{
			_next.EncodePacket(out);
		}
	}

	/**
	 * добавить следующий пакет который будет отправлен вслед за этим
	 * позволяет указать очередность отправки пакетов. и одним пакетом отправить сразу группу пакетов
	 * например при добавлении в мир
	 * @param pkt пакет
	 * @return следующий пакет
	 */
	public BaseSendPacket addNext(BaseSendPacket pkt)
	{
		_next = pkt;
		return pkt;
	}

	protected void writeC(int value)
	{
		_bao.write(value & 0xff);
	}

	protected void writeH(int value)
	{
		_bao.write(value & 0xff);
		_bao.write((value >> 8) & 0xff);
	}

	protected void writeD(int value)
	{
		_bao.write(value & 0xff);
		_bao.write((value >> 8) & 0xff);
		_bao.write((value >> 16) & 0xff);
		_bao.write((value >> 24) & 0xff);
	}

	protected void writeF(double org)
	{
		long value = Double.doubleToRawLongBits(org);
		_bao.write((int) (value & 0xff));
		_bao.write((int) ((value >> 8) & 0xff));
		_bao.write((int) ((value >> 16) & 0xff));
		_bao.write((int) ((value >> 24) & 0xff));
		_bao.write((int) ((value >> 32) & 0xff));
		_bao.write((int) ((value >> 40) & 0xff));
		_bao.write((int) ((value >> 48) & 0xff));
		_bao.write((int) ((value >> 56) & 0xff));
	}

	protected void writeS(String text)
	{
		try
		{
			if (text != null)
			{
				byte[] sbuf = text.getBytes("UTF-8");
				writeH(sbuf.length);
				_bao.write(sbuf);
			}
			else
			{
				writeH(0);
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	protected void writeB(byte[] array)
	{
		try
		{
			_bao.write(array);
		}
		catch (IOException e)
		{
			_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	protected void writeQ(long value)
	{
		_bao.write((int) (value & 0xff));
		_bao.write((int) ((value >> 8) & 0xff));
		_bao.write((int) ((value >> 16) & 0xff));
		_bao.write((int) ((value >> 24) & 0xff));
		_bao.write((int) ((value >> 32) & 0xff));
		_bao.write((int) ((value >> 40) & 0xff));
		_bao.write((int) ((value >> 48) & 0xff));
		_bao.write((int) ((value >> 56) & 0xff));
	}

	abstract protected void write();
}
