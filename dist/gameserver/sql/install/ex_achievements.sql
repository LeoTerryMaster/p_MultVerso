CREATE TABLE IF NOT EXISTS `ex_achievements` (
  `objId` INT NOT NULL DEFAULT '0',
  `achId` INT NOT NULL DEFAULT '0',
  `value` INT NOT NULL DEFAULT '0',
  PRIMARY KEY (`objId`,`achId`)
) ENGINE=InnoDB;