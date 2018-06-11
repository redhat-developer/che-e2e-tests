LOCUST_FILE=${2:-osioperf.py}

echo "Creating locustfile template $LOCUST_FILE"
cp -rvf ${1:-$JOB_BASE_NAME.py} $LOCUST_FILE

echo "Filtering $LOCUST_FILE"
sed -i -e "s,@@SERVER_HOST@@,$SERVER_HOST,g" $LOCUST_FILE;
sed -i -e "s,@@JOB_BASE_NAME@@,$JOB_BASE_NAME,g" $LOCUST_FILE;
sed -i -e "s,@@BUILD_NUMBER@@,$BUILD_NUMBER,g" $LOCUST_FILE;
