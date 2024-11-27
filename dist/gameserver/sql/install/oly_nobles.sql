CREATE TABLE IF NOT EXISTS `oly_nobles` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `class_id` smallint(6) unsigned NOT NULL DEFAULT '0',
  `char_name` varchar(45) NOT NULL DEFAULT '',
  `points_current` smallint(6) NOT NULL DEFAULT '0',
  `points_past` smallint(6) NOT NULL DEFAULT '0',
  `points_pre_past` smallint(6) NOT NULL DEFAULT '0',
  `class_free_cnt` smallint(6) NOT NULL DEFAULT '0',
  `class_based_cnt` smallint(6) NOT NULL DEFAULT '0',
  `team_cnt` smallint(6) NOT NULL DEFAULT '0',
  `comp_win` smallint(6) NOT NULL DEFAULT '0',
  `comp_loose` smallint(6) NOT NULL DEFAULT '0',
  `comp_done` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`char_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;