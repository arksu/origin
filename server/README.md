# a4server
server of MMO game Origin

under BSD license

to deploy server:
1. clone rep

2. init mysql db:
	mysql -u root -p < init_db.sql
	mysql -u root -p < origin_dump.sql
3. ant, after it you will have 2 jar files for login and game server in dir: build/dist/

4. copy config files from ./config dir to build/dist/login and build/dist/game (like: cp config/HikariDB.properties build/dist/login/config/ and cp config/HikariDB.properties build/dist/game/config)

5. now you can start login server:
	cd build/dist/login/
	./startLoginServer.sh

6. start game server:
	cd build/dist/game/
        ./startLoginServer.sh

7. ...PROFIT

if you have any questions please contact me (skype: ark.su, or email: ark@ark.su)

2015 jan 28
