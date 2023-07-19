# Generate application JAR file.
mvn clean package

# Build the Docker image:
docker build -t tournamaths .

# Run the Docker container:
docker run -p 8080:8080 tournamaths
