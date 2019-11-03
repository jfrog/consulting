#!/usr/bin/env bash
set -o xtrace
set -e

usage() {
    echo "Deploy project to Production"
    echo 'Usage: $1 artifactory address'
    echo 'Usage: $3 artifactory apikey'
    echo 'Usage: $4 gosvc image tag'
    exit 1
}

if [ -z "$1" ] || [ -z "$2"  ] || [ -z "$3" ]; then
    usage
fi


SERVER=$1
APIKEY=$2
GOSVCIMGTAG=$3


sudo systemctl restart docker

echo "$APIKEY" | docker login --username admin --password-stdin "$SERVER"

docker stop docker-gosvc  && docker rm $_
docker rmi $(docker images | grep "${SERVER}/gosvc" | awk '{ print $3 }') || true

docker run -d --name docker-gosvc -p 3000:3000 ${SERVER}/gosvc:$GOSVCIMGTAG

echo "Deploy Done"
