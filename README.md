# TournaMaths

## Instructions to Build and Run Locally

```
mvn clean install
docker-compose --profile dev build
docker-compose --profile dev up -d db
docker-compose --profile dev up tournamaths-app
```
