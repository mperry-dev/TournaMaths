# Generate application JAR file.
mvn clean package

# Build the Docker image:
docker build -t tournamaths .

docker-compose up
