server = Artifactory.server "artifactory"
rtIpAddress = server.url - ~/^http?.:\/\// - ~/\/artifactory$/

podTemplate(label: 'jenkins-pipeline-npm' , cloud: 'k8s' , containers: [
        containerTemplate(name: 'node', image: 'node:8', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'java', image: 'openjdk:8-jre', command: 'cat', ttyEnabled: true)]) {

    node('jenkins-pipeline-npm') {

        def buildNumber = env.BUILD_NUMBER
        def workspace = env.WORKSPACE
        def buildUrl = env.BUILD_URL

        stage ('Clone') {
            git url: 'https://github.com/eladh/npm-app-demo.git' ,credentialsId: 'github'
        }

        stage('Info') {
            echo "workspace directory is $workspace"
            echo "build URL is $buildUrl"
            echo "build Number is $buildNumber"
            echo "PATH is $env.PATH"
        }

        stage ('Prep env') {
            container('node') {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass',
                                  usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                    sh("curl -u${env.USERNAME}:${env.PASSWORD} http://${rtIpAddress}:80/artifactory/api/npm/auth > ~/.npmrc")
                    sh('echo "email = youremail@email.com" >> ~/.npmrc')
                    sh("npm config set registry http://${rtIpAddress}/artifactory/api/npm/npm/")
                    sh 'npm --no-git-tag-version version minor'
                }
            }
        }

        stage ('Install npm') {
            container('node') {
                sh 'npm install'
            }
        }

        stage ('Build npm') {
            container('node') {
                sh 'npm run build'
                sh 'cp package.json dist/'
            }
        }

        stage('SonarQube analysis') {
            container('java') {
                def scannerHome = tool 'sonar-server-7.6';
                withSonarQubeEnv('my-sonar-qube') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
            }
        }

        stage ('Publish npm') {
            sh 'ssh-keygen -t rsa -N "" -f ~/.ssh/id_rsa'
            sh "ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts"
//            sshagent(credentials: ['githubsshkey']) {
//                sh 'git config --global user.email "you@example.com"'
//                sh 'git config --global user.name "Your Name"'
//                sh 'git remote set-url origin "ssh://git@github.com/eladh/npm-app-demo.git" ';
//                sh 'git add package.json'
//                sh 'git commit -m "bump npm version" package.json '
//                sh 'git push origin master'
//            }
            container('node') {
                sh 'cd dist;npm publish'
            }
        }
    }
}