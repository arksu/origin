#!/bin/sh

cp ../../build/libs/loginserver.jar .
java -Xms128m -Xmx128m -cp ./../libs/*:loginserver.jar com.a4server.loginserver.LoginServer
