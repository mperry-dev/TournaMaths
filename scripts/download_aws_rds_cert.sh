#!/bin/bash

# Download RDS truststore, for SSL communication. Put into temporary location in case download interrupted.
wget -O /tmp/us-east-1-bundle.pem https://truststore.pki.rds.amazonaws.com/us-east-1/us-east-1-bundle.pem >> /var/log/download_aws_rds_cert.log 2>&1

if [ $? -eq 0 ]; then
    # Copy to required location if download seemed to succeed (not going to the effort of verifying checksum or SSL details though)
    # mv should be atomic? https://unix.stackexchange.com/a/322074
    mv /tmp/us-east-1-bundle.pem /home/ec2-user/us-east-1-bundle.pem
    echo "SSL certificate download succeeded." >> /var/log/download_aws_rds_cert.log 2>&1
else
    echo "SSL certificate download failed." >> /var/log/download_aws_rds_cert.log 2>&1
fi
