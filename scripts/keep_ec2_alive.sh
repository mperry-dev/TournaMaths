#!/bin/bash

# Simple script run using cron, to keep EC2 instances alive and application alive.
echo "Running keep_ec2_alive.sh"
curl https://tournamaths.com/questions
echo "Finished running keep_ec2_alive.sh"
