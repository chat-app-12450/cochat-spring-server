pipeline {
    agent any

    stages {
        // stage('Test Chat Server') {
        //     steps {
        //         dir('chat-server') {
        //             sh './gradlew test'
        //         }
        //     }
        //     post {
        //         always {
        //             publishHTML([
        //                 allowMissing: false,
        //                 alwaysLinkToLastBuild: true,
        //                 keepAll: true,
        //                 reportDir: 'chat-server/build/reports/tests/test',
        //                 reportFiles: 'index.html',
        //                 reportName: 'Chat Server JUnit Report'
        //             ])
        //         }
        //     }
        // }
        
        stage('Build Chat server ~~') {
            steps {
                dir('chat-server') {
                    sh './gradlew clean build'
                }
            }
        }
        
        stage('Docker Build & Push (Kaniko)') {
            agent {
                kubernetes {
                    containerTemplate {
                        name 'kaniko'
                        image 'gcr.io/kaniko-project/executor:latest'
                        command ''
                        args ''
                        volumeMounts: [
                            [
                                mountPath: '/kaniko/.docker',
                                name: 'dockerhub-secret',
                                readOnly: true
                            ]
                        ]
                    }
                    yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins/jenkins-agent: "true"
spec:
  volumes:
  - name: dockerhub-secret
    secret:
      secretName: dockerhub-secret
"""
                }
            }

            steps {
                container('kaniko') {
                    sh '''
                    /kaniko/executor \
                      --context `pwd`/chat-server \
                      --dockerfile `pwd`/chat-server/Dockerfile \
                      --destination=docker.io/goggleacckai/chat-server:latest \
                      --skip-tls-verify
                    '''
                }
            }
        }
    }
}
