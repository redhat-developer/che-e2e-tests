#osioUrlPart should be "openshift.io" or "prod-preview.openshift.io"
#cheWorkspaceName is name displayed in dashboard - not and ID!
mvn clean install -f pom.xml -DskipTests
mvn clean verify -f tests/pom.xml -Dtest=E2ETestSuite -DosioUrlPart=$1 -DosioUsername=$2 -DosioPassword=$3 -DcheWorkspaceName=$4 -DkeycloakToken=$5 -DpreserveWorkspace=true

exit $?