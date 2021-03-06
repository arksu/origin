package com.a4server.gameserver.model.collision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 07.01.2015.
 */
public class Move
{
	private static final Logger _log = LoggerFactory.getLogger(MoveType.class.getName());

	public enum MoveType
	{
		// используется только когда объект спавнится в мир, или телепорт в другое место
		SPAWN,
		// передвижение по суше
		WALK,
		// плывет по воде
		SWIMMING
	}
}
