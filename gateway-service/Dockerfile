# Use a tiny, lightweight Java 21 JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 🛑 THE FIX: Directly copy the fresh JAR file from your local computer!
COPY target/*.jar app.jar

# Expose the exact port for the Gateway
EXPOSE 8090

# Fire up Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]