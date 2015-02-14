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
  `del` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `global_variables` (
  `var` char(50) NOT NULL,
  `value_int` int(11) DEFAULT NULL,
  `value_str` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `sg_0_obj` (
  `id` int(11) NOT NULL,
  `grid` int(10) unsigned NOT NULL COMMENT 'номер грида',
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `type` smallint(5) unsigned NOT NULL COMMENT 'тип',
  `hp` smallint(5) unsigned NOT NULL,
  `data` text,
  `create_tick` int(10) unsigned NOT NULL COMMENT 'время создания, тик',
  `last_tick` int(10) unsigned DEFAULT '0' COMMENT 'время последнего апдейта, тик',
  `del` tinyint(3) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `grid` (`grid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `sg_0` (
  `id` int(11) unsigned NOT NULL,
  `data` blob NOT NULL,
  `last_tick` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;