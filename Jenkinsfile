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
    - name: jnlp                        # Gradle이 실행되는 컨테이너
      image: jenkins/inbound-agent:3327.v868139a_d00e0-6
      tty: true
      volumeMounts:
        - name: gradle-cache
          mountPath: /home/jenkins/.gradle   # ★ 캐시 경로
    - name: kaniko
      tty: true
      image: gcr.io/kaniko-project/executor:debug   # ← debug 이미지!
      command: ["/busybox/sh","-c"]                 # 컨테이너를 살아있게 유지
      args: ["sleep 365d"]
      volumeMounts:
        - name: dockerhub-secret
          mountPath: /kaniko/.docker/
          readOnly: true
  volumes:
    - name: gradle-cache
      persistentVolumeClaim:
        claimName: gradle-cache-pvc
    - name: dockerhub-secret 
      secret:
        secretName: dockerhub-secret
        items:                  
          - key: .dockerconfigjson
            path: config.json     
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
