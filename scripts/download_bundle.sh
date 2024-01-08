#!/bin/bash
echo "Running download_bundle.sh"

mkdir /tmp/tournamaths

# Download Spring Boot application ZIP (containing a JAR) from S3
aws s3 cp s3://tournamaths/tournamaths-deployment.zip /tmp/tournamaths/

# Unzip JAR from ZIP
unzip /tmp/tournamaths/tournamaths-deployment.zip -d /tmp/tournamaths/
