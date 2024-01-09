#!/bin/bash

# Setup system services - particularly so that if terminate an EC2 instance, SpringBoot application is terminated.
sudo cp /home/ec2-user/systemd-services/stop_application.service /etc/systemd/system/stop_application.service

sudo systemctl daemon-reload
sudo systemctl enable stop_application.service
sudo systemctl start stop_application.service
