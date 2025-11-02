pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                echo '=== Jenkinsfile 실행 시작 ==='
                echo 'Jenkinsfile ✅'
                echo '=== Test Stage 완료 ==='
            }
        }
    }
    
    post {
        always {
            echo '=== Pipeline 실행 완료 ==='
        }
    }
}