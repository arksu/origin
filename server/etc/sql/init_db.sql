DROP DATABASE IF EXISTS a4server;
CREATE DATABASE IF NOT EXISTS a4server;

DROP USER 'a4server'@'localhost';
CREATE USER 'a4server'@'localhost' IDENTIFIED BY 'a4server';
GRANT ALL PRIVILEGES ON a4server.* TO 'a4server'@'%' WITH GRANT OPTION;


-- migrate
ALTER TABLE `sg_0_obj` ADD `heading` SMALLINT NOT NULL DEFAULT '0' AFTER `y`;