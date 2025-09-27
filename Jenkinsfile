pipeline {
    agent any

    stages {
        stage('Build Chat Server') {
            steps {
                dir('chat-server') {
                    sh './gradlew clean build'
                }
            }
        }

        stage('Test Chat Server') {
            steps {
                dir('chat-server') {
                    sh './gradlew test'
                }
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'chat-server/build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Chat Server JUnit Report'
                    ])
                }
            }
        }

        
    }
}
