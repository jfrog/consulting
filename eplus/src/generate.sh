cert=$(<key.pub)
private=$(<key.priv)
cert="${cert//$'\n'/\\\\n}"
private="${private//$'\n'/\\\\n}"

rm distribution.json temp.json artifactory.json 2> /dev/null

sed "s~\"PUBLIC_KEY_VALUE\"~\"$cert\"~" dist-template.json >> temp.json
sed "s~\"PRIVATE_KEY_VALUE\"~\"$private\"~" temp.json > distribution.json
sed "s~\"PUBLIC_KEY_VALUE\"~\"$cert\"~" arti-template.json >> artifatory.json
