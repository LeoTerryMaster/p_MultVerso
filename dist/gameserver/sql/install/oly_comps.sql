CREATE TABLE IF NOT EXISTS `oly_comps` (
  `season` int(11) NOT NULL DEFAULT '0',
  `char_id` int(11) NOT NULL DEFAULT '0',
  `rival_id` int(11) NOT NULL DEFAULT '0',
  `rule` tinyint(4) NOT NULL DEFAULT '0',
  `result` tinyint(4) NOT NULL DEFAULT '0',
  `elapsed_time` int(11) NOT NULL DEFAULT '0',
  `mtime` int(11) NOT NULL DEFAULT '0',
  KEY `char_id` (`char_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;