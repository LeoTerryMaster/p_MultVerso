CREATE TABLE IF NOT EXISTS `pawnshop` (
  `id` int(11) NOT NULL,
  `ownerId` int(11) NOT NULL,
  `itemType` int(11) NOT NULL,
  `amount` int(11) NOT NULL,
  `enchantLevel` int(11) NOT NULL,
  `currency` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `varOpt1` int(11) NOT NULL,
  `varOpt2` int(11) NOT NULL
) ENGINE=InnoDB;

DELIMITER $$

DROP PROCEDURE IF EXISTS `lip_ex_PawnShopLoadItems` $$
CREATE PROCEDURE `lip_ex_PawnShopLoadItems`()
 SQL SECURITY DEFINER
BEGIN
  SELECT SQL_NO_CACHE
    `ps`.`id` AS `id`,
    `ps`.`ownerId` AS `ownerId`,
    `ps`.`itemType` AS `itemType`,
    `ps`.`amount` AS `amount`,
    `ps`.`enchantLevel` AS `enchantLevel`,
    `ps`.`currency` AS `currency`,
    `ps`.`price` AS `price`,
    `ps`.`varOpt1` AS `varOpt1`,
    `ps`.`varOpt2` AS `varOpt2`
  FROM `pawnshop` `ps`;
END $$

DROP PROCEDURE IF EXISTS `lip_ex_PawnShopStoreItem` $$
CREATE PROCEDURE `lip_ex_PawnShopStoreItem`(IN iPawnShopId INTEGER UNSIGNED,
                                            IN iOwnerId INTEGER UNSIGNED,
                                            IN iItemType MEDIUMINT UNSIGNED,
                                            IN iAmount INTEGER UNSIGNED,
                                            IN iEnchant SMALLINT UNSIGNED,
                                            IN iCurrencyTypeType MEDIUMINT UNSIGNED,
                                            IN iPrice INTEGER UNSIGNED,
                                            IN iVarStat1 SMALLINT UNSIGNED,
                                            IN iVarStat2 SMALLINT UNSIGNED)
 SQL SECURITY DEFINER
BEGIN
  REPLACE LOW_PRIORITY INTO `pawnshop`(
    `id`,
    `ownerId`,
    `itemType`,
    `amount`,
    `enchantLevel`,
    `currency`,
    `price`,
    `varOpt1`,
    `varOpt2`
  ) VALUES (
    iPawnShopId,
    iOwnerId,
    iItemType,
    iAmount,
    iEnchant,
    iCurrencyTypeType,
    iPrice,
    iVarStat1,
    iVarStat2
  );
END $$

DROP PROCEDURE IF EXISTS `lip_ex_PawnShopDeleteItem` $$
CREATE PROCEDURE `lip_ex_PawnShopDeleteItem`(IN iPawnShopId INTEGER UNSIGNED)
 SQL SECURITY DEFINER
BEGIN
  DELETE FROM `pawnshop` WHERE `id` = iPawnShopId;
END $$

DELIMITER ;