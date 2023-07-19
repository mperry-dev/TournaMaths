# Generate application JAR file.
mvn clean install

# Build the Docker image. Need to rerun when rebuild java package since it grabs JAR.
docker build -t tournamaths .

docker-compose up
