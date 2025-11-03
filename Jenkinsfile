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
    - name: jnlp
      image: jenkins/inbound-agent:3327.v868139a_d00e0-6
      tty: true
      volumeMounts:
        - name: gradle-cache
          mountPath: /home/jenkins/.gradle
    - name: kaniko
      image: gcr.io/kaniko-project/executor:debug
      tty: true
      command: ["/busybox/sh","-c"]
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

    environment {
        DOCKER_REPO = 'docker.io/dockeracckai'     // Docker Hub repository
        IMAGE_NAME  = 'chat-server'                // 이미지 이름
        TAG         = "${new Date().format('yyyyMMdd')}-${UUID.randomUUID().toString().take(4)}"
    }

    stages {
        stage('Build Chat Server') {
            steps {
                dir('chat-server') {
                    sh '''
                        ./gradlew clean build
                        mv build/libs/*SNAPSHOT.jar build/libs/app.jar
                    '''
                }
            }
        }

        stage('Docker Build & Push (Kaniko)') {
            steps {
                container('kaniko') {
                    sh '''
                        echo "🔹 Building image: ${DOCKER_REPO}/${IMAGE_NAME}:${TAG}"

                        /kaniko/executor \
                          --context `pwd`/chat-server \
                          --dockerfile `pwd`/chat-server/Dockerfile \
                          --destination=${DOCKER_REPO}/${IMAGE_NAME}:${TAG} \
                          --skip-tls-verify
                    '''
                }
            }
        }
    }
}
