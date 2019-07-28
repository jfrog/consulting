#!/usr/bin/env bash
set -e

usage() {
    echo "Set pgp keys"
    echo 'Usage: $1 distribution IP'
    echo 'Usage: $2 china artifactory IP'
    echo 'Usage: $3 london edge IP'
    exit 1
}

if [ -z "$1" ] || [ -z "$2"  ] || [ -z "$3" ] ; then
    usage
fi

apk add --no-cache gnupg
gpg --pinentry-mode=loopback --passphrase "" --quiet --batch --no-tty --gen-key gen-key-params

# Find the latest key.
id=$(gpg --no-tty --list-secret-keys --with-colons 2>/dev/null \
    	| tail | awk -F: '/^sec:/ { print $5 } ' | tail -n 1)

private=$(gpg --armor --export-secret-keys $id)
cert=$(gpg --armor --export $id)

cert="${cert//$'\n'/\\\\n}"
private="${private//$'\n'/\\\\n}"

sed "s~\"PUBLIC_KEY_VALUE\"~\"$cert\"~" dist-template.json > temp.json
sed "s~\"PRIVATE_KEY_VALUE\"~\"$private\"~" temp.json > distribution.json
sed "s~\"PUBLIC_KEY_VALUE\"~\"$cert\"~" arti-template.json > artifatory.json

curl -X PUT -u admin:password -H "Content-Type: application/json" "http://$1/api/v1/keys/pgp" -T distribution.json
curl -X POST -u admin:password -H "Content-Type: application/json" "http://$2/artifactory/api/security/keys/trusted" -T artifatory.json 
curl -X POST -u admin:password -H "Content-Type: application/json" "http://$3/artifactory/api/security/keys/trusted" -T artifatory.json 
