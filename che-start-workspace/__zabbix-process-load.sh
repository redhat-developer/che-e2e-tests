#!/bin/bash

source _setenv.sh

INPUT="$JOB_BASE_NAME-$BUILD_NUMBER"
ENDPOINT=${1-"GET","/api/workspace"}
METRIC_PREFIX=${2-auth-api-user}
if [ -z $ZABBIX_TIMESTAMP ]; then
  export ZABBIX_TIMESTAMP=`date +%s`;
fi

VALUES=(`cat $LOG_DIR/csv/$INPUT-report_requests.csv | grep -F "$ENDPOINT" | cut -d ',' -f 3-10 | tr ',' ' '`)
VAL_REQ=$((${VALUES[0]}+${VALUES[1]}))
VAL_FAIL=${VALUES[1]}
VAL_FAIL_RATE=`echo "100*$VAL_FAIL/$VAL_REQ" | bc -l`
VAL_MED=${VALUES[2]}
VAL_AVG=${VALUES[3]}
VAL_MIN=${VALUES[4]}
VAL_MAX=${VALUES[5]}

echo "$ZABBIX_HOST che_$METRIC_PREFIX-failed $ZABBIX_TIMESTAMP $VAL_FAIL"
echo "$ZABBIX_HOST che_$METRIC_PREFIX-fail_rate $ZABBIX_TIMESTAMP $VAL_FAIL_RATE"
echo "$ZABBIX_HOST che_$METRIC_PREFIX-average $ZABBIX_TIMESTAMP $VAL_AVG"
echo "$ZABBIX_HOST che_$METRIC_PREFIX-median $ZABBIX_TIMESTAMP $VAL_MED"
echo "$ZABBIX_HOST che_$METRIC_PREFIX-min $ZABBIX_TIMESTAMP $VAL_MIN"
echo "$ZABBIX_HOST che_$METRIC_PREFIX-max $ZABBIX_TIMESTAMP $VAL_MAX"

DISTR=(`cat $LOG_DIR/csv/$INPUT-report_distribution.csv | sed -e 's/ /","/' | grep -F "$ENDPOINT" | tr ',' ' '`)
