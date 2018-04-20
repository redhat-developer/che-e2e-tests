#!/bin/bash

TOKEN=$1

if [ -z "$TOKEN" ]; then
  echo "Active token is empty."
  exit 1
fi
TOKEN_PARTS_INDEX=0
IFS='.' read -ra TOKEN_PARTS <<< "${TOKEN}"
for i in "${TOKEN_PARTS[@]}"; do
    TOKEN_PARTS_INDEX=$((TOKEN_PARTS_INDEX + 1))
done
echo "Active token has $TOKEN_PARTS_INDEX sections."
if [[ ! TOKEN_PARTS_INDEX -eq 3 ]]; then
  echo "JWT token parse failed!"
  exit 1
fi
echo "MD5:$(echo -n "${TOKEN}" | md5sum | awk '{print $1}')"
TOKEN_EXP=$(echo "${TOKEN_PARTS[1]}" | base64 -d - 2>/dev/null | ./jq .exp)
CURRENT_UNIX_TIME=$(date +%s)
echo "Active token exp:${TOKEN_EXP}"
echo "Current time in mils:${CURRENT_UNIX_TIME}"
if [[ TOKEN_EXP -gt CURRENT_UNIX_TIME ]]; then
  echo "Token is all well and active"
else
  echo "JWT token is expired"
  exit 1
fi
