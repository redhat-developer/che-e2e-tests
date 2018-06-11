#!/bin/bash

export TOKENS_FILE=$WORKSPACE/users.tokens

if [ "$RUN_LOCALLY" != "true" ]; then
	echo "export USER_TOKENS=\"0;0\"
" >> $ENV_FILE-master;

	TOKEN_COUNT=`cat $TOKENS_FILE | wc -l`
	i=1
	s=1
	rm -rf $TOKENS_FILE-slave-*;
	if [ $TOKEN_COUNT -ge $SLAVES ]; then
		while [ $i -le $TOKEN_COUNT ]; do
			sed "${i}q;d" $TOKENS_FILE >> $TOKENS_FILE-slave-$s;
			i=$((i+1));
			if [ $s -lt $SLAVES ]; then
				s=$((s+1));
			else
				s=1;
			fi;
		done;
	else
		while [ $s -le $SLAVES ]; do
			sed "${i}q;d" $TOKENS_FILE >> $TOKENS_FILE-slave-$s;
			s=$((s+1));
			if [ $i -lt $TOKEN_COUNT ]; then
				i=$((i+1));
			else
				i=1;
			fi;
		done;
	fi
	for s in $(seq 1 $SLAVES); do
		echo "export CHE_SERVER_URL=\"$CHE_SERVER_URL\"
export USER_TOKENS=\"$(cat $TOKENS_FILE-slave-$s)\"
" >> $ENV_FILE-slave-$s;
	done
else
	echo "#!/bin/bash
export USER_TOKENS=\"`cat $TOKENS_FILE`\"
" >> $ENV_FILE-master;
fi