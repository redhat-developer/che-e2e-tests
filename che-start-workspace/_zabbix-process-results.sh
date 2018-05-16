#!/bin/bash

source _setenv.sh

export ZABBIX_LOG=$1
export ZABBIX_TIMESTAMP=`date +%s`

./__zabbix-process-load.sh '"POST","createWorkspace"' "createWorkspace" >> $ZABBIX_LOG
./__zabbix-process-load.sh '"DELETE","deleteWorkspace"' "deleteWorkspace" >> $ZABBIX_LOG
./__zabbix-process-load.sh '"GET","getWorkspaceStatus"' "getWorkspaceStatus" >> $ZABBIX_LOG
./__zabbix-process-load.sh '"POST","startWorkspace"' "startWorkspace" >> $ZABBIX_LOG
./__zabbix-process-load.sh '"DELETE","stopWorkspace"' "stopWorkspace" >> $ZABBIX_LOG
./__zabbix-process-load.sh '"REPEATED_GET","timeForStartingWorkspace"' "timeForStartingWorkspace" >> $ZABBIX_LOG
./__zabbix-process-load.sh '"REPEATED_GET","timeForStoppingWorkspace"' "timeForStoppingWorkspace" >> $ZABBIX_LOG
./__zabbix-process-load.sh '"REPEATED_GET","timeForRemovingPod"' "timeForRemovingPod" >> $ZABBIX_LOG