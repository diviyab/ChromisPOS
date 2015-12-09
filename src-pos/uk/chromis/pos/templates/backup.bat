@echo off
echo Backing up Lemon...
FOR /F "TOKENS=1-4 DELIMS=/ " %%I IN ('DATE /T') DO (SET MYDATE=%%I%%J%%K%%L)
SET mySQLfile=lemon.%MYDATE%.sql
"C:\Project\Delivery\mysql Server 5.0\bin\mysqldump" -uroot -proot --result-file="%mySQLfile%" openbravopos
echo ---
echo Backup Complete ;-)
echo ---
@ping 127.0.0.1 -n 5 -w 1000 > nul
exit