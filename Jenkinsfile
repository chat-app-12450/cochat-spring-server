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
        }
    }
}
