# 1. Use Java 21
FROM eclipse-temurin:21-jdk-alpine

# 2. Set the working folder inside the container
WORKDIR /app

# 3. Copy your compiled code
COPY target/*.jar app.jar

# 4. Open the port
EXPOSE 8080

# 5. Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]