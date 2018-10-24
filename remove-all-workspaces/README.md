# Scripts for cleaning workspaces 

In this package you can find scripts that are used for daily cleaning of users workspaces. 

## get_active_tokens.sh

This script require file ```users.properties``` which contains data about user accounts which should be deleted. The file needs to be placed in the same folder as scripts.
The file requires one input parameter which determines output file, where obtained tokens should be saved. Example of calling the script:
```
./get_active_tokens.sh tokens.txt
```

### example of users.properties file

This file should contain information about user accounts such as username, password and information about cluster, where the user is provided. The cluster should be "prod" or "prod-preview".
Each user should start on a new line and individual information about user should be divided by ";". 

```
username1;password;prod
nextUser;123pass;prod-preview
```

### example of tokens file

The name of file with tokens is chosen by user and passed as a parameter. The file should contains some addition information as a username and cluster of a user. Each information should
be divided by ";" and each user should start on a new line - as shown in an example below. 

```
usertoken;username1;prod
anothertoken;nextUser;prod-preview
```

## remove_workspaces.sh

This script removes all workspaces from user account. If some of workspaces are running then it will stop before removal. This script requires one input parameter which determines
file with active tokens in format described above. Example of calling the script:
```
./remove_workspaces.sh tokens.txt
```
