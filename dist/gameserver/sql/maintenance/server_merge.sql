-- Инструкция к скрипту по объединению серверов - заметьте ОБЪЕДИНЕНИЮ, не переноса данных от базы 1 к базе 2:
-- Перенос вы можете сделать самым простым образом - бекап !
-- ATTENTION/ВНИМАНИЕ
-- ВЫ ОБЯЗАНЫ СДЕЛАТЬ ПЕРЕД ВЫПОЛНЕНИЕМ ДАННОГО SQL файла РЕЗЕРВНОЕ КОПИРОВАНИЕ ОБЕИХ БАЗ.
-- ЕСЛИ ВЫ ЭТОГО НЕ СДЕЛАЕТЕ И ВОЗНИКНУТ ЛЮБАЫЕ ОШИБКИ В ПРОЦЕССЕ ВЫПОЛНЕНИЯ. ВЫ ЛИШИТЕСЬ ОБЕИХ БАЗ

-- и так поехали, теперь мы должны указать имена баз из которой объединяем данные: 
-- в примере это src_db и куда, в перемер это dst_db т.е. данные src_db > dst_db. 

-- Если у вас база логина называется по другому - временно перенесите в одну базу. Таблица accounts.
-- Логины так же нужно делать CONCAT из за того что на аккаунте может быть к примеру 7 персонажей. 
-- Перенесенный аккаунт будет иметь все те же данные, но уже с приставкой XL. Саму приставку вы можете менять как вам угодно. 
-- У меня в примере CONCAT(login,'XL')

-- EN
-- Guide to the script on the federated server - note the association, not the transfer of data from base 1 to base 2:
-- Transfer you can make the most simple way - a backup!
-- ATTENTION / WARNING
-- YOU MUST DO THIS BEFORE SQL backup files both databases.
-- IF you do not HAVE backups AND ANY ERRORS IN THE COURSE OF PERFORMANCE. You lose both databases

-- so let's go, now we have to specify the names of the databases that combine data:
-- main db is dst_db, import db is lucera2_from_db (You mast change databases in example on your db name)

-- If you login database called differently - temporarily move into one database. accounts table.
-- Logins as CONCAT to do due to the fact that your account may be for example 7 characters.
-- Transferred Account will have all the same data, but with the prefix XL. Top box, you can change as you like.
-- In the example, CONCAT (login, 'XL')

-- make table with current objectId's situation from main database

DROP TABLE IF EXISTS dst_db.objectIdx;
CREATE TABLE dst_db.objectIdx (
    object_id int NOT NULL,
    PRIMARY KEY (object_id)
);

drop table if exists src_db.`acc_conv`;
create table src_db.`acc_conv` (
  `src_login` VARCHAR(32) NOT NULL,
  `dst_login` VARCHAR(32) NOT NULL,
  primary key(`src_login`),
  unique key(`dst_login`)
) engine=InnoDB default charset=utf8;



INSERT INTO dst_db.objectIdx (object_id)
SELECT obj_id from dst_db.characters;

INSERT INTO dst_db.objectIdx (object_id)
SELECT item_id from dst_db.items;

INSERT INTO dst_db.objectIdx (object_id)
SELECT clan_id from dst_db.clan_data;

INSERT INTO dst_db.objectIdx (object_id)
SELECT ally_id from dst_db.ally_data;

INSERT INTO dst_db.objectIdx (object_id)
SELECT objId from dst_db.pets;

-- make table with current objectId's situation from imported database
DROP TABLE IF EXISTS src_db.objectIdx;
CREATE TABLE src_db.objectIdx (
shortIdx int NOT NULL auto_increment,
object_id int NOT NULL,
PRIMARY KEY  (shortIdx),
KEY `object_id` (`object_id`)
);


INSERT INTO src_db.objectIdx (object_id)
SELECT obj_id FROM src_db.characters;
INSERT INTO src_db.objectIdx (object_id)
SELECT item_id FROM src_db.items;
INSERT INTO src_db.objectIdx (object_id)
SELECT clan_id FROM src_db.clan_data;
INSERT INTO src_db.objectIdx (object_id)
SELECT ally_id FROM src_db.ally_data;
INSERT INTO src_db.objectIdx (object_id)
SELECT objId FROM src_db.pets;

-- make table with oldId/newId for imported database
DROP TABLE IF EXISTS src_db.exportIdx;
CREATE TABLE src_db.exportIdx (
old_id int NOT NULL,
new_id int NOT NULL,
PRIMARY KEY  (old_id),
KEY `new_id` (`new_id`)
);
INSERT INTO src_db.exportIdx
SELECT object_id,shortIdx+(SELECT max(object_id) FROM dst_db.objectIdx)
from src_db.objectIdx;

-- okeys, lets do change old_id on new_id
-- main ids change
UPDATE src_db.characters a,src_db.exportIdx b 
SET a.obj_id = b.new_id where a.obj_id = b.old_id; 

UPDATE src_db.items a,src_db.exportIdx b
SET a.item_id = b.new_id where a.item_id = b.old_id;

UPDATE src_db.clan_data a,src_db.exportIdx b
SET a.clan_id = b.new_id where a.clan_id = b.old_id;

UPDATE src_db.ally_data a,src_db.exportIdx b
SET a.ally_id = b.new_id where a.ally_id = b.old_id;

UPDATE src_db.pets a,src_db.exportIdx b
SET a.objId = b.new_id where a.objId = b.old_id;
-- secondary ids change
UPDATE src_db.items a,src_db.exportIdx b
SET a.owner_id = b.new_id
WHERE a.owner_id= b.old_id;

UPDATE src_db.items_duration a,src_db.exportIdx b
SET a.item_id = b.new_id
WHERE a.item_id= b.old_id;

UPDATE src_db.items_options a,src_db.exportIdx b
SET a.item_id = b.new_id
WHERE a.item_id= b.old_id;

UPDATE src_db.items_period a,src_db.exportIdx b
SET a.item_id = b.new_id
WHERE a.item_id= b.old_id;

UPDATE src_db.items_variation a,src_db.exportIdx b
SET a.item_id = b.new_id
WHERE a.item_id= b.old_id;

UPDATE src_db.character_quests a,src_db.exportIdx b
SET a.char_id = b.new_id
WHERE a.char_id = b.old_id;

UPDATE src_db.character_friends a,src_db.exportIdx b
SET a.char_id = b.new_id
WHERE a.char_id = b.old_id;

UPDATE src_db.character_hennas a,src_db.exportIdx b
SET a.char_obj_id = b.new_id
WHERE a.char_obj_id = b.old_id;

UPDATE src_db.character_recipebook a,src_db.exportIdx b
SET a.char_id = b.new_id
WHERE a.char_id = b.old_id;

UPDATE src_db.character_shortcuts a,src_db.exportIdx b
SET a.object_id = b.new_id
WHERE a.object_id = b.old_id;

UPDATE src_db.character_shortcuts a,src_db.exportIdx b
SET a.shortcut_id = b.new_id
WHERE a.shortcut_id = b.old_id AND type = 1;

UPDATE src_db.character_macroses a,src_db.exportIdx b
SET a.char_obj_id = b.new_id
WHERE a.char_obj_id = b.old_id;

UPDATE src_db.character_skills a,src_db.exportIdx b
SET a.char_obj_id = b.new_id
WHERE a.char_obj_id = b.old_id;

UPDATE src_db.character_skills_save a,src_db.exportIdx b
SET a.char_obj_id = b.new_id
WHERE a.char_obj_id = b.old_id;

UPDATE src_db.character_subclasses a,src_db.exportIdx b
SET a.char_obj_id = b.new_id
WHERE a.char_obj_id = b.old_id;

UPDATE src_db.character_variables a,src_db.exportIdx b
SET a.obj_id = b.new_id
WHERE a.obj_id = b.old_id;

UPDATE src_db.characters a,src_db.exportIdx b
SET a.clanid = b.new_id
WHERE a.clanid = b.old_id;

UPDATE src_db.siege_clans a,src_db.exportIdx b
SET a.clan_id = b.new_id
WHERE a.clan_id = b.old_id;

UPDATE src_db.clan_data a,src_db.exportIdx b
SET a.ally_id = b.new_id
WHERE a.ally_id = b.old_id;

UPDATE src_db.clan_subpledges a,src_db.exportIdx b
SET a.clan_id = b.new_id
WHERE a.clan_id = b.old_id;

UPDATE src_db.clan_privs a,src_db.exportIdx b
SET a.clan_id = b.new_id
WHERE a.clan_id = b.old_id;

UPDATE src_db.clan_skills a,src_db.exportIdx b
SET a.clan_id = b.new_id
WHERE a.clan_id = b.old_id;

UPDATE src_db.pets a,src_db.exportIdx b
SET a.item_obj_id = b.new_id
WHERE a.item_obj_id = b.old_id;

insert into src_db.acc_conv (src_login, dst_login) select src_db.characters.account_name, src_db.characters.account_name from src_db.characters
group by src_db.characters.account_name
;

delimiter $$
drop FUNCTION if exists `lip_ex_TrunLoginWithPfx` $$
CREATE DEFINER = CURRENT_USER FUNCTION `lip_ex_TrunLoginWithPfx` (`sSrcLogin` varchar(32)) RETURNS varchar(32)
  NOT DETERMINISTIC
  SQL SECURITY DEFINER
entry: BEGIN
  declare iIdx int default 0;
  declare iCnt int default 0;
	declare sLogin varchar(32) default 0;
  set sLogin = sSrcLogin;

  REPEAT
    set iIdx = iIdx + 1;
    if CHAR_LENGTH(sSrcLogin) < 11 THEN
      select trim(concat(sSrcLogin, '_', iIdx)) into sLogin;
    else
      select trim(concat(LEFT(sSrcLogin, char_length(sSrcLogin) - 3), '_', iIdx)) into sLogin;
    end if;
    select count(dst_login) into iCnt from acc_conv where acc_conv.dst_login = sLogin;
  UNTIL (iCnt < 1) END REPEAT;

	RETURN sLogin;
END $$
delimiter ;

update src_db.acc_conv set src_db.acc_conv.dst_login = lip_ex_TrunLoginWithPfx(src_db.acc_conv.src_login);

SELECT '-- check login (in characters) ---';
UPDATE src_db.characters, src_db.acc_conv
SET src_db.characters.account_name = src_db.acc_conv.dst_login WHERE src_db.characters.account_name = src_db.acc_conv.src_login;



SELECT '-- check char_name ---';
UPDATE src_db.characters
SET char_name = CONCAT(char_name,'XL')
WHERE char_name in (SELECT char_name FROM dst_db.characters);

SELECT '-- check clan_name ---';
UPDATE src_db.clan_subpledges
SET name = CONCAT(name,'XL')
WHERE name in (SELECT name FROM dst_db.clan_subpledges);

SELECT '-- check ally_name ---';
UPDATE src_db.ally_data
SET ally_name = CONCAT(ally_name,'XL')
WHERE ally_name in (SELECT ally_name FROM dst_db.ally_data);

-- now may start merge
-- main tables
SELECT 'Start merge process.';
-- INSERT INTO dst_db.accounts SELECT * FROM src_db.accounts;

insert into dst_db.accounts(login, `password`, email) 
select src_db.acc_conv.dst_login, x.password, x.email
from src_db.acc_conv, src_db.accounts x where x.login = src_db.acc_conv.src_login;

SELECT 'accounts ok.';
INSERT INTO dst_db.characters SELECT * FROM src_db.characters; 
SELECT 'characters ok.';
INSERT INTO dst_db.items SELECT * FROM src_db.items; 
SELECT 'items ok.';
INSERT INTO dst_db.clan_data SELECT * FROM src_db.clan_data; 
SELECT 'clan_data ok.';
INSERT INTO dst_db.ally_data SELECT * FROM src_db.ally_data; 
SELECT 'ally_data ok.';
INSERT INTO dst_db.pets SELECT * FROM src_db.pets; 
SELECT 'pets ok.';
-- secondary tables
INSERT INTO dst_db.character_quests SELECT * FROM src_db.character_quests;
SELECT 'character_quests ok.';
INSERT INTO dst_db.character_friends SELECT * FROM src_db.character_friends;
SELECT 'character_friends ok.';
INSERT INTO dst_db.character_hennas SELECT * FROM src_db.character_hennas;
SELECT 'character_hennas ok.';
INSERT INTO dst_db.character_recipebook SELECT * FROM src_db.character_recipebook;
SELECT 'character_recipebook ok.';
INSERT INTO dst_db.character_shortcuts SELECT * FROM src_db.character_shortcuts;
SELECT 'character_shortcuts ok.';
INSERT INTO dst_db.character_macroses SELECT * FROM src_db.character_macroses;
SELECT 'character_macroses ok.';
INSERT INTO dst_db.character_skills SELECT * FROM src_db.character_skills;
SELECT 'character_skills ok.';
INSERT INTO dst_db.character_skills_save SELECT * FROM src_db.character_skills_save;
SELECT 'character_skills_save ok.';
INSERT INTO dst_db.character_subclasses SELECT * FROM src_db.character_subclasses;
SELECT 'character_subclasses ok.';
INSERT INTO dst_db.character_variables SELECT * FROM src_db.character_variables;
SELECT 'character_variables ok.';
INSERT INTO dst_db.items_duration SELECT * FROM src_db.items_duration;
SELECT 'items_duration ok.';
INSERT INTO dst_db.items_options SELECT * FROM src_db.items_options;
SELECT 'items_options ok.';
INSERT INTO dst_db.items_period SELECT * FROM src_db.items_period;
SELECT 'items_period ok.';
INSERT INTO dst_db.items_variation SELECT * FROM src_db.items_variation;
SELECT 'items_variation ok.';
INSERT INTO dst_db.siege_clans SELECT * FROM src_db.siege_clans;
SELECT 'siege_clans ok.';
INSERT INTO dst_db.clan_subpledges SELECT * FROM src_db.clan_subpledges; 
SELECT 'clan_subpledges ok.';
INSERT INTO dst_db.clan_privs SELECT * FROM src_db.clan_privs; 
SELECT 'clan_privs ok.';
INSERT INTO dst_db.clan_skills SELECT * FROM src_db.clan_skills; 
SELECT 'clan_skills ok.';
drop FUNCTION if exists `lip_ex_TrunLoginWithPfx`;

