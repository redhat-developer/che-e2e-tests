#!/bin/bash

source ./_setenv.sh

export LOG_DIR=$JOB_BASE_NAME-$BUILD_NUMBER
mkdir $LOG_DIR

export COMMON="common.git"
git clone https://github.com/pmacik/openshiftio-performance-common $COMMON

source $COMMON/config/_setenv.sh
WORKSPACE=$(pwd)

echo " Wait for the server to become available"
./_wait-for-server.sh
if [ $? -gt 0 ]; then
	exit 1
fi

echo " Login users and get auth tokens"
LOGIN_USERS=openshift-loginusers.git
git clone https://github.com/pmacik/openshiftio-loginusers $LOGIN_USERS

mvn -f $LOGIN_USERS/java/pom.xml clean compile
cat $USERS_PROPERTIES_FILE > $LOGIN_USERS/java/target/classes/users.properties
TOKENS_FILE=users.tokens

echo "  OAuth2 friendly login..."

MVN_LOG=$LOG_DIR/$JOB_BASE_NAME-$BUILD_NUMBER-oauth2-mvn.log
mvn -f $LOGIN_USERS/java/pom.xml -l $MVN_LOG exec:java -Dmax.users=$USERS -Dauth.server.address=$AUTH_SERVER_URL -Duser.tokens.file=$TOKENS_FILE -Poauth2 -Duser.tokens.include.username=true
LOGIN_USERS_OAUTH2_LOG=$LOG_DIR/$JOB_BASE_NAME-$BUILD_NUMBER-login-users-oauth2.log

cat $MVN_LOG | grep login-users-log > $LOGIN_USERS_OAUTH2_LOG
chmod +r $TOKENS_FILE

endtime=$(date -d "+$DURATION seconds" +%X)
echo " Tests will end approximately at $endtime"

$COMMON/_execute.sh

echo "Removing all workspaces from accounts"
./removeWorkspaces.sh $TOKENS_FILE

while read p; do
  echo $p | cut -d';' -f 3 >> $LOG_DIR/$JOB_BASE_NAME-$BUILD_NUMBER-locust-master.log
done <$TOKENS_FILE

 echo "Check for errors in Locust master log"

 REPORT_COUNT=`wc -l < $LOG_DIR/csv/$JOB_BASE_NAME-$BUILD_NUMBER-report_distribution.csv`
 EXPECTED_REPORT_COUNT=9
 EXIT_CODE=0
 if [[ "0" -ne `cat $LOG_DIR/$JOB_BASE_NAME-$BUILD_NUMBER-locust-master.log | grep 'Error report' | wc -l` ]]; then
    echo 'THERE WERE ERRORS OR FAILURES WHILE SENDING REQUESTS';
    EXIT_CODE=1;
 elif [[ REPORT_COUNT -ne $EXPECTED_REPORT_COUNT ]]; then
    echo "THERE WERE NOT CORRECT AMOUNT OF RECORDS IN REPORT FILE expected $EXPECTED_REPORT_COUNT gotten $REPORT_COUNT";
    EXIT_CODE=1;
 else
    echo 'NO ERRORS OR FAILURES DETECTED';
 fi

 echo "Artifacts: https://osioperf-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/job/$JOB_BASE_NAME/$BUILD_NUMBER/artifact/che-start-workspace/$JOB_BASE_NAME-$BUILD_NUMBER/"


 exit $EXIT_CODE


