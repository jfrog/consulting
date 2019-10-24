import groovy.json.JsonSlurper

server = Artifactory.server "artifactory"
rtFullUrl = server.url

podTemplate(label: 'helm-template' , cloud: 'k8s' , containers: [
        containerTemplate(name: 'jfrog-cli', image: 'docker.bintray.io/jfrog/jfrog-cli-go:latest', command: 'cat', ttyEnabled: true) ,
        containerTemplate(name: 'helm', image: 'alpine/helm:latest', command: 'cat', ttyEnabled: true) ]) {

    node('helm-template') {
        stage('Build Chart & push it to Artifactory') {
            latestHelmBuildId =  getLatestHelmChartBuildNumber()
            distributeToEdgeNodes("demo-app" , latestHelmBuildId ,env.DISTRIBUTION_SERVICE_HOST)
        }
    }
}

//Utils


private executeAql(aqlString) {
    File aqlFile = File.createTempFile("aql-query", ".tmp")
    aqlFile.deleteOnExit()
    aqlFile << aqlString

    withCredentials([[$class : 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass',
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


def getLatestHelmChartBuildNumber () {
    def aqlString = 'builds.find ({"name": {"$eq":"helm-app-demo"}}).sort({"$desc":["created"]}).limit(1)'
    results = executeAql(aqlString)

    return results['build.number']
}

def distributeToEdgeNodes (name ,version, distribution_url) {
    def distributePayload = """ {
      "dry_run":"false",
      "distribution_rules": [
        {
            "service_name": "*edge*"
        }
      ]
      }"""

    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        def rbdnRequest = ["curl", "-X", "POST", "-H", "Content-Type:application/json", "-d", "${distributePayload}", "-u", "$USERNAME:$PASSWORD", "${distribution_url}" +
                "/api/v1/distribution/$name/$version"]

        try {
            def rbdnResponse = rbdnRequest.execute().text
            println "Distribution Response is: " + rbdnResponse
        } catch (Exception e) {
            println "Caught exception when requesting distribution. Message ${e.message}"
            throw e
        }
    }

}