#!/bin/bash

# Simple script run using cron, to keep EC2 instances alive and application alive.
curl https://tournamaths.com/health_check >> /var/log/keep_ec2_alive.log 2>&1
