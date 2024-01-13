# Generate application JAR file.
mvn clean install

docker-compose down

# Build the Docker image. Need to rerun when rebuild java package since it grabs JAR.
docker-compose --profile dev build tournamaths-app-run

docker-compose --profile dev up -d db
docker-compose --profile dev up tournamaths-app-run
