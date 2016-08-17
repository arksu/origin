# server
game & login server for survival MMO game Origin
http://origin-world.com

to deploy server:

1. init mysql db:

	```mysql -u root -p < init_db.sql``` - you need to create database and user with valid rights
	```mysql -u root -p < struct.sql``` - create tables
	```mysql -u root -p < test_data.sql``` - fill test data
	
	OR
	
	```gradle rgi``` - special task for game server when it init db on server start, and fill test data, may be DEPRECATED...
	
2. ```gradle build``` 
    after it you will have 2 jar files for login and game server in dir: build/dist/

3. copy config files from ./config dir to build/dist/login and build/dist/game (like: ```cp config/HikariDB.properties build/dist/login/config/``` and ```cp config/HikariDB.properties build/dist/game/config```)

4. now you can start login server:

	```cd build/dist/login/```
	
	```./startLoginServer.sh```
	
    and start game server:

	```cd build/dist/game/```
	
	```./startLoginServer.sh```
	
    OR

    ```gradle rl``` - start login server
    
    ```gradle rg``` - start game server

5. ...PROFIT

