# We only need the Runtime image (No Maven needed!)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create a non-root user
RUN addgroup --system fintech && adduser --system --group fintech
USER fintech

# Copy the JAR you built locally on your Mac
# (Ensure you run 'mvn clean package' locally first!)
COPY pricing-engine/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]