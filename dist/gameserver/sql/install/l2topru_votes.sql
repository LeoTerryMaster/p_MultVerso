CREATE TABLE IF NOT EXISTS `l2topru_votes` (
  `obj_Id` int(11) NOT NULL DEFAULT '0',
  `last_vote` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`obj_Id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

