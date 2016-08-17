-- MySQL dump 10.15  Distrib 10.0.21-MariaDB, for osx10.11 (x86_64)
--
-- Host: 127.0.0.1    Database: a4server
-- ------------------------------------------------------
-- Server version	10.0.21-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `a4server`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `a4server` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `a4server`;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `accounts` (
  `login` varchar(45) NOT NULL DEFAULT '',
  `password` varchar(45) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastactive` bigint(13) unsigned NOT NULL DEFAULT '0',
  `accessLevel` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'lvl<0 = banned',
  `lastIP` char(15) DEFAULT NULL,
  `lastServer` tinyint(4) DEFAULT '1',
  `lastChar` tinyint(4) NOT NULL DEFAULT '0',
  `key1` int(11) NOT NULL DEFAULT '0',
  `key2` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`login`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `characters`
--

DROP TABLE IF EXISTS `characters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `characters` (
  `charId` int(11) NOT NULL,
  `account` varchar(45) NOT NULL COMMENT 'accounts.login',
  `charName` varchar(45) NOT NULL,
  `accessLevel` int(11) NOT NULL COMMENT 'lvl<0=banned',
  `x` int(11) NOT NULL DEFAULT '0',
  `y` int(11) NOT NULL DEFAULT '0',
  `lvl` int(11) NOT NULL DEFAULT '0',
  `face` tinyint(4) NOT NULL DEFAULT '0',
  `hairColor` tinyint(4) NOT NULL DEFAULT '0',
  `hairStyle` tinyint(4) NOT NULL DEFAULT '0',
  `sex` tinyint(4) NOT NULL DEFAULT '0',
  `title` varchar(32) DEFAULT '',
  `lastAccess` int(10) unsigned NOT NULL,
  `createDate` int(10) unsigned NOT NULL,
  `onlineTime` int(10) unsigned NOT NULL,
  `del` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT 'deleted?',
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `global_variables`
--

DROP TABLE IF EXISTS `global_variables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `global_variables` (
  `var` char(50) NOT NULL,
  `value_int` int(11) DEFAULT NULL,
  `value_str` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `items` (
  `id` int(10) unsigned NOT NULL COMMENT 'ид вещи (объекта)',
  `itemId` int(10) unsigned NOT NULL COMMENT 'тип вещи',
  `inventoryId` int(10) unsigned NOT NULL COMMENT 'объект которому принадлежит вещь',
  `x` tinyint(3) unsigned NOT NULL COMMENT 'координаты в инвентаре',
  `y` tinyint(3) unsigned NOT NULL,
  `q` mediumint(8) unsigned NOT NULL DEFAULT '10' COMMENT 'качество',
  `amount` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT 'количество',
  `stage` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT 'стадия, доп цифра',
  `ticks` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT 'тиков прошло',
  `ticksTotal` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT 'сколько всего тиков',
  `del` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'deleted?',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sg_0`
--

DROP TABLE IF EXISTS `sg_0`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sg_0` (
  `id` int(11) unsigned NOT NULL,
  `data` blob NOT NULL,
  `last_tick` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sg_0_obj`
--

DROP TABLE IF EXISTS `sg_0_obj`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sg_0_obj` (
  `id` int(11) NOT NULL,
  `grid` int(10) unsigned NOT NULL COMMENT 'номер грида',
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `type` smallint(5) unsigned NOT NULL COMMENT 'тип',
  `hp` smallint(5) unsigned NOT NULL DEFAULT '100',
  `data` text COMMENT 'json',
  `create_tick` int(10) unsigned NOT NULL COMMENT 'время создания, тик',
  `last_tick` int(10) unsigned DEFAULT '0' COMMENT 'время последнего апдейта, тик',
  `del` tinyint(3) unsigned DEFAULT '0' COMMENT 'deleted?',
  PRIMARY KEY (`id`),
  KEY `grid` (`grid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'a4server'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-08-17 21:20:26
