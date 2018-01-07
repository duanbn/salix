#!/bin/sh

basedir=`dirname $0`
basedir=`cd $basedir/..; pwd;`

libdir=$basedir/lib
configdir=$basedir/conf

classpath=$classpath:$libdir/*
#load config file
classpath=$classpath:$configdir

main=com.salix.server.Shutdown
shutdown_port="xxxx"
args="$shutdown_port"

exec java -cp $classpath $main $args >>$basedir/logs/console.log 2>&1 &
