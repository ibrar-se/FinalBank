# ==========================================
# STAGE 1: The Build Stage (The Factory)
# ==========================================
# We use a heavy image with Maven and the full JDK to compile the code
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml .
COPY src ./src

# Package the application (skip tests to speed up the build)
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: The Runtime Stage (The Shipping Container)
# ==========================================
# We use a tiny, ultra-lightweight JRE image just to run the compiled app
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy ONLY the compiled .jar file from STAGE 1, leaving all the heavy Maven tools behind
COPY --from=builder /app/target/*.jar app.jar

# Expose a default port (Docker Compose will override this if needed)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]