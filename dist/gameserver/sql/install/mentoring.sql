CREATE TABLE IF NOT EXISTS `mentoring` (
  `mentorObjId` int(11) NOT NULL,
  `menteeObjId` int(11) NOT NULL,
  PRIMARY KEY (`mentorObjId`,`menteeObjId`)
) ENGINE=MyISAM;