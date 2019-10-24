import groovy.json.JsonSlurper
import groovy.json.JsonOutput

server = Artifactory.server "artifactory"
rtFullUrl = server.url

podTemplate(label: 'helm-template' , cloud: 'k8s' , containers: [
        containerTemplate(name: 'jfrog-cli', image: 'docker.bintray.io/jfrog/jfrog-cli-go:latest', command: 'cat', ttyEnabled: true) ,
        containerTemplate(name: 'helm', image: 'alpine/helm:latest', command: 'cat', ttyEnabled: true) ]) {

    node('helm-template') {
        stage('Build Chart & push it to Artifactory') {
            latestHelmBuildId =  getLatestHelmChartBuildNumber()
            dockerChecksum = getDockerPathByChecksum(getBuildDockerImageManifestChecksum(latestHelmBuildId))
            createDemoAppReleaseBundle(latestHelmBuildId ,dockerChecksum , env.DISTRIBUTION_SERVICE_HOST)
        }
    }
}

//Utils

private executeAql(aqlString) {
    File aqlFile = File.createTempFile("aql-query", ".tmp")
    aqlFile.deleteOnExit()
    aqlFile << aqlString

    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass',
                      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {

        def getAqlSearchUrl = "curl -u$USERNAME:$PASSWORD -X POST " + rtFullUrl + "/api/search/aql -T " + aqlFile.getAbsolutePath()
        echo getAqlSearchUrl
        try {
            println aqlString
            def response = getAqlSearchUrl.execute().text
            println response
            def jsonSlurper = new JsonSlurper()
            def latestArtifact = jsonSlurper.parseText("${response}")

            println latestArtifact
            return new HashMap<>(latestArtifact.results[0])
        } catch (Exception e) {
            println "Caught exception finding lastest artifact. Message ${e.message}"
            throw e as java.lang.Throwable
        }
    }
}

def getArtifactoryServiceId() {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        getServiceIdCommand = ["curl", "-s", "-u$USERNAME:$PASSWORD", "$rtFullUrl/api/system/service_id"]
        return getServiceIdCommand.execute().text
    }
}


def getLatestHelmChartBuildNumber () {
    def aqlString = 'builds.find ({"name": {"$eq":"helm-app-demo"}}).sort({"$desc":["created"]}).limit(1)'
    results = executeAql(aqlString)

    return results['build.number']
}

def getDockerPathByChecksum (checksum) {
    def aqlString = 'items.find ({ "repo":"docker-prod-local","actual_sha1":"' + checksum + '", "path":{"$ne":"docker-multi-app/latest"}})'
    results = executeAql(aqlString)

    return results.path
}


def getBuildDockerImageManifestChecksum (build_number) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        def getBuildInfo = "curl -u$USERNAME:$PASSWORD " + rtFullUrl + "/api/build/helm-app-demo/$build_number"
        println getBuildInfo

        try {
            def buildInfoText = getBuildInfo.execute().text
            def jsonSlurper = new JsonSlurper()
            def buildInfo = jsonSlurper.parseText("${buildInfoText}")

            println buildInfo.buildInfo.modules[0].dependencies

            return buildInfo.buildInfo.modules[0].dependencies.find{it.id == "manifest.json"}.sha1
        } catch (Exception e) {
            println "Caught exception finding latest helm chart build number. Message ${e.message}"
            throw e
        }
    }
}

def createDemoAppReleaseBundle(chartBuildId, dockerImage, distribution_url) {

    def aqlhelmString = """
            items.find(
                    {
                        \"artifact.module.build.name\": {
                                    \"\$eq\": \"helm-app-demo\"
                                }
                    } ,
                    {
                        \"artifact.module.build.number\": {
                                    \"\$eq\": \"${chartBuildId}\"
                                }
                    },
                    {
                       \"repo\": {
                                    \"\$eq\": \"helm-local\"
                                }
                    }
           )
            """.replaceAll(" ", "").replaceAll("\n", "")

    def aqldockerAppString = "items.find({\"repo\":\"docker-prod-local\",\"path\":\"" + dockerImage + "\"})"

    def releaseBundleBody = [
            'name': "demo-app",
            'version': "${chartBuildId}",
            'dry_run': false,
            'sign_immediately': true,
            'description': 'Release bundle for the example java-project',
            'spec': [
                    'source_artifactory_id': "${getArtifactoryServiceId()}",
                    'queries': [
                            [
                                    'aql': "${aqldockerAppString}"
                            ],
                            [
                                    'aql': "${aqlhelmString}"
                            ]
                    ]
            ]
    ]

    releaseBundleJson = JsonOutput.toJson(releaseBundleBody)


    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        def rbdnRequest = ["curl", "-X", "POST", "-H", "Content-Type: application/json", "-d", "${releaseBundleJson}", "-u", "$USERNAME:$PASSWORD", "${distribution_url}/api/v1/release_bundle"]

        try {
            def rbdnResponse = rbdnRequest.execute().text
            println "Release Bundle Response is: " + rbdnResponse
            if (rbdnResponse.contains("status_code")) {
               error("unable to create distribution bundle")
            }
        } catch (Exception e) {
            println "Caught exception finding latest docker-multi-app helm chart. Message ${e.message}"
            throw e
        }
    }

}