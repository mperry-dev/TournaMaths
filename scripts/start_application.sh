#!/bin/bash

# Run Spring Boot application with production profile
java -jar -Dspring.profiles.active=prod /home/ec2-user/tournamaths.jar > /dev/null 2> /dev/null < /dev/null &
