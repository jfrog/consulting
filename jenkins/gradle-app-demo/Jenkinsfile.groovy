podTemplate(label: 'jenkins-gradle-pipeline' , cloud: 'k8s' , containers: [
        containerTemplate(name: 'gradle', image: 'frekele/gradle:4.7-jdk8u141', command: 'cat', ttyEnabled: true , privileged: true)]) {

    node('jenkins-gradle-pipeline') {
        def server = Artifactory.server "artifactory"
        def rtGradle = Artifactory.newGradleBuild()
        def buildInfo = Artifactory.newBuildInfo()

        stage('Clone sources') {
            git url: 'https://github.com/eladh/gradle-app-demo.git' ,credentialsId: 'github'
        }

        stage ('Artifactory configuration') {
            // Tool name from Jenkins configuration
            // Set Artifactory repositories for dependencies resolution and artifacts deployment.
            rtGradle.deployer repo:'gradle-local', server: server
            rtGradle.resolver repo:'gradle', server: server
            rtGradle.tool = 'gradle-4.10'
            rtGradle.usesPlugin = false // Artifactory plugin already defined in build script
            rtGradle.useWrapper = true
        }

        stage ('Gradle build') {
            container('gradle') {
                sh 'gradle clean build'
                rtGradle.run rootDir: ".", buildFile: 'build.gradle', tasks: 'clean build', buildInfo: buildInfo
                rtGradle.run rootDir: ".", buildFile: 'build.gradle', tasks: 'artifactoryPublish', buildInfo: buildInfo
            }
        }

        stage('SonarQube analysis') {
            def scannerHome = tool 'sonar-server-7.6';
            withSonarQubeEnv('my-sonar-qube') {
                sh "${scannerHome}/bin/sonar-scanner"
            }
        }


        stage ('Publish build info') {
            server.publishBuildInfo buildInfo
        }
    }
}