pipeline {
    agent any

    stages {
        stage('Say Hello') {
            steps {
                echo 'this is jenkins pipeline'
            }
        }

        stage('test') {
            steps {
                dir('chat-server') {
                    sh './gradlew test'
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'chat-server/build/test-results/test/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'chat-server/build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Test Report'
                    ])
                }
            }
        }
    }
}
