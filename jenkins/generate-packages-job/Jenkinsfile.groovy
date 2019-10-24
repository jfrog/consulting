firstTimeInit()

podTemplate(label: 'jenkins-pipeline' , cloud: 'k8s' , containers: [
        containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true , privileged: true)],
        volumes: [hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')]) {

    node('jenkins-pipeline') {

        stage('Cleanup') {
            cleanWs()
        }

        stage('Docker build') {
            withCredentials([usernamePassword(credentialsId: 'artifactorypass', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                container('docker') {
                    sh("docker run --rm -e 'ARTIFACTORY_URL=$ARTIFACTORY_URL' \
                                        -e 'ARTIFACTORY_USER=$USER' \
                                        -e 'ARTIFACTORY_PASSWORD=$PASSWORD' \
                                        -e 'ARTIFACTORY_REPO=$REPO_NAME' \
                                        -e 'PACKAGE_CLONE_MAX_LEVEL=2' \
                                        -e 'PACKAGES_DUPLICATION_RATE=2' \
                                        -e 'PACKAGE_NUMBER=$NUM_OF_ARTIFACTS' \
                                        -e 'PACKAGE_SIZE_MIN=$PACKAGE_SIZE_MIN' \
                                        -e 'PACKAGE_SIZE_MAX=$PACKAGE_SIZE_MAX'  eladhr/generic-generator:2.0")
                }
            }
        }
    }
}

void firstTimeInit() {
    if  (params.ARTIFACTORY_URL == null) {
        properties([
                parameters([
                        string(name: 'ARTIFACTORY_URL', defaultValue: '' ,description: 'please select artifactory url - http://xxx.xxx.xxx.xxx/artifactory',),
                        string(name: 'REPO_NAME', defaultValue: '' ,description: 'Please select target repo name',),
                        string(name: 'PACKAGE_SIZE_MIN', defaultValue: '' ,description: 'Please select min size (bytes) ',),
                        string(name: 'PACKAGE_SIZE_MAX', defaultValue: '' ,description: 'Please select max size (bytes)',),
                        string(name: 'NUM_OF_ARTIFACTS', defaultValue: '' ,description: 'Please select num of artifacts to generate',),
                ])
        ])
        currentBuild.result = 'SUCCESS'
        error('Aborting for first time job setup')
    }
}
