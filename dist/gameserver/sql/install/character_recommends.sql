CREATE TABLE IF NOT EXISTS `character_recommends` (
	`objId` INT NOT NULL,
	`targetId` INT NOT NULL,
	PRIMARY KEY  (`objId`,`targetId`)
) ENGINE=MyISAM;