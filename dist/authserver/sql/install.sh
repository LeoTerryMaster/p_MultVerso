#!/bin/sh

if [ -f ./mysql_settings.sh ]; then
        . ./mysql_settings.sh
else
        echo "Can't find mysql_settings.sh file!"
        exit
fi

for sqlfile in install/*.sql
do
        echo Loading $sqlfile to " $DBNAME " ...
        mysql --host=$DBHOST --user=$USER --password=$PASS $DBNAME < $sqlfile
done
