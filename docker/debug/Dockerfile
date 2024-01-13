# Dockerfile for debugging application locally from VSCode

# Start with a base image containing Java runtime
FROM eclipse-temurin:21-jdk-alpine

# Add Maintainer Info
LABEL maintainer="info@tournamaths.com"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Expose port 5005 for debugger https://stackoverflow.com/a/72705981
EXPOSE 5005

# The application's jar file
ARG JAR_FILE=target/tournamaths-1.0.jar

# Add the application's jar to the container
ADD ${JAR_FILE} tournamaths.jar

# Run the jar file with debugging
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n", "-jar", "/tournamaths.jar"]
