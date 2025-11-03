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
        DOCKER_REPO = 'docker.io/dockeracckai'     
        IMAGE_NAME  = 'chat-server'               
        TAG         = "${new Date().format('yyyyMMdd')}-${UUID.randomUUID().toString().take(4)}"
        GITEA_TOKEN = credentials('gitea-pat-secret')
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
                dir('chat-server') {
                    sh '''
                      ### ✅ 1. Clone helm_repo from Gitea (with PAT)
                      rm -rf helm_repo || true
                      git clone -b main http://jenkins:${GITEA_TOKEN}@gitea-http.infra.svc.cluster.local:3000/chaops/helm_repo.git

                      cd helm_repo

                      ### ✅ 2. Replace Image Tag
                      sed -i 's#^\\( *tag: *\\).*$#\\1"'"$TAG"'"#' server/chat/values.yaml

                      ### ✅ 3. Commit and Push to main
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
