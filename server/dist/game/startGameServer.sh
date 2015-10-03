#!/bin/sh

cp ../../build/libs/gameserver.jar .
java -Xms128m -Xmx128m -cp ./../libs/*:gameserver.jar com.a4server.gameserver.GameServer
