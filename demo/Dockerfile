# 1. Use Java 21 (The brain of your app)
FROM eclipse-temurin:21-jdk-alpine

# 2. Where the app lives inside the container
WORKDIR /app

# 3. Copy the 'package' we are about to build
COPY target/*.jar app.jar

# 4. Open the door (Port 8080)
EXPOSE 8080

# 5. Start the engine
ENTRYPOINT ["java", "-jar", "app.jar"]