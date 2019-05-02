def ACTIVE_USER_TOKENS_RAW
def RELATIVE_PATH = "che-start-workspace"
def PROPERTIES_FILE = "users.properties"
def EXPORT_FILE = "exports"
def TOKENS_FILE = "tokens.txt"
def CHE_SERVER_URL = "https://che.prod-preview.openshift.io"

pipeline {
    agent { label 'osioperf-master3' }
    environment {
        USERS_PROPERTIES_FILE = credentials('${USERS_PROPERTIES_FILE_ID}')
        USER_TOKENS = ""
        LOG_DIR = ""
        ZABBIX_FILE = ""
        // CYCLES_COUNT = 5
    }
    stages {
        stage ("Prepairing environment") {
            steps {
                echo ("Pulling git repositories")
                    checkout([$class: 'GitSCM', 
                        branches: [[name: '*/467-feature-fix-workspace-startup-monitoring-jobs']], 
                        doGenerateSubmoduleConfigurations: false, 
                        extensions: [], 
                        submoduleCfg: [], 
                        userRemoteConfigs: [[url: 'https://www.github.com/ScrewTSW/che-functional-tests.git']]
                    ])
                echo ("Getting user active tokens")
                    sh "./${RELATIVE_PATH}/get_active_tokens.sh \"${TOKENS_FILE}\""
                    script {
                        USER_TOKENS = sh(returnStdout:true, script:"cat ${TOKENS_FILE}").trim()
                    }
            }
        }
        stage ("Running worksapce test") {
            steps {
                script {
                    LOG_DIR = sh(returnStdout:true, script:"echo ${JOB_BASE_NAME}-${BUILD_NUMBER}").trim()
                    ZABBIX_FILE = "${LOG_DIR}/${JOB_BASE_NAME}-${BUILD_NUMBER}-zabbix.log"
                    echo ("Creating logs directory: ${LOG_DIR}")
                }
                dir ("${LOG_DIR}") {
                    sh """
                    set +x
                    export USER_TOKENS="$USER_TOKENS"
                    export CYCLES_COUNT="$CYCLES_COUNT"
                    export CHE_STACK_FILE="../${RELATIVE_PATH}/che7_ephemeral.json"
                    locust -f "../${RELATIVE_PATH}/osioperf.py" --no-web -c `echo -e "$USER_TOKENS" | wc -l` -r 1 --only-summary --csv="$EXPORT_FILE"
                    set -x
                    """
                }
            }
        }
        stage ("Generating zabbix report") {
            steps {
                script {
                    def long DATETIME_TAG = System.currentTimeMillis() / 1000L
                    def test_log_file = readFile("${LOG_DIR}/${EXPORT_FILE}_requests.csv")
                    def BASE_DIR = pwd()
                    def lines = test_log_file.split("\n")
                    sh "touch ${ZABBIX_FILE}"
                    for (line in lines) {
                        def elements = line.split(",")
                        def method = elements[0].replace("\"","")
                        if (method.equals("Method") || method.equals("None")) {
                            echo "Skipping line, no data."
                            continue
                        }
                        def name_host_metric = elements[1].replace("\"","").split("_")
                        def name = name_host_metric[0]
                        if (name.equals("getWorkspaces")) {
                            echo "Skipping line, not a metric, part of another method"
                            continue
                        }
                        def host = name_host_metric[1]
                        def int requests_count = elements[2]
                        def int failures = elements[3]
                        def int success = requests_count-failures
                        def float fail_rate = (failures == 0) ? 0 : requests_count/failures*100
                        def int median = elements[4]
                        def int average = elements[5]
                        def int minimum = elements[6]
                        def int maximum = elements[7]
                        def int avg_content_size = elements[8]
                        def float requests_per_second = elements[9]
                        def output_basestring = "qa-".concat(host).concat(" ").concat("che-start-workspace.").concat(method).concat(".").concat(name)
                        def output_failed = output_basestring.concat("-failed").concat(" ")
                                            .concat(String.valueOf(DATETIME_TAG)).concat(" ")
                                            .concat(String.valueOf(failures))
                        def output_failrate = output_basestring.concat("-fail_rate").concat(" ")
                                              .concat(String.valueOf(DATETIME_TAG)).concat(" ")
                                              .concat(String.valueOf(fail_rate))
                        def output_average = output_basestring.concat("-rt_average").concat(" ")
                                            .concat(String.valueOf(DATETIME_TAG)).concat(" ")
                                            .concat(String.valueOf(average))
                        def output_median = output_basestring.concat("-rt_median").concat(" ")
                                            .concat(String.valueOf(DATETIME_TAG)).concat(" ")
                                            .concat(String.valueOf(median))
                        def output_min = output_basestring.concat("-rt_min").concat(" ")
                                         .concat(String.valueOf(DATETIME_TAG)).concat(" ")
                                         .concat(String.valueOf(minimum))
                        def output_max = output_basestring.concat("-rt_max").concat(" ")
                                         .concat(String.valueOf(DATETIME_TAG)).concat(" ")
                                         .concat(String.valueOf(maximum))
                        sh (script: """
                        #!/bin/bash
                        set +x;
                        echo $output_failed >> ${ZABBIX_FILE}
                        echo $output_failrate >> ${ZABBIX_FILE}
                        echo $output_average >> ${ZABBIX_FILE}
                        echo $output_median >> ${ZABBIX_FILE}
                        echo $output_min >> ${ZABBIX_FILE}
                        echo $output_max >> ${ZABBIX_FILE}
                        set -x;
                        """, returnStdout: false)
                    }
                    echo "Writing zabbix report done."
                    echo readFile("${ZABBIX_FILE}")
                }
            }
        }
        stage ("Reporting to zabbix") {
            steps {
                sh "zabbix_sender -vv -i ${ZABBIX_FILE} -T -z ${ZABBIX_SERVER} -p ${ZABBIX_PORT}"
            }
        }
    }
    post("Cleanup") {
        always {
            deleteDir()
        }
        failure {
            // mail to: team@example.com, subject: 'The Pipeline failed :('
            echo "NOP"
        }
    }
}