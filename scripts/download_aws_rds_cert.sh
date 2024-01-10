#!/bin/bash

# Download RDS truststore, for SSL communication.
wget -O /home/ec2-user/us-east-1-bundle.pem https://truststore.pki.rds.amazonaws.com/us-east-1/us-east-1-bundle.pem >> /var/log/download_aws_rds_cert.log 2>&1
