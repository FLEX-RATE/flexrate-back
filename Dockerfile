FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/flexrate-back-0.0.1-SNAPSHOT.jar app.jar
COPY build/libs/.env .env
CMD ["java", "-jar", "app.jar"]
EXPOSE 8080