################ Launch configuration.
resource "aws_launch_template" "tournamaths_lt" {
  name_prefix   = "TournaMaths-LT-"
  image_id      = "ami-079db87dc4c10ac91" # Amazon Linux 2023 AMI (chose because optimized for AWS and comes with extra apps, also better documented)
  instance_type = "t3.micro"              # A cheap instance which is built on Nitro System, so can connect via EC2 Serial Console.

  vpc_security_group_ids = [aws_security_group.tournamaths_ec2_sg.id]

  # Disables T3 Unlimited feature so costs don't go up (not too expensive though https://aws.amazon.com/ec2/instance-types/t3/)
  credit_specification {
    cpu_credits = "standard"
  }

  lifecycle {
    create_before_destroy = true
  }

  iam_instance_profile {
    arn = aws_iam_instance_profile.ec2_profile.arn
  }

  user_data = base64encode(<<-EOF
              #!/bin/bash

              sudo yum update -y
              cd /tmp

              # Setup password to login via EC2 Serial Console
              PASSWORD=$(aws secretsmanager get-secret-value --secret-id ${aws_secretsmanager_secret.ec2_password.id} --query 'SecretString' --output text | jq -r .password)
              echo "ec2-user:$PASSWORD" | chpasswd

              # Install CodeDeploy Agent (for AWS CodeDeploy to be able to deploy on these EC2 instances)
              sudo yum install -y ruby wget
              wget https://aws-codedeploy-${var.aws_region}.s3.amazonaws.com/latest/install
              chmod +x ./install
              sudo ./install auto

              # Install CloudWatch Agent
              sudo yum install -y amazon-cloudwatch-agent

              # Start the CodeDeploy and CloudWatch agents
              sudo service codedeploy-agent start
              sudo systemctl start amazon-cloudwatch-agent

              # Install Java
              wget https://download.oracle.com/java/20/archive/jdk-20_linux-x64_bin.rpm
              sudo rpm -ivh jdk-20_linux-x64_bin.rpm

              #################### Download application and start it
              # Download Spring Boot application ZIP (containing a JAR) from S3
              aws s3 cp s3://tournamaths/tournamaths-deployment.zip /home/ec2-user/

              # Unzip JAR from ZIP
              unzip /home/ec2-user/tournamaths-deployment.zip -d /home/ec2-user/

              cd /home/ec2-user
              cp target/tournamaths-1.0.jar tournamaths.jar

              ./scripts/setup_systemd.sh
              ./scripts/start_application.sh

              #################### Setup cron to keep EC2 instances alive - make request to application every 10 minutes.
              # It would be better to solve the root cause of EC2 instances "going to sleep",
              # but because this cron's request goes through all infrastructure components (from the domain down to the instance),
              # it is a more thorough way to keep alive than running target group health check.
              sudo yum install -y cronie
              # NOTE - this is all being executed as the root user, so have to use "sudo su -" to see the crontab jobs.
              echo "*/10 * * * * /home/ec2-user/scripts/keep_ec2_alive.sh" | crontab -
              sudo systemctl enable crond
              sudo systemctl start crond
              EOF
  )
}

################ Autoscaling group for application.
resource "aws_autoscaling_group" "tournamaths_asg" {
  name                      = "TournaMaths-ASG"
  desired_capacity          = 2
  max_size                  = 5
  min_size                  = 1
  health_check_grace_period = 300
  health_check_type         = "EC2"
  force_delete              = true
  vpc_zone_identifier       = [aws_subnet.tournamaths_public_subnet_1a.id, aws_subnet.tournamaths_public_subnet_1b.id]

  target_group_arns = [aws_lb_target_group.tournamaths_tg.arn]

  launch_template {
    id      = aws_launch_template.tournamaths_lt.id
    version = aws_launch_template.tournamaths_lt.latest_version # Specify this instead of "$Latest" so instance refresh triggered when launch template changes
  }

  # Just before an instance is terminated, give 300 seconds to perform any required actions,
  # and if this time expires, termination process continues as normal.
  initial_lifecycle_hook {
    name                 = "instance-termination-hook"
    default_result       = "CONTINUE"
    heartbeat_timeout    = 300 # In seconds
    lifecycle_transition = "autoscaling:EC2_INSTANCE_TERMINATING"
  }

  # When deploy infrastructure changes, replace the EC2 instances 1 by 1 so don't have downtime.
  instance_refresh {
    strategy = "Rolling"

    preferences {
      min_healthy_percentage = 50
      instance_warmup        = 300 # In seconds
    }

    triggers = ["tag"]
  }

  # Adding this tag, so if IAM policies change, instance refresh is triggered
  # (since trigger instance refresh above when tags change).
  # NOTE also that a refresh will always be triggered by a change in any of
  # launch_configuration, launch_template, or mixed_instances_policy:
  # https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/autoscaling_group
  tag {
    key                 = "iam-policy-hash"
    value               = sha256(file("${path.module}/iam.tf"))
    propagate_at_launch = true
  }
}
