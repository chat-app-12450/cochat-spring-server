pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins/jenkins-agent: "true"
spec:
  containers:
    - name: kaniko
      image: gcr.io/kaniko-project/executor:latest
      tty: true
      volumeMounts:
        - name: dockerhub-secret
          mountPath: /kaniko/.docker/
          readOnly: true
  volumes:
    - name: dockerhub-secret
      secret:
        secretName: dockerhub-secret
"""
        }
    }

    stages {
        stage('Build Chat Server') {
            steps {
                dir('chat-server') {
                    sh './gradlew clean build'
                }
            }
        }

        stage('Docker Build & Push (Kaniko)') {
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
