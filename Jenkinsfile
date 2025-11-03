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

        stage('Checkout helm_repo') {
          steps {
            dir('helm_repo') {
              // ✅ Gitea PAT로 인증해 최신 main을 "클린하게" 가져옴
              git branch: 'main',
                  credentialsId: 'gitea-personal-access-token',
                  url: 'http://gitea-http.infra.svc.cluster.local:3000/chaops/helm_repo.git'

              sh '''
                git fetch origin main
                git checkout main
                git reset --hard origin/main
              '''
            }
          }
        }

        stage('Update values & push') {
          steps {
            dir('helm_repo') {
              withCredentials([string(credentialsId: 'gitea-pat-secret', variable: 'GIT_TOKEN')]) {
                sh '''
                  git config user.email "jenkins@infra.local"
                  git config user.name "jenkins"

                  yq e -i ".image.tag = env.TAG" server/chat/values.yaml \
                    || sed -i 's#^\\( *tag: *\\).*$#\\1"'"$TAG"'"#' server/chat/values.yaml

                  git add server/chat/values.yaml
                  git commit -m "Update image tag to ${TAG}" || echo "No changes"

                  git remote set-url origin "http://jenkins:${GIT_TOKEN}@gitea-http.infra.svc.cluster.local:3000/chaops/helm_repo.git"
                  git push origin HEAD:main
                '''
              }
            }
          }
        }

    }
}
