# ==========================================
# STAGE 1: Build the application using Maven
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy your actual source code and build the .jar file
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the .jar file from STAGE 1 into this final container!
COPY --from=builder /app/target/*.jar app.jar

# Open the port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]