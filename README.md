# TournaMaths

## Stuff to Install Locally (thse instructions are for Ubuntu)

- Java 21: https://ubuntuhandbook.org/index.php/2022/03/install-jdk-18-ubuntu/
- Terraform: https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli#install-terraform
- Docker: https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository

Further instructions are below for extra setup of linters.

## Instructions to Build and Run Locally

```
./run-local.sh
```

The local website is accessible at http://localhost:8080/

## Instructions to Build, Run and Debug Locally

```
./debug-local.sh
```

Then open VSCode, with the `Extension Pack for Java` (by Microsoft) extension installed, and follow these instructions to run the debugger: https://code.visualstudio.com/docs/editor/debugging

This allows you to use a debugger from VSCode, but it is much slower to run than just running the application.

The local website is accessible at http://localhost:8080/

## Manual Java Linting

#### Setting up Google Java Format On Ubuntu

Full instructions are at: https://github.com/google/google-java-format

Particularly:
```
wget https://github.com/google/google-java-format/releases/download/v1.19.2/google-java-format-1.19.2-all-deps.jar

sudo mv google-java-format-1.19.2-all-deps.jar /usr/local/bin/google-java-format.jar

sudo cp scripts/google-java-format /usr/local/bin/google-java-format
```

#### Executing Java Linting Manually

```
find src/main/java/com/tournamaths -name "*.java" -exec google-java-format -i {} \;
```

There is also a Github CI job to lint the Java and check whether the maven build works, and a pre-commit hook to lint the Java. The pre-commit hook requires Google Java Format to be setup as above.

## Executing Terraform Linting Manually

```
terraform fmt infrastructure/*.tf
```

There is also a Github CI job to lint and print the plan for Terraform, and a pre-commit hook to lint the Terraform.

You don't need to do anything beyond installing Terraform to be able to run Terraform linting.

## Manual JavaScript Linting

#### Setting up ESLint and Prettier on Ubuntu (do this in the root diectory of repo)

1. Install Node.js (for ESLint): https://github.com/nodesource/distributions?tab=readme-ov-file#using-ubuntu
2. Install ESLint and Prettier (I've done this globally as not using Node.js much, but for others it may be best to do it non-globally): https://eslint.org/docs/latest/use/getting-started#global-install https://www.freecodecamp.org/news/using-prettier-and-jslint/#how-to-implement-eslint-and-prettier

```
sudo npm install eslint eslint-plugin-es6 prettier --global
```

#### Regenerating Configuration File from the one in this repo (do this in the root directory of repo)

```
# Go through menu here to select configurations
sudo npm init @eslint/config
```

#### Executing JavaScript Linting/Formatting Manually (do this in the root directory of repo)

```
# Linting
eslint src/main/resources/static/js

# Formatting
prettier --write src/main/resources/static/js
```

## Setting up Git Hooks (particularly pre-commit hooks)

```
git config core.hooksPath git-hooks
```

NOTE - you need to run this every time you make any updates to a Git hook file.

## Accessing Local Development Docker Database via Shell

```
PGPASSWORD=password psql -h localhost -p 5432 -U admin_user -d dev
```

You can also docker exec onto the container and use `psql` to get in from there.

## Accessing Production PostgreSQL Database via Shell from EC2 Serial Console

Start by logging into EC2 serial console. Then run:

```
psql "host=tournamath-db.cj4diopwsatb.us-east-1.rds.amazonaws.com dbname=tournamaths_db user=admin_user password=$(aws secretsmanager get-secret-value --secret-id 'rds!db-b1c7e2b7-9824-4bf7-bb39-6db7c282322d' --region us-east-1 --query 'SecretString' --output text | jq -r .password) sslmode=verify-full sslrootcert=/home/ec2-user/us-east-1-bundle.pem"
```

NOTE if the database or other AWS components are replaced, this command will need to be updated.

## Accessing Local Development Redis Cache via Shell

```
docker exec -it tournamaths_redis_1 redis-cli
```

#### Useful commands to debug Local Development Redis Cache Connectivity

```
# Connect to application image
docker exec -it tournamaths_tournamaths-app-run_1 bash

# If no error (hangs), indicates TCP connection can be established to the Redis
bash -c 'cat < /dev/tcp/redis/6379'

# Should see "PING" as the result - this indicates that the Redis is receiving and processing commands
(echo -en "PING\r\n"; cat < /dev/tcp/redis/6379) | head -c 7
```

## Accessing Production Redis Cache via Shell from EC2 Serial Console

```
redis6-cli -h tournamaths-redis-cluster.cb3ejx.0001.use1.cache.amazonaws.com -p 6379
```

NOTE if the cache or other AWS components are replaced, this command will need to be updated.

## Deploying to Production

Click the "Run workflow" dropdown and select your branch to deploy the latest commit of at https://github.com/mperry-dev/TournaMaths/actions/workflows/deploy.yml, and then click the green "Run workflow" button that appears.

#### Notes

- Secrets for deployment are at https://github.com/mperry-dev/TournaMaths/settings/secrets/actions
- Have spending limit implemented at for Github Actions https://github.com/settings/billing/spending_limit
- Switched on bucket versioning for S3 bucket, in case want to roll back versions of stored code in future.

## Core Technology Components Used and Why I Chose Them

#### Application-level

- Java - I normally program in Python and felt like programming in a different language. Java's got good typing and web frameworks.
- SpringBoot - very popular and well-supported. Its bean system implements separation of concerns well.
- KaTeX library for maths equations - simple, popular, powerful and well-supported. The main alternative seems to be MathJax, which seems to be very slow.
- Thymeleaf for the template engine - popular, powerful and well-supported. Since files are also HTML pages, can load them in browser, which is nice. Beats the main popular competitor I considered (FreeMarker) by supporting custom functionality better: https://springhow.com/spring-boot-template-engines-comparison/
- Hibernate ORM - this seems to be the most popular, best-supported ORM for Spring. It integrates very nicely with SpringBoot.
- JQuery - easier than vanilla javascript
- Typescript - types are nice (EDIT: not doing this yet, this is TODO)
- pico.css - very simple and elegant

#### Infrastructure-level
- Terraform - simple, well-supported. Beats alternatives like Cloudformation as it's cross-platform.
- Docker - used for local development to isolate environments. Particularly useful for local PostgreSQL image, which can be easily spun up and torn down.
- EC2 - I mainly felt like playing around with EC2 for this project. For most projects I'd be inclined to recommend a serverless approach (AWS Lambda) since scaling becomes very simple and OS-level components usually don't need to be handled (overall much simpler). For Lambdas, deployment also becomes a lot simpler and faster since they can be immediately deployed from an S3 bucket, whilst deploying to EC2 instances is slower and is best done using a service like CodeDeploy. For a personal pet project, EC2 provides the advantage of not accidentally increasing costs drastically. It's also a bit simpler to bring together EC2 and RDS than Lambda and RDS, since in EC2 everything gets setup upon instance launch once (or upon some lifecycle hooks), whilst in Lambda you need to potentially handle creating/recreating resources like database engines per Lambda invocation, which can cause performance issues. EC2 also makes it easier to customize things and to access the instance securely using the EC2 Serial Console.
- RDS - PostgreSQL is a powerful relational database, providing strong ACID compliance. I chose non-serverless PostgreSQL to avoid escalating costs for a personal project, but would recommend AWS's serverless PostgreSQL for new applications - it handles scaling up and down automatically, you don't need to add components such as read replicas to improve performance, and it still has full ACID compliance.
- Github Actions for deployment - very simple and powerful way to run AWS deployments from your Github repository. Since it's got full access to the Github repository, my current setup can be extended to provide a mechanism to deploy an arbitrary git tag/commit, and to setup a quick rollback mechanism in-case of deployment issues.
- CodeDeploy for application deployment - AWS's managed deployment service - it provides simple hooks for different deployment lifecycle events.

#### Notes on SpringBoot

- This Springboot app on EC2 initializes and starts up once, including starting the embedded Tomcat server, initializing Spring's application context, and setting up beans and services.
- The embedded Tomcat server handles incoming HTTP requests concurrently using a pool of threads (not using separate processes per-request). There is a single Java process constituting the Springboot application.
- Can scale vertically by upgrading EC2 instances to provide more CPU, memory and resources to handle more concurrent requests effectively. Can scale horizontally by adding more EC2 instances in autoscaling group.

## Application Notes

Currently have:
- Registration/login pages, that implement good security practices - with session-based authentication stored in Redis Elasticache. I lock off or give access to all of the pages on the website appropriately depending on their login status.
    - There is more to do r.e. security best practices here
- A page to create maths questions in an editor
- A page that will be used later to display questions and run a "tournament" of answering as many questions as possible under a time limit
- The ability to logout

## Security Notes

- AWS automatically provides AWS Shield Standard (DDOS protection)
- Have CSRF protection
- Have XSS protection
- Have a Content Security Policy (TODO = expand this)
- Have rate limiting (TODO = expand this based on per-user rate limiting)
- Have password hashing/salting
- You can use https://www.srihash.org/ to get the Subresource Integrity hash (SRI) for a Javascript resource you include from external links

## Infrastructure Notes

- Have RDS database, EC2 instances, Redis Elasticache, Application Load Balancer, IAM permissions, ACLs, security groups, Codedeploy application code deployment (rolling deployment), Web-Application-Firewall
- Security groups and ACLs are used to lock-off resources from the internet as much as possible
- The database is protected by a "defense in depth" approach - it has security groups/ACLs protecting its communications, fully verified SSL encryption of its traffic, encryption of the database, username/password that is automatically managed by AWS
- Have implemented session management using Redis (Elasticache). I generally prefer not to use caches too heavily as it can add complexity, however using a cache is appropriate for sessions and Springboot's integration of it makes it much simpler to be confident in correctness.

## Potential Future Improvements

#### Infrastructure

- Rate limiting using Redis backend
- Stop downtime from occurring when deploy application (by avoiding target group immediately connecting to new EC2 instance before warmup period finished)
- For database - backups, deletion protection
- Connection pooling https://www.baeldung.com/spring-boot-tomcat-connection-pool https://www.baeldung.com/hibernate-spring
- Database migrations library
- Production Cloudwatch Logging
- CDN for faster content delivery
- Backing up and serving Javascript libraries to ensure availability regardless of provider's availability
- Staging environment
- Autoscaling
- Deployment process allowing fast rollbacks and deployments of particular commits (rather than just the latest)
- Faster deployments
- Github Actions deployments waiting for CodeDeploy (this is implemented but commented out)
- Access Control Lists for infrastructure other than the database/cache (as an extra layer on top of security groups - but can add complexity, so not high priority)
- Add indexes to database
- Logging
- Monitoring

#### Security

- Better password policies
- Password reset
- Email verification of user accounts
- Remove ability to login as root user in EC2 from ec2-user (leaving it without doing this for now as it's convenient for debugging purposes)
- SQL injection and Javascript injection protection
- JWT authentication
- 2FA
- CAPTCHAs
- Temporary account lockout
- Lock down file permissions more on EC2 instances

#### Application

- Add Typescript in
- Check password strength when user signs up
- CAPTCHA for bot protection
- Confirm email address when entered in for a new account
- Password reset
- Tests/coverage checking
- End-to-end testing
- Only admin users should be able to edit/delete questions
- Admin interface
- Multi-user competitions
- Design better around multiple screen sizes - particularly mobile phones
- Expressions so that can add templates for questions with random numbers to be added, rather than having to write out every instance of a question
- Different types of questions

## Recommended VSCode Extensions

- Extension Pack for Java (Microsoft)
- Github Actions (Github)
- Git History (Don Jayamanne)
- Lombok Annotations Support for VS Code (Microsoft)
- Github Copilot (Github)
- HashiCorp Terraform (Hashicorp)
- Cron Explained (Tumido)
- Systemd Helper (Liu Yue)
- Code Spell Checker (Street Side Software)
- Docker (Microsoft)
