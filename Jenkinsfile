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
        GIT_TOKEN   = credentialsId('gitea-pat-secret')
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
      stage('Update Helm Repo Image Tag') {
        steps {
          ✅ 1. helm_repo 클론 (Gitea PAT 인증 포함)
          rm -rf helm_repo || true
          git clone -b main http://jenkins:${GITEA_TOKEN}@gitea-http.infra.svc.cluster.local:3000/chaops/helm_repo.git
          
          dir('chat-server') {
            sh '''

              cd helm_repo

              # ✅ 2. 최신 main 브랜치 동기화
              git checkout main
              git pull origin main

              # ✅ 3. 이미지 태그 교체
              sed -i 's#^\\( *tag: *\\).*$#\\1"'"$TAG"'"#' server/chat/values.yaml

              # ✅ 4. 커밋 & 푸시
              git config --global user.email "jenkins@infra.local"
              git config --global user.name "jenkins"

              git add server/chat/values.yaml
              git commit -am "Update chat-server image tag to ${TAG}" || echo "No changes to commit"

              git push origin main
            '''
          }
        }
      }

        }

    }
}
