#!/bin/bash

if [[ $# -eq 0 ]] ; then
echo 'please supply pgp key'
    exit 1
fi

gpg --armor --export $1 > key.pub
gpg --armor --export-secret-keys  $1 > key.priv

cert=$(<key.pub)
private=$(<key.priv)
cert="${cert//$'\n'/\\\\n}"
private="${private//$'\n'/\\\\n}"

sed "s~\"PUBLIC_KEY_VALUE\"~\"$cert\"~" dist-template.json > temp.json
sed "s~\"PRIVATE_KEY_VALUE\"~\"$private\"~" temp.json > distribution.json
sed "s~\"PUBLIC_KEY_VALUE\"~\"$cert\"~" arti-template.json > artifatory.json

rm temp.json
echo 'look for distribution.json and artifatory.json files"
