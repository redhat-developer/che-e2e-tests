def ACTIVE_USER_TOKENS_RAW
def RELATIVE_PATH = "che-start-workspace"
def PROPERTIES_FILE = "users.properties"
def EXPORT_FILE = "exports"
def TOKENS_FILE = "tokens.txt"
def CHE_SERVER_URL = "https://che.prod-preview.openshift.io"

pipeline {
    agent { label 'osioperf-master1' }
    environment {
        USERS_PROPERTIES_FILE = credentials('${USERS_PROPERTIES_FILE_ID}')
        USER_TOKENS = ""
        LOG_DIR = ""
        ZABBIX_FILE = ""
    }
    options {
        timeout(time: "${PIPELINE_TIMEOUT}", unit: 'MINUTES') 
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
                            continue
                        }
                        def name_host_metric = elements[1].replace("\"","").split("_")
                        def name = name_host_metric[0]
                        if (name.equals("getWorkspaces") | name.equals("getWorkspaceStatus")) {
                            continue
                        }
                        def host = name_host_metric[1]
                        def int average = elements[5]
                        def output_basestring = "qa-".concat(host).concat(" ")
                                                .concat("che-start-workspace.").concat(method).concat(".")
                                                .concat(name).concat(".eph")
                        def output = output_basestring.concat(" ")
                                     .concat(String.valueOf(DATETIME_TAG)).concat(" ")
                                     .concat(String.valueOf(average))
                        sh (script: """
                        #!/bin/bash
                        set +x;
                        echo $output >> ${ZABBIX_FILE}
                        set -x;
                        """, returnStdout: false)
                    }
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
            for (user in USER_TOKENS) {
                def user_array = user.split(";")
                def active_token = user_array[0]
                def username = user_array[1]
                def environment = user_array[2]
                def reset_api_url = "https://api.openshift.io/api/user/services"
                if (environment.equals("prod-preview")) {
                    reset_api_url = "https://api.prod-preview.openshift.io/api/user/services"
                }
                sh "curl -s -X DELETE --header 'Content-Type: application/json' --header 'Authorization: Bearer ${active_token}' ${reset_api_url}"
                sh "curl -s -X PATCH --header 'Content-Type: application/json' --header 'Authorization: Bearer ${active_token}' ${reset_api_url}"
            }
        }
    }
}