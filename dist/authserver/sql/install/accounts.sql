CREATE TABLE IF NOT EXISTS `accounts` (
  `login` varchar(32) NOT NULL,
  `password` varchar(255) NOT NULL,
  `lastactive` int(11) DEFAULT NULL,
  `accessLevel` tinyint(6) DEFAULT NULL,
  `lastIP` varchar(15) DEFAULT NULL,
  `lastServerId` int(4) DEFAULT NULL,
  `ban_expire` int(11) NOT NULL DEFAULT '0',
  `allow_ip` varchar(255) NOT NULL DEFAULT '',
  `l2email` varchar(45) DEFAULT NULL,
  `privatekey` varchar(18) DEFAULT NULL,
  PRIMARY KEY (`login`),
  KEY `last_ip` (`lastIP`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


DELIMITER $$

DROP PROCEDURE IF EXISTS `lip_AccountCreate` $$
CREATE PROCEDURE `lip_AccountCreate`(IN `sAccountName` VARCHAR(32), 
                                     IN `sAccountPasswordHash` VARCHAR(255))
BEGIN
    INSERT LOW_PRIORITY INTO `accounts` (
        `login`,
        `password`
    ) VALUES (
        sAccountName,
        sAccountPasswordHash
    );
END $$

DROP PROCEDURE IF EXISTS `lip_AccountLoad` $$
CREATE PROCEDURE `lip_AccountLoad`(IN `sAccountName` VARCHAR(32))
BEGIN
    SELECT 
        `a`.`password` AS `password`, 
        `a`.`accessLevel` AS `accessLevel`, 
        `a`.`lastServerId` AS `lastServerId`, 
        `a`.`lastIP` AS `lastIP`, 
        `a`.`lastactive` AS `lastactive`, 
        `a`.`l2email` AS `email`
    FROM 
        `accounts` `a` 
    WHERE 
        `a`.`login` = sAccountName;
END $$


DROP PROCEDURE IF EXISTS `lip_AccountUpdate` $$
CREATE PROCEDURE `lip_AccountUpdate`(IN `sAccountName` VARCHAR(32), 
                                     IN `sAccountPasswordHash` VARCHAR(255), 
									 IN `iAccessLevel` TINYINT(6), 
									 IN `iLastServerId` INT(4), 
									 IN `sLastIp` VARCHAR(15), 
									 IN `iLastActive` INT(11), 
									 IN `sEmail` VARCHAR(45))
BEGIN
    UPDATE LOW_PRIORITY 
        `accounts` 
    SET
        `password` = sAccountPasswordHash,
        `accessLevel` = iAccessLevel,
        `lastServerId` = iLastServerId,
        `lastIP` = sLastIp,
        `lastactive` = iLastActive,
        `l2email` = sEmail
    WHERE
        `login` = sAccountName
    LIMIT 1;
END $$

DELIMITER ;