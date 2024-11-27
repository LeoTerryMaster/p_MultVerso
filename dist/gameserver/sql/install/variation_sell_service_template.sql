--
-- variation_sell_service_template
--

DROP TABLE IF EXISTS `variation_sell_service_template`;
CREATE TABLE `variation_sell_service_template` (
  `menuId` INT(11) unsigned NOT NULL DEFAULT 0,
  `variationOption1` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `variationOption2` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `consumeList` VARCHAR(255) DEFAULT '',
  PRIMARY KEY (`menuId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
insert into variation_sell_service_template values
(1,14561,700,'4037-10'), -- Big Head
(2,14562,700,'4037-10'), -- Eva
(3,14563,700,'4037-10'), -- Acrobatics
(4,14564,700,'4037-10'), -- Iron Body
(5,14565,700,'4037-10'), -- Firework
(6,14566,700,'4037-10'); -- Music