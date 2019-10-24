
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

def downloadArtifact(serverUrl ,repo ,artifact ,type , buildInfo ,explode) {
    def aqlString = 'items.find ({ "repo":"' + repo + '", "path":{"\$match":"' + artifact + '"},' +
            '"name":{"\$match":"*.' + type + '"}' +
            '}).include("created","path","name").sort({"\$desc":["created"]}).limit(1)'

    def artifactInfo = getLatestArtifact(serverUrl ,aqlString)
    def lastArtifact = artifactInfo.path + "/" + artifactInfo.name

    def latestVer = repo +  "/" + lastArtifact

    def downloadConfig = """{
                 "files": [
                 {
                 "pattern": "${latestVer}",
                 "flat": "true",
                 "explode": "${explode}"
                 }
                 ]
             }"""

    server.download(downloadConfig, buildInfo)
}

return this
