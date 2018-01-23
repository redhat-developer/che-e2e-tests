# Soak test for che.
Right now, there is only one scenario, which runs by default only once.

1) Create workspace
2) Start that workspace
3) Wait for it to be running
4) Delete that workspace
5) Repeat from step 1)

If you want to run it multiple times, edit src/test/resources/scenarios/create-start-delete.xml. Search for tag "run" and edit it's  value.

# How to run

1) Fill user tokens into src/test/resources/users.properties
3) Edit src/test/resources/scenarios/create-start-delete.xml to have as much threads as you have users in users.properties.
2) `mvn clean install perfcake:scenario-run -DcheStarterHost=<cheStarterHost>`

That's it.

## Properties

_cheStarterHost_ - URL of che starter. For local cheStarter it is `http://localhost:10000`

_token_ - User token obtained from openshift.io

