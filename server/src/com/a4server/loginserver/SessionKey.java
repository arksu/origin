package com.a4server.loginserver;

/**
 * Created by arksu on 03.01.2015.
 */
public class SessionKey
{
	private final int _id1;
	private final int _id2;

	public SessionKey(int id1, int id2)
	{
		_id1 = id1;
		_id2 = id2;
	}

	public int getId1()
	{
		return _id1;
	}

	public int getId2()
	{
		return _id2;
	}

	@Override
	public boolean equals(Object that)
	{
		if (this == that)
		{
			return true;
		}
		if (!(that instanceof SessionKey))
		{
			return false;
		}
		final SessionKey key = (SessionKey) that;

		return ((_id1 == key._id1) && (_id2 == key._id2));
	}
}
