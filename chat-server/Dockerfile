FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} /app/app.jar

WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]
