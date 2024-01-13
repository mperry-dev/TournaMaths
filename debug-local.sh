# Generate application JAR file.
mvn clean install

# Move JAR into build context
cp target/tournamaths-1.0.jar docker/debug/

docker-compose down

# Build the Docker image. Need to rerun when rebuild java package since it grabs JAR.
docker-compose --profile dev build tournamaths-app-debug

docker-compose --profile dev up -d db
docker-compose --profile dev up tournamaths-app-debug
