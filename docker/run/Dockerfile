# Dockerfile for running application locally without exposing ports for debugging

# Start with a base image containing Java runtime
FROM eclipse-temurin:21-jdk-alpine

# Add Maintainer Info
LABEL maintainer="info@tournamaths.com"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file (in docker/run folder, parallel to this Dockerfile)
ARG JAR_FILE=tournamaths-1.0.jar

# Add the application's jar to the container
ADD ${JAR_FILE} tournamaths.jar

# Run the jar file with debugging
ENTRYPOINT ["java", "-jar", "/tournamaths.jar"]
