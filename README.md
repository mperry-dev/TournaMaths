# TournaMaths

## Instructions to Build and Run Locally

```
mvn clean install
docker-compose --profile dev build
docker-compose --profile dev up -d db
docker-compose --profile dev up tournamaths-app
```

## Deployment Notes

- Secrets for deployment are at https://github.com/mperry-dev/TournaMaths/settings/secrets/actions
- Have spending limit implemented at for Github Actions https://github.com/settings/billing/spending_limit
- Switched on bucket versioning for S3 bucket, in case want to roll back versions of stored code in future.
