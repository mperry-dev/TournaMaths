#!/bin/bash
echo "Running start_application.sh"

# Run Spring Boot application
java -jar /home/ec2-user/tournamaths.jar > /dev/null 2> /dev/null < /dev/null &
