package com.a4server.loginserver.network.serverpackets;

/**
 * Created by arksu on 03.01.2015.
 */
public class LoginFail extends LoginServerPacket
{
    public static enum LoginFailReason
    {
        REASON_USER_OR_PASS_WRONG(0x01),
        REASON_USER_NOT_FOUND(0x02),
        REASON_PERMANENTLY_BANNED(0x03),
        REASON_ACCOUNT_IN_USE(0x04),
        REASON_PURGE_TIMEOUT(0x05),
        REASON_UNKNOWN_ERROR(0x200);

        private final int _code;

        LoginFailReason(int code)
        {
            _code = code;
        }

        public final int getCode()
        {
            return _code;
        }
    }

    private LoginFailReason _reason;

    public LoginFail(LoginFailReason reason)
    {
        _reason = reason;
    }

    @Override
    protected void write()
    {
        //SLoginFail 0x03
        writeC(0x03);

        writeC(_reason.getCode());
    }
}
