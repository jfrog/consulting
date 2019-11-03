#!/usr/bin/env groovy
import groovy.json.JsonSlurper

node {
    def WATCHNAME = env.JOB_NAME
    def jobName = env.JOB_NAME

    def server_url = "http://jfrog.local/artifactory"


    def repo = "helm-virtual"

    stage('Clone repository') {
        /* Let's make sure we have the repository cloned to our workspace */
        git url: 'https://github.com/jfrogtraining/kubernetes_example.git', branch: 'master'
    }

    stage('Prep') {


        /* Configure jfrog cli
         withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
              sh "jfrog rt c beta --user ${USERNAME} --password ${PASSWORD} --url ${server_url} < /dev/null"
         }

    }

   stage('Build Modules') {
             steps {
                     script {
                         sh "make build"
                     }
             }
   }


    stage('Publish') {
              environment {
                  DEMO_VERSION = getVersion()
              }
              steps {
                      script {
                          echo "Publishing all modules with version: ${env.DEMO_VERSION}"
                          sh "make publish"
                     }
              }
     }
}

String getVersion() {
    "${getTimestamp()}-${env.BUILD_NUMBER}".toString()
}


String getTimestamp() {
    new SimpleDateFormat('yyyyMMddHHmmss').format(new Date())
}