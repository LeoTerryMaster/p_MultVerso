#!/bin/bash

while :;
do
	java -server -Dfile.encoding=UTF-8 -Xmx256m -cp config:./* l2.authserver.AuthServer > log/stdout.log 2>&1

	[ $? -ne 2 ] && break
	sleep 10;
done
