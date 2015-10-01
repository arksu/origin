#!/bin/sh
mysqldump --default-character-set=utf8 -d -R -h 127.0.0.1 -u root -p1 --databases a4server > ./server/res/sql/struct.sql
