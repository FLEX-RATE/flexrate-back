FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test --no-daemon
CMD ["java", "-jar", "build/libs/*.jar"]
EXPOSE 8080