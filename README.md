# TournaMaths

## Instructions to Build and Run Locally

```
./run-local.sh
```

## Instructions to Build, Run and Debug Locally

```
./debug-local.sh
```

Then open VSCode, with the `Extension Pack for Java` (by Microsoft) extension installed, and follow these instructions to run the debugger: https://code.visualstudio.com/docs/editor/debugging

This allows you to use a debugger from VSCode, but it is much slower to run than just running the application.

## Accessing Local Development Docker Database

`PGPASSWORD=password psql -h localhost -p 5432 -U admin_user -d dev`

## Accessing Production PostgreSQL Database via Shell from EC2 Serial Console

Start by logging into EC2 serial console. Then run:

```
psql "host=tournamath-db.cj4diopwsatb.us-east-1.rds.amazonaws.com dbname=tournamaths_db user=admin_user password=$(aws secretsmanager get-secret-value --secret-id 'rds!db-bca8c669-5e58-4292-9a6a-c18f1992caef' --region us-east-1 --query 'SecretString' --output text | jq -r .password) sslmode=verify-full sslrootcert=/home/ec2-user/us-east-1-bundle.pem"
```

NOTE if the database or other AWS components are replaced, this command will need to be updated.

## Deployment Notes

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

## Potential Future Improvements

#### Infrastructure

- Lock versions of pom.xml dependencies in-place, to avoid stuff breaking
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
- Linting CI jobs
- Job to check Terraform changes
- Faster deployments
- Github Actions deployments waiting for CodeDeploy (this is implemented but commented out)
- Access Control Lists for infrastructure other than the database (as an extra layer on top of security groups - but can add complexity, so not high priority)

#### Security

- Remove ability to login as root user in EC2 from ec2-user (leaving it without doing this for now as it's convenient for debugging purposes)
- SQL injection and Javascript injection protection
- CSRF protection
- JWT authentication
- Rate-limiting endpoints which could be abused, like user registration or login
- 2FA
- Lock down file permissions more on EC2 instances

#### Application

- Logout should replace ability to login/register when logged in, redirect should occur from login page, registration shouldn't be available. Endpoints to login/register should also return error.
- Add Typescript in
- Login/account setup page, with logo
- Check password strength when user signs up
- CAPTCHA for bot protection
- Confirm email address when entered in for a new account
- Password reset
- Tests/coverage checking
- Only admin users should be able to edit/delete questions
- Admin interface
- Ability to edit/delete questions
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
