    CERT=`cat docker.crt`

    kubectl get no -o name | while read node;do
    echo $node
    gcloud compute ssh --zone "$1" ${node:5} --command \
            " sudo su -c ' mkdir -p /etc/docker/certs.d/docker.artifactory.$2.jfrog.com' root &&
    echo '$CERT' > /tmp/ca.crt &&
            sudo su -c ' mv /tmp/ca.crt /etc/docker/certs.d/docker.artifactory.$2.jfrog.com/' root"  </dev/null;
    done
