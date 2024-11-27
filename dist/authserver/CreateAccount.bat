@echo off
title Create account
set /p user="Enter account name: "
set /p pass="Enter account pass: "
java -Dfile.encoding=UTF-8 -Xms1024m -Xmx1024m -cp config;./* l2.authserver.AccountManager -c %user% %pass%
pause
exit
