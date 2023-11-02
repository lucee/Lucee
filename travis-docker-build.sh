#!/bin/bash

# read current job log after a short delay
sleep 10s
curl -m 30 -s "https://api.travis-ci.org/v3/job/${TRAVIS_JOB_ID}/log.txt?deansi=true" > travis_output.log

# get lucee version
LUCEE_VERSION=$(grep -oP "(?<=\[INFO\] Building Lucee Loader Build )(\d+\.\d+\.\d+\.\d+([-a-zA-Z]*))" travis_output.log)

# build the travis request body
function build_request {
cat <<EOF
{
  "request": {
    "message": "Automated build for version ${LUCEE_VERSION}",
    "branch":"travis-build-matrix",
    "config": {
      "merge_mode": "deep_merge",
      "env": {
        "global": {
          "LUCEE_VERSION": "${LUCEE_VERSION}"
        }
      }
    }
  }
}
EOF
}
REQUEST_BODY=$(build_request)

# trigger the lucee-dockerfiles travis job for this lucee version
curl -m 30 -s -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Travis-API-Version: 3" \
  -H "Authorization: token ${TRAVIS_TOKEN}" \
  -d "${REQUEST_BODY}" \
  https://api.travis-ci.org/repo/lucee%2Flucee-dockerfiles/requests
