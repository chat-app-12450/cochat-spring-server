FROM openjdk:17-jdk-slim

# 빌드 결과물 복사 (단일 jar 파일)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
