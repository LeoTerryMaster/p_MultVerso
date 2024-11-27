#!/bin/bash

while :;
do
  #This is config for backuping data in gameserver/backup (do not forgot make dir /backup) For launche remove " :: " and this comment (on russian too) ! Do not forget edit for access to database
  #Данная опция позаляет бекапить БД на рестарте. Для бекапа вы должны ! Удалить :: и все коменнтари и английские тоже . Так же создать /backup в корне сервера!  Не заьудьте так же указать логины пассы и т.д. для доступа к БД в строках ниже
  #mysqlcheck --host=127.0.0.1 --port=3306 --user=root --password=qwerty -s -r x10>>"log/`date +%Y-%m-%d_%H:%M:%S`-sql_check.log"
  #mysqldump --host=127.0.0.1 --port=3306 --user=root --password=qwerty x10 | gzip > "backup/`date +%Y-%m-%d_%H:%M:%S`-x10_gameserver.gz"
  #mysqlcheck --host=127.0.0.1 --port=3306 --user=root --password=qwerty -s -r x10>>"log/`date +%Y-%m-%d_%H:%M:%S`-sql_check.log"
  #mysqldump --host=127.0.0.1 --port=3306 --user=root --password=qwerty x10 | gzip > "backup/`date +%Y-%m-%d_%H:%M:%S`-x10_gameserver.gz"
	java -server -Dfile.encoding=UTF-8 -Xmx3G -cp config:./* l2.gameserver.GameServer > log/stdout.log 2>&1

	[ $? -ne 2 ] && break
	sleep 30;
done

