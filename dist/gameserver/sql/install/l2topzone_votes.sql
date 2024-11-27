CREATE TABLE IF NOT EXISTS `l2topzone_votes` (
  `player_key` varchar(64) NOT NULL DEFAULT '',
  `last_check` int(11) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_key`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
