# server
game server for survival MMO game Origin

run local game server:

1. init mysql database:

	```mysql -u root -p < ./etc/sql/init.sql``` - create database and user with valid rights, tables
	```mysql -u root -p < ./etc/sql/test_data.sql``` - fill test data
	
2. import some map data: ```gradle rg_map``` - this will import one map supergrid from ./etc/map.bmp

3. in one console start login server - ```gradle rl```

4. in two console start game server - ```gradle rg```, you need started both login and game servers
	
build distributive: 
```gradle build``` - after complete you will have 2 ZIP distributive archives in ./build/distr
