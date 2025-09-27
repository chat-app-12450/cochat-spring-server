FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/*.jar

# ✅ 반드시 목적지를 디렉토리로 지정
COPY ${JAR_FILE} /app/

WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]
