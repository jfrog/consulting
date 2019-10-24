
import groovy.json.JsonSlurper

private getLatestArtifact(serverUrl ,aqlString) {
    File aqlFile = File.createTempFile("aql-query", ".tmp")
    aqlFile.deleteOnExit()
    aqlFile << aqlString

    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass',
                      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {

        def getAqlSearchUrl = "curl -u$USERNAME:$PASSWORD -X POST " + serverUrl + "/api/search/aql -T " + aqlFile.getAbsolutePath()
        echo getAqlSearchUrl
        try {
            def response = getAqlSearchUrl.execute().text
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

def executeAql(serverUrl ,aqlString) {
    return getLatestArtifact(serverUrl , aqlString)
}


return this
