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
      tty: true
      image: gcr.io/kaniko-project/executor:debug
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
        IMAGE_TAG = "build-${env.BUILD_NUMBER}-${new Date().format('yyyyMMddHHmm')}"
        IMAGE_NAME = "docker.io/dockeracckai/kai:${IMAGE_TAG}"
        GIT_REPO = "https://<git-user>:<token>@git.yourdomain.com/yourteam/gitops-repo.git"
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
                    sh """
                    /kaniko/executor \
                      --context `pwd`/chat-server \
                      --dockerfile `pwd`/chat-server/Dockerfile \
                      --destination=${IMAGE_NAME} \
                      --skip-tls-verify
                    """
                }
            }
        }

        stage('Update GitTea Repo (for Argo CD)') {
            steps {
                sh '''
                git config --global user.name "jenkins-bot"
                git config --global user.email "jenkins@ci.local"

                git clone ${GIT_REPO} gitops
                cd gitops

                # 예: k8s/deployment.yaml 내 image 라인 교체
                sed -i "s|image: .*|image: ${IMAGE_NAME}|" k8s/chat-server/deployment.yaml

                git add .
                git commit -m "Update chat-server image to ${IMAGE_TAG}"
                git push origin main
                '''
            }
        }
    }
}
