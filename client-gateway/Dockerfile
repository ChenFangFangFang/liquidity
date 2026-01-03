# Use Java 17 Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create a non-root user (Security Best Practice)
RUN addgroup --system fintech && adduser --system --group fintech
USER fintech

# Copy the JAR (You must build it locally first!)
COPY target/*.jar app.jar

# Gateways usually run on 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]