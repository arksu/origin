#!/bin/sh

java -Xms128m -Xmx128m -cp ./../libs/*:login.jar com.a2server.loginserver.LoginServer
