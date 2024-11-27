-- `items`
CREATE TABLE IF NOT EXISTS `items` (
  `item_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `owner_id` INT UNSIGNED NOT NULL,
  `item_type` MEDIUMINT UNSIGNED NOT NULL DEFAULT 0,
  `amount` BIGINT /*UNSIGNED*/ NOT NULL DEFAULT 0,
  `location` ENUM('VOID','INVENTORY','PAPERDOLL','WAREHOUSE','FREIGHT','CLANWH','MAIL','PET_INVENTORY','PET_PAPERDOLL') NOT NULL DEFAULT 'VOID',
  `slot` INT NOT NULL DEFAULT -1,
  `enchant` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT `pk_items` PRIMARY KEY (`item_id`),
  KEY `k_owner_id` (`owner_id`),
  KEY `k_item_type` (`item_type`),
  KEY `k_location` (`location`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `items_duration` (
  `item_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `duration` INT NOT NULL DEFAULT -9999,
  CONSTRAINT `pk_items_duration` PRIMARY KEY (`item_id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `items_period` (
  `item_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `period` INT NOT NULL DEFAULT -1,
  CONSTRAINT `pk_items_period` PRIMARY KEY (`item_id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `items_attributes` (
  `item_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `attack_type` TINYINT NOT NULL DEFAULT -2,
  `attack_value` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `defence_fire` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `defence_water` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `defence_wind` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `defence_earth` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `defence_holy` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `defence_unholy` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT `pk_items_attributes` PRIMARY KEY (`item_id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `items_variation` (
  `item_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `stat1` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  `stat2` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT `pk_items_variation` PRIMARY KEY (`item_id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `items_options` (
  `item_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `blessed` INT NOT NULL DEFAULT 0,
  `damaged` INT NOT NULL DEFAULT 0,
  `energy` INT NOT NULL DEFAULT 0,
  `flags` INT UNSIGNED NOT NULL DEFAULT 0,
  `item_vis_type` MEDIUMINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT `pk_items_options` PRIMARY KEY (`item_id`)
) ENGINE=InnoDB;

DELIMITER $$

/* Get full item by item_id (objId) */
DROP PROCEDURE IF EXISTS `lip_GetItem` $$
CREATE PROCEDURE `lip_GetItem` (IN iItemID INTEGER UNSIGNED)
 NOT DETERMINISTIC
 SQL SECURITY DEFINER
BEGIN
  SELECT SQL_NO_CACHE 
    `i`.`item_id` AS `item_id`,
    `i`.`owner_id` AS `owner_id`,
    `i`.`item_type` AS `item_type`,
    `i`.`amount` AS `amount`,
    `i`.`location` AS `location`,
    `i`.`slot` AS `slot`,
    `i`.`enchant` AS `enchant`,
    IFNULL(`id`.`duration`, -9999) AS `duration`,
    IFNULL(`ip`.`period`, -1) AS `period`,
    IFNULL(`ia`.`attack_type`, -2) AS `attack_attr_type`,
    IFNULL(`ia`.`attack_value`, 0) AS `attack_attr_val`,
    IFNULL(`ia`.`defence_fire`, 0) AS `defence_attr_fire`,
    IFNULL(`ia`.`defence_water`, 0) AS `defence_attr_water`,
    IFNULL(`ia`.`defence_wind`, 0) AS `defence_attr_wind`,
    IFNULL(`ia`.`defence_earth`, 0) AS `defence_attr_earth`,
    IFNULL(`ia`.`defence_holy`, 0) AS `defence_attr_holy`,
    IFNULL(`ia`.`defence_unholy`, 0) AS `defence_attr_unholy`,
    IFNULL(`iv`.`stat1`, 0) AS `variation_stat1`,
    IFNULL(`iv`.`stat2`, 0) AS `variation_stat2`,
    IFNULL(`io`.`blessed`, 0) AS `blessed`,
    IFNULL(`io`.`damaged`, 0) AS `damaged`,
    IFNULL(`io`.`energy`, 0) AS `item_energy`,
    IFNULL(`io`.`flags`, 0) AS `custom_flags`,
    IFNULL(`io`.`item_vis_type`, 0) AS `item_vis_type`
  FROM
    `items` `i`
    LEFT JOIN 
      `items_duration` `id`
    ON
      `id`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_period` `ip`
    ON
      `ip`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_attributes` `ia`
    ON
      `ia`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_variation` `iv`
    ON
      `iv`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_options` `io`
    ON
      `io`.`item_id` = `i`.`item_id` -- iItemID
  WHERE `i`.`item_id` = iItemID LIMIT 1;
END $$


/* Load all item by owner_id */
DROP PROCEDURE IF EXISTS `lip_LoadItemsByOwner` $$
CREATE PROCEDURE `lip_LoadItemsByOwner` (IN iOwnerID INTEGER UNSIGNED)
 NOT DETERMINISTIC
 SQL SECURITY DEFINER
BEGIN
  SELECT SQL_NO_CACHE 
    `i`.`item_id` AS `item_id`,
    `i`.`owner_id` AS `owner_id`,
    `i`.`item_type` AS `item_type`,
    `i`.`amount` AS `amount`,
    `i`.`location` AS `location`,
    `i`.`slot` AS `slot`,
    `i`.`enchant` AS `enchant`,
    IFNULL(`id`.`duration`, -9999) AS `duration`,
    IFNULL(`ip`.`period`, -1) AS `period`,
    IFNULL(`ia`.`attack_type`, -2) AS `attack_attr_type`,
    IFNULL(`ia`.`attack_value`, 0) AS `attack_attr_val`,
    IFNULL(`ia`.`defence_fire`, 0) AS `defence_attr_fire`,
    IFNULL(`ia`.`defence_water`, 0) AS `defence_attr_water`,
    IFNULL(`ia`.`defence_wind`, 0) AS `defence_attr_wind`,
    IFNULL(`ia`.`defence_earth`, 0) AS `defence_attr_earth`,
    IFNULL(`ia`.`defence_holy`, 0) AS `defence_attr_holy`,
    IFNULL(`ia`.`defence_unholy`, 0) AS `defence_attr_unholy`,
    IFNULL(`iv`.`stat1`, 0) AS `variation_stat1`,
    IFNULL(`iv`.`stat2`, 0) AS `variation_stat2`,
    IFNULL(`io`.`blessed`, 0) AS `blessed`,
    IFNULL(`io`.`damaged`, 0) AS `damaged`,
    IFNULL(`io`.`energy`, 0) AS `item_energy`,
    IFNULL(`io`.`flags`, 0) AS `custom_flags`,
    IFNULL(`io`.`item_vis_type`, 0) AS `item_vis_type`
  FROM
    `items` `i`
    LEFT JOIN 
      `items_duration` `id`
    ON
      `id`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_period` `ip`
    ON
      `ip`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_attributes` `ia`
    ON
      `ia`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_variation` `iv`
    ON
      `iv`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_options` `io`
    ON
      `io`.`item_id` = `i`.`item_id` -- iItemID
  WHERE `i`.`owner_id` = iOwnerID;
END $$

/* Load all item by owner_id and loc */
DROP PROCEDURE IF EXISTS `lip_LoadItemsByOwnerAndLoc` $$
CREATE PROCEDURE `lip_LoadItemsByOwnerAndLoc` (IN iOwnerID INTEGER UNSIGNED, IN eLoc ENUM
            ('VOID',
             'INVENTORY',
             'PAPERDOLL',
             'WAREHOUSE',
             'FREIGHT',
             'CLANWH',
             'MAIL',
             'PET_INVENTORY',
             'PET_PAPERDOLL'))
 NOT DETERMINISTIC
 SQL SECURITY DEFINER
BEGIN
  SELECT SQL_NO_CACHE 
    `i`.`item_id` AS `item_id`,
    `i`.`owner_id` AS `owner_id`,
    `i`.`item_type` AS `item_type`,
    `i`.`amount` AS `amount`,
    `i`.`location` AS `location`,
    `i`.`slot` AS `slot`,
    `i`.`enchant` AS `enchant`,
    IFNULL(`id`.`duration`, -9999) AS `duration`,
    IFNULL(`ip`.`period`, -1) AS `period`,
    IFNULL(`ia`.`attack_type`, -2) AS `attack_attr_type`,
    IFNULL(`ia`.`attack_value`, 0) AS `attack_attr_val`,
    IFNULL(`ia`.`defence_fire`, 0) AS `defence_attr_fire`,
    IFNULL(`ia`.`defence_water`, 0) AS `defence_attr_water`,
    IFNULL(`ia`.`defence_wind`, 0) AS `defence_attr_wind`,
    IFNULL(`ia`.`defence_earth`, 0) AS `defence_attr_earth`,
    IFNULL(`ia`.`defence_holy`, 0) AS `defence_attr_holy`,
    IFNULL(`ia`.`defence_unholy`, 0) AS `defence_attr_unholy`,
    IFNULL(`iv`.`stat1`, 0) AS `variation_stat1`,
    IFNULL(`iv`.`stat2`, 0) AS `variation_stat2`,
    IFNULL(`io`.`blessed`, 0) AS `blessed`,
    IFNULL(`io`.`damaged`, 0) AS `damaged`,
    IFNULL(`io`.`energy`, 0) AS `item_energy`,
    IFNULL(`io`.`flags`, 0) AS `custom_flags`,
    IFNULL(`io`.`item_vis_type`, 0) AS `item_vis_type`
  FROM
    `items` `i`
    LEFT JOIN 
      `items_duration` `id`
    ON
      `id`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_period` `ip`
    ON
      `ip`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_attributes` `ia`
    ON
      `ia`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_variation` `iv`
    ON
      `iv`.`item_id` = `i`.`item_id` -- iItemID
    LEFT JOIN
      `items_options` `io`
    ON
      `io`.`item_id` = `i`.`item_id` -- iItemID
  WHERE `i`.`owner_id` = iOwnerID AND `i`.`location` = eLoc;
END $$


/* Delete item from db by item_id */
DROP PROCEDURE IF EXISTS `lip_DeleteItem` $$
CREATE PROCEDURE `lip_DeleteItem` (IN iItemID INTEGER UNSIGNED)
 NOT DETERMINISTIC
 SQL SECURITY DEFINER
BEGIN
  DELETE FROM `items` WHERE `items`.`item_id` = iItemID LIMIT 1;
  DELETE FROM `items_duration` WHERE `items_duration`.`item_id` = iItemID LIMIT 1;
  DELETE FROM `items_period` WHERE `items_period`.`item_id` = iItemID LIMIT 1;
  DELETE FROM `items_attributes` WHERE `items_attributes`.`item_id` = iItemID LIMIT 1;
  DELETE FROM `items_variation` WHERE `items_variation`.`item_id` = iItemID LIMIT 1;  
  DELETE FROM `items_options` WHERE `items_options`.`item_id` = iItemID LIMIT 1;
END $$

/* Save item */
DROP PROCEDURE IF EXISTS `lip_StoreItem` $$
CREATE PROCEDURE `lip_StoreItem` (IN iItemID INTEGER UNSIGNED,
                                 IN iOwnerID INTEGER UNSIGNED,
                                 IN iItemType MEDIUMINT UNSIGNED,
                                 IN iAmount BIGINT,
                                 IN iSlot INTEGER,
                                 IN eLoc ENUM('VOID','INVENTORY','PAPERDOLL','WAREHOUSE','FREIGHT','CLANWH','MAIL','PET_INVENTORY','PET_PAPERDOLL'),
                                 IN iEnchant SMALLINT UNSIGNED,
                                 IN iDuration INTEGER,
                                 IN iPeriod INTEGER,
                                 IN iAttackType TINYINT,
                                 IN iAttackVal SMALLINT,
                                 IN iDefFire SMALLINT,
                                 IN iDefWater SMALLINT,
                                 IN iDefWind SMALLINT,
                                 IN iDefEarth SMALLINT,
                                 IN iDefHoly SMALLINT,
                                 IN iDefUnholy SMALLINT,
                                 IN iVariStat1 SMALLINT UNSIGNED,
                                 IN iVariStat2 SMALLINT UNSIGNED,
                                 IN iBlessed INTEGER,
                                 IN iDamaged INTEGER,
                                 IN iItemEnergy INTEGER,
                                 IN iCustomFlags INTEGER UNSIGNED,
                                 IN iItemVisType MEDIUMINT UNSIGNED)
 NOT DETERMINISTIC
 SQL SECURITY DEFINER
entry: BEGIN
  DECLARE iRowCount INTEGER DEFAULT 0;
  IF (iAmount <= 0) OR (iDuration = 0) OR (iPeriod = 0) OR (iOwnerID = 0) THEN -- OR (eLoc = 'VOID')
    CALL `lip_DeleteItem`(iItemID);
    LEAVE entry;
  END IF;
  
  INSERT LOW_PRIORITY INTO `items` (
    `item_id`,
    `owner_id`,
    `item_type`,
    `amount`,
    `location`,
    `slot`,
    `enchant`
  ) VALUES (
    iItemID,
    iOwnerID,
    iItemType,
    iAmount,
    eLoc,
    iSlot,
    iEnchant
  ) ON DUPLICATE KEY UPDATE 
    `owner_id` = iOwnerID,
    `item_type` = iItemType,
    `amount` = iAmount,
    `location` = eLoc,
    `slot` = iSlot,
    `enchant` = iEnchant;
  
  SET iRowCount = ROW_COUNT();
  -- SELECT 'lip_StoreItem[303]', iRowCount;
  
  -- TODO: ROW_COUNT() bug ???
  CASE 
    WHEN iRowCount = 1 THEN -- insert new
      BEGIN
        IF iDuration > 0 THEN -- new item duration
          INSERT LOW_PRIORITY INTO `items_duration` (
            `item_id`,
            `duration`
          ) VALUES (
            iItemID,
            iDuration
          ) ON DUPLICATE KEY UPDATE
            `duration` = iDuration;
          -- SELECT 'lip_StoreItem[316]', iItemID, iDuration, ROW_COUNT();
        END IF;
        
        IF iPeriod > 0 THEN -- new item period
          INSERT LOW_PRIORITY INTO `items_period` (
            `item_id`,
            `period`
          ) VALUES (
            iItemID,
            iPeriod
          ) ON DUPLICATE KEY UPDATE
            `period` = iPeriod;
          -- SELECT 'lip_StoreItem[326]', iItemID, iPeriod, ROW_COUNT();
        END IF;
        
        IF ((iAttackType > -2) AND (iAttackVal > 0)) OR -- have attack or some defence in new item
           (iDefFire > 0) OR (iDefWater > 0) OR 
           (iDefWind > 0) OR (iDefEarth > 0) OR 
           (iDefHoly > 0) OR (iDefUnholy > 0) THEN
          INSERT LOW_PRIORITY INTO `items_attributes` (
            `item_id`,
            `attack_type`,
            `attack_value`,
            `defence_fire`,
            `defence_water`,
            `defence_wind`,
            `defence_earth`,
            `defence_holy`,
            `defence_unholy`
          ) VALUES (
            iItemID,
            iAttackType,
            iAttackVal,
            iDefFire,
            iDefWater,
            iDefWind,
            iDefEarth,
            iDefHoly,
            iDefUnholy
          ) ON DUPLICATE KEY UPDATE
            `attack_type` = iAttackType,
            `attack_value` = iAttackVal,
            `defence_fire` = iDefFire,
            `defence_water` = iDefWater,
            `defence_wind` = iDefWind,
            `defence_earth` = iDefEarth,
            `defence_holy` = iDefHoly,
            `defence_unholy` = iDefUnholy;

          -- SELECT 'lip_StoreItem[353]', iItemID,iAttackType,iAttackVal,iDefFire,iDefWater,iDefWind,iDefEarth,iDefHoly,iDefUnholy,ROW_COUNT();
        END IF;
      
        IF (iVariStat1 > 0) OR (iVariStat2 > 0) THEN -- have some variation(augumentation) in new item
          INSERT LOW_PRIORITY INTO `items_variation` (
            `item_id`,
            `stat1`,
            `stat2`
          ) VALUES (
            iItemID,
            iVariStat1,
            iVariStat2
          ) ON DUPLICATE KEY UPDATE
            `stat1` = iVariStat1,
            `stat2` = iVariStat2;
          -- SELECT 'lip_StoreItem[365]', iItemID, iVariStat1, iVariStat2, ROW_COUNT();
        END IF;
        
        IF (iBlessed > 0) OR (iDamaged > 0) OR (iItemEnergy > 0) OR (iCustomFlags > 0) OR (iItemVisType > 0) THEN -- have some rare or custom flag in item
          INSERT LOW_PRIORITY INTO `items_options` (
            `item_id`,
            `blessed`,
            `damaged`,
            `energy`,
            `flags`,
            `item_vis_type`
          ) VALUES (
            iItemID,
            iBlessed,
            iDamaged,
            iItemEnergy,
            iCustomFlags,
            iItemVisType
          ) ON DUPLICATE KEY UPDATE
            `blessed` = iBlessed,
            `damaged` = iDamaged,
            `energy` = iItemEnergy,
            `flags` = iCustomFlags,
            `item_vis_type` = iItemVisType;
          -- SELECT 'lip_StoreItem[381]', iItemID, iBlessed, iDamaged, iItemEnergy, iCustomFlags, ROW_COUNT();
        END IF;
      END;
    WHEN (iRowCount = 0) OR (iRowCount = 2) OR (iRowCount = 3) THEN -- not changed or updated or Bug#46675(or toad for mysql future?)
      BEGIN
        IF iDuration > 0 THEN
          UPDATE LOW_PRIORITY `items_duration` SET `duration` = iDuration WHERE `item_id` = iItemID LIMIT 1;
--           INSERT LOW_PRIORITY INTO `items_duration` (
--             `item_id`,
--             `duration`
--           ) VALUES (
--             iItemID,
--             iDuration
--           ) ON DUPLICATE KEY UPDATE 
--             `duration` = iDuration;
--        SELECT 'lip_StoreItem[395]', iItemID, iDuration, ROW_COUNT();
        END IF;
        
        IF iPeriod > 0 THEN
          UPDATE LOW_PRIORITY `items_period` SET `period` = iPeriod WHERE `item_id` = iItemID LIMIT 1;
--           INSERT LOW_PRIORITY INTO `items_period` (
--             `item_id`,
--             `period`
--           ) VALUES (
--             iItemID,
--             iPeriod
--           ) ON DUPLICATE KEY UPDATE 
--             `period` = iPeriod;
--        SELECT 'lip_StoreItem[407]', iItemID, iDuration, ROW_COUNT();
        END IF;
        
        IF ((iAttackType > -2) AND (iAttackVal > 0)) OR -- have attack or some defence
           (iDefFire > 0) OR (iDefWater > 0) OR 
           (iDefWind > 0) OR (iDefEarth > 0) OR 
           (iDefHoly > 0) OR (iDefUnholy > 0) THEN
          INSERT LOW_PRIORITY INTO `items_attributes` ( -- add new or update old
            `item_id`,
            `attack_type`,
            `attack_value`,
            `defence_fire`,
            `defence_water`,
            `defence_wind`,
            `defence_earth`,
            `defence_holy`,
            `defence_unholy`
          ) VALUES (
            iItemID,
            iAttackType,
            iAttackVal,
            iDefFire,
            iDefWater,
            iDefWind,
            iDefEarth,
            iDefHoly,
            iDefUnholy
          ) ON DUPLICATE KEY UPDATE 
            `attack_type` = iAttackType,
            `attack_value` = iAttackVal,
            `defence_fire` = iDefFire,
            `defence_water` = iDefWater,
            `defence_wind` = iDefWind,
            `defence_earth` = iDefEarth,
            `defence_holy` = iDefHoly,
            `defence_unholy` = iDefUnholy;
          -- SELECT 'lip_StoreItem[443]', iItemID,iAttackType,iAttackVal,iDefFire,iDefWater,iDefWind,iDefEarth,iDefHoly,iDefUnholy,ROW_COUNT();
        ELSE -- or delete (eg remove attribute)
          DELETE LOW_PRIORITY FROM `items_attributes` WHERE `item_id` = iItemID LIMIT 1;
          -- SELECT 'lip_StoreItem[447]', iItemID,iAttackType,iAttackVal,iDefFire,iDefWater,iDefWind,iDefEarth,iDefHoly,iDefUnholy,ROW_COUNT();
        END IF;
        
        
        IF (iVariStat1 > 0) OR (iVariStat2 > 0) THEN -- have some new variation(augumentation) in existing item
          INSERT INTO `items_variation` (
            `item_id`,
            `stat1`,
            `stat2`
          ) VALUES (
            iItemID,
            iVariStat1,
            iVariStat2
          ) ON DUPLICATE KEY UPDATE 
            `stat1` = iVariStat1,
            `stat2` = iVariStat2;
          -- SELECT 'lip_StoreItem[462]', iVariStat1, iVariStat2, ROW_COUNT();
        ELSE -- delete variation (variation was removed or newer exists)
          DELETE LOW_PRIORITY FROM `items_variation` WHERE `item_id` = iItemID LIMIT 1;
          -- SELECT 'lip_StoreItem[465]', iVariStat1, iVariStat2, ROW_COUNT();
        END IF;
        
        IF (iBlessed > 0) OR (iDamaged > 0) OR (iItemEnergy > 0) OR (iCustomFlags > 0) OR (iItemVisType > 0) THEN -- have some new rare or custom flag in item
          INSERT LOW_PRIORITY INTO `items_options` (
            `item_id`,
            `blessed`,
            `damaged`,
            `energy`,
            `flags`,
            `item_vis_type`
          ) VALUES (
            iItemID,
            iBlessed,
            iDamaged,
            iItemEnergy,
            iCustomFlags,
            iItemVisType
          ) ON DUPLICATE KEY UPDATE
            `blessed` = iBlessed,
            `damaged` = iDamaged,
            `energy` = iItemEnergy,
            `flags` = iCustomFlags,
            `item_vis_type` = iItemVisType;
          -- SELECT 'lip_StoreItem[484]', iItemID, iBlessed, iDamaged, iItemEnergy, iCustomFlags, ROW_COUNT();
        ELSE -- remove rare flags 
          DELETE LOW_PRIORITY FROM `items_options` WHERE `item_id` = iItemID LIMIT 1;
          -- SELECT 'lip_StoreItem[487]', iItemID, iBlessed, iDamaged, iItemEnergy, iCustomFlags, ROW_COUNT();
        END IF;
      END;
    ELSE
      BEGIN
        -- exception
        -- SELECT 'lip_StoreItem[384] exception', iRowCount;
        CALL `Unexpected ROW_COUNT() result in lip_StoreItem`;
      END;
  END CASE;
END $$

DELIMITER ;