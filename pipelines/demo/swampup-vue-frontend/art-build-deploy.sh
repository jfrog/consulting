#!/usr/bin/env bash


usage() {
    echo "Deploy a vue-based build to Artifactory using npm"
    echo "Usage: $1 artifactory address";
    echo "Usage: $1$2 artifactory password";
#    exit 1
}

if [ -z "$1" ] || [ -z "$2"  ] ; then
    usage
fi


artiUrl=$1
artPassword=$2

#setup npm locally
curl -u admin:${artPassword} http://${artiUrl}:80/artifactory/api/npm/auth > ~/.npmrc
echo "email = wq237wq@gmail.com" >> ~/.npmrc
npm config set registry http://${artiUrl}/artifactory/api/npm/npm-libs-local/

# remove existing artifact
#curl -uadmin:${artPassword} -XDELETE http://${artiUrl}:80/artifactory/npm-libs-local/frontend/-/frontend-3.0.0.tgz

# build and publish
npm i && npm run build &&  npm publish

