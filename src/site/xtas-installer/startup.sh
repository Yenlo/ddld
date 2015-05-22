#!/bin/bash
#
# Yenlo Custom start script for DDLD Server - Philipp Gayret (philipp.gayret@yenlo.nl)
#
# Based on Myron's script for yclxsynpoc01
#
#
### BEGIN INIT INFO
# Provides:          DDLD
# Required-Start:
# Required-Stop:
# Default-Start:     3 5
# Default-Stop:      0 1 2 6
# Short-Description: Start all DDLD services
# Description:       Start all DDLD services for in case of a shutdown or restart
### END INIT INFO

#Setting variables
POCHOME=/u00
RUN_AS=pgayret
HOME=$POCHOME
PRG1="ElasticSearch"
PRG2="WildFly - jBoss AS 8"

#Testing Binaries
ELASTIC_BIN=$POCHOME/elasticsearch/bin/elasticsearch
test -x $ELASTIC_BIN || { echo "$ELASTIC_BIN not installed";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 5; fi; }

WILDFLY_BIN=$POCHOME/wildfly/bin/standalone.sh
test -x $WILDFLY_BIN || { echo "$WILDFLY_BIN not installed";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 5; fi; }

#help function
show_help () {
echo "Usage: $0 {start|stop|status|restart}"
}
########################################################################
#####################MAIN ROUTINE STARTS HERE###########################
########################################################################

case "$1" in
  start)
    then echo "starting $PRG1"
    start-stop-daemon --start -u $RUN_AS -x $ELASTIC_BIN > /dev/null &
    then echo "starting $PRG2"
    start-stop-daemon --start -u $RUN_AS -x $WILDFLY_BIN > /dev/null &
    echo "(both are java processes) "
    ;;
  stop)
    echo -"stopping $PRG1 and $PRG2"
    kill -9 `ps aux | grep -i "elasticsearch" | grep -v grep | awk '{print $2}'`
    kill -9 `ps aux | grep -i "wildfly" | grep -v grep | awk '{print $2}'`
    ;;
  restart)
    $0 stop
    sleep 10
    $0 start
    rc_status -v
    ;;
  status)
    echo "$PRG1 pid ="
    echo `ps aux | grep -i "elasticsearch" | grep -v grep | awk '{print $2}'`
    echo "$PRG2 pid = "
    echo `ps aux | grep -i "wildfly" | grep -v grep | awk '{print $2}'`
    ;;
  *)
    show_help
    exit 1
    ;;
esac
exit 0