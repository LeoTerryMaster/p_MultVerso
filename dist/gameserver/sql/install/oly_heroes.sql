CREATE TABLE IF NOT EXISTS `oly_heroes` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `count` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `played` tinyint(4) NOT NULL DEFAULT '0',
  `active` tinyint(4) NOT NULL DEFAULT '0',
  `message` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`char_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
