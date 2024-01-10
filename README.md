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

## Potential Future Improvements

#### Infrastructure

- For database - backups, deletion protection, full SSL encryption (SSL communication for defense-in-depth)
- Database migrations library
- Autoscaling
- Staging environment
- Deployment process allowing fast rollbacks and deployments of particular commits (rather than just the latest)
- Linting CI jobs
- Job to check Terraform changes
- Faster deployments
- Github Actions deployments waiting for CodeDeploy (this is implemented but commented out)
- Template for PRs
- Access Control Lists for infrastructure other than the database (as an extra layer on top of security groups - but can add complexity, so not high priority)

## Miscellaneous Notes

- It's probably better to build most new applications in a serverless fashion (using Lambdas and serverless PostgreSQL), but I felt like doing it differently for this project
