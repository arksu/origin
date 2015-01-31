package com.a2client.util.network;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class BaseSendPacket
{
    private static final Logger _log = Logger.getLogger(BaseSendPacket.class.getName());

    private final ByteArrayOutputStream _bao;

    protected BaseSendPacket()
    {
        _bao = new ByteArrayOutputStream(32);
    }

    public byte[] EncodePacket()
    {
        write();
        int len = _bao.size();
        byte[] buf = new byte[len + 2];
        // len
        buf[0] = (byte) (len & 0xff);
        buf[1] = (byte) ((len >> 8) & 0xff);
        // data
        System.arraycopy(_bao.toByteArray(), 0, buf, 2, len);
        return buf;
    }

    public void EncodePacket(ByteBuf out)
    {
        write();
        // len
        int len = _bao.size();
        out.writeByte(len & 0xff);
        out.writeByte((len >> 8) & 0xff);
        // data
        out.writeBytes(_bao.toByteArray());
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
        }
        catch (Exception e)
        {
            _log.warning(getClass().getSimpleName() + ": " + e.getMessage());
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
            _log.warning(getClass().getSimpleName() + ": " + e.getMessage());
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
