################ Database secrets.
data "aws_secretsmanager_secret" "db_creds_secret" {
  name = "DB_Creds_Secret"
}

data "aws_secretsmanager_secret_version" "db_creds" {
  secret_id = data.aws_secretsmanager_secret.db_creds_secret.id
}

################ VPC.
resource "aws_vpc" "tourna_math_vpc" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "TournaMaths-VPC"
  }
}

################ Public subnets for the application's EC2 instances.
resource "aws_subnet" "tourna_math_subnet_1a" {
  vpc_id            = aws_vpc.tourna_math_vpc.id
  cidr_block        = "10.0.1.0/24"
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "TournaMaths-Subnet-1a"
  }
}

resource "aws_subnet" "tourna_math_subnet_1b" {
  vpc_id            = aws_vpc.tourna_math_vpc.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "TournaMaths-Subnet-1b"
  }
}

################ Private subnets for database, with a subnet group that the database will be restricted to.
resource "aws_subnet" "tourna_math_private_subnet_1a" {
  vpc_id            = aws_vpc.tourna_math_vpc.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "TournaMaths-Private-Subnet-1a"
  }
}

resource "aws_subnet" "tourna_math_private_subnet_1b" {
  vpc_id            = aws_vpc.tourna_math_vpc.id
  cidr_block        = "10.0.4.0/24"
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "TournaMaths-Private-Subnet-1b"
  }
}

resource "aws_db_subnet_group" "tourna_math_private_db_subnet_group" {
  name       = "tournamaths-private-db-subnet-group"
  subnet_ids = [aws_subnet.tourna_math_private_subnet_1a.id, aws_subnet.tourna_math_private_subnet_1b.id]

  tags = {
    Name = "tournamaths-private-db-subnet-group"
  }
}

################ Internet gateway.
resource "aws_internet_gateway" "tourna_math_igw" {
  vpc_id = aws_vpc.tourna_math_vpc.id

  tags = {
    Name = "TournaMaths-IGW"
  }
}

################ Route table with associations to subnets.
resource "aws_route_table" "tourna_math_route_table" {
  vpc_id = aws_vpc.tourna_math_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.tourna_math_igw.id
  }

  tags = {
    Name = "TournaMaths-Route-Table"
  }
}

# route_table_association to public route table means the linked subnets are public.
resource "aws_route_table_association" "tourna_math_route_table_association_1a" {
  subnet_id      = aws_subnet.tourna_math_subnet_1a.id
  route_table_id = aws_route_table.tourna_math_route_table.id
}

resource "aws_route_table_association" "tourna_math_route_table_association_1b" {
  subnet_id      = aws_subnet.tourna_math_subnet_1b.id
  route_table_id = aws_route_table.tourna_math_route_table.id
}

################ Security group.
resource "aws_security_group" "tourna_math_sg" {
  vpc_id = aws_vpc.tourna_math_vpc.id

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "TournaMaths-SG"
  }
}

################ ALB, Target-Group, Listener.
resource "aws_lb" "tourna_math_alb" {
  name               = "TournaMaths-ALB"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.tourna_math_sg.id]
  subnets            = [aws_subnet.tourna_math_subnet_1a.id, aws_subnet.tourna_math_subnet_1b.id]

  enable_deletion_protection = false

  tags = {
    Name = "TournaMaths-ALB"
  }
}

resource "aws_lb_target_group" "tourna_math_tg" {
  name     = "TournaMaths-TG"
  port     = 80
  protocol = "HTTP"
  vpc_id   = aws_vpc.tourna_math_vpc.id

  health_check {
    enabled  = true
    interval = 30
    path     = "/"
    timeout  = 3
  }

  tags = {
    Name = "TournaMaths-TG"
  }
}

resource "aws_lb_listener" "front_end" {
  load_balancer_arn = aws_lb.tourna_math_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tourna_math_tg.arn
  }
}

################ Launch configuration.
resource "aws_launch_template" "tourna_math_lt" {
  name_prefix   = "TournaMaths-LT-"
  image_id      = "ami-053b0d53c279acc90" # Ubuntu Server 22.04 LTS (HVM), SSD Volume Type (provided by Ubuntu)
  instance_type = "t2.micro"              # A cheap instance which unlike t3.micro, doesn't have unlimited bursting, so is safer cost-wise.

  vpc_security_group_ids = [aws_security_group.tourna_math_sg.id]

  user_data = base64encode(<<-EOF
              #!/bin/bash

              # Install CodeDeploy Agent (for AWS CodeDeploy to be able to deploy on these EC2 instances)
              sudo yum update -y
              sudo yum install -y ruby
              sudo yum install -y wget
              cd /home/ubuntu
              wget https://aws-codedeploy-${var.aws_region}.s3.amazonaws.com/latest/install
              chmod +x ./install
              sudo ./install auto

              # Install Java
              sudo apt update
              sudo apt install -y openjdk-20-jdk

              # Download Spring Boot application JAR from S3
              aws s3 cp s3://tournamaths/tournamaths.jar /home/ubuntu/

              # Run Spring Boot application
              java -jar /home/ubuntu/tournamaths.jar > /dev/null 2> /dev/null < /dev/null &
              EOF
  )

  lifecycle {
    create_before_destroy = true
  }

  iam_instance_profile {
    arn = aws_iam_instance_profile.ec2_codedeploy_profile.arn
  }
}

################ Autoscaling group for application.
resource "aws_autoscaling_group" "tourna_math_asg" {
  desired_capacity          = 2
  max_size                  = 5
  min_size                  = 1
  health_check_grace_period = 300
  health_check_type         = "EC2"
  force_delete              = true
  vpc_zone_identifier       = [aws_subnet.tourna_math_subnet_1a.id, aws_subnet.tourna_math_subnet_1b.id]

  target_group_arns = [aws_lb_target_group.tourna_math_tg.arn]

  launch_template {
    id      = aws_launch_template.tourna_math_lt.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "TournaMaths-ASG"
    propagate_at_launch = true
  }
}

################ Route53 configuration - records, and SSL certificate.

resource "aws_route53_zone" "tournamaths_zone" {
  name          = "tournamaths.com"
  force_destroy = true
}

resource "aws_route53_record" "tournamaths_a_record" {
  zone_id = aws_route53_zone.tournamaths_zone.zone_id
  name    = "tournamaths.com"
  type    = "A"

  alias {
    name                   = aws_lb.tourna_math_alb.dns_name
    zone_id                = aws_lb.tourna_math_alb.zone_id
    evaluate_target_health = true
  }
}

# Because certificates have to be validated, might need to run Terraform multiple times if replace certificate
# https://stackoverflow.com/questions/72227832/certificate-must-have-a-fully-qualified-domain-name-a-supported-signature-and
resource "aws_acm_certificate" "tournamaths_cert" {
  domain_name       = "tournamaths.com"
  validation_method = "DNS"
}

resource "aws_lb_listener" "https_listener" {
  load_balancer_arn = aws_lb.tourna_math_alb.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = aws_acm_certificate.tournamaths_cert.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tourna_math_tg.arn
  }

  # Want this listener created after SSL certificate validated, otherwise can get this sort of error:
  # https://stackoverflow.com/questions/72227832/certificate-must-have-a-fully-qualified-domain-name-a-supported-signature-and
  depends_on = [
    aws_acm_certificate_validation.tournamaths_cert_validation
  ]
}

# Keep registered domain nameservers up-to-date with hosted zone
# NOTE AWS hosted zones always have 4 name servers https://aws.amazon.com/route53/faqs/
# Doing this statically since if use loop, Terraform creates resources for each nameserver,
# replaces them 1-by-1, and then we hit an error that there must be 2 to 6 nameservers for the domain.
resource "aws_route53domains_registered_domain" "tournamaths_domain" {
  domain_name = "tournamaths.com"

  name_server {
    name = aws_route53_zone.tournamaths_zone.name_servers[0]
  }
  name_server {
    name = aws_route53_zone.tournamaths_zone.name_servers[1]
  }
  name_server {
    name = aws_route53_zone.tournamaths_zone.name_servers[2]
  }
  name_server {
    name = aws_route53_zone.tournamaths_zone.name_servers[3]
  }
}

# DNS Validation of cerficate
resource "aws_route53_record" "tournamaths_cert_validation_record" {
  for_each = {
    for dvo in aws_acm_certificate.tournamaths_cert.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  }

  allow_overwrite = true
  zone_id         = aws_route53_zone.tournamaths_zone.zone_id
  name            = each.value.name
  type            = each.value.type
  records         = [each.value.record]
  ttl             = 600
}

resource "aws_acm_certificate_validation" "tournamaths_cert_validation" {
  certificate_arn         = aws_acm_certificate.tournamaths_cert.arn
  validation_record_fqdns = [for record in aws_route53_record.tournamaths_cert_validation_record : record.fqdn]

  # Certificate validation depends on registered domain nameservers matching hosted zone nameservers
  depends_on = [
    aws_route53domains_registered_domain.tournamaths_domain
  ]
}

################ Database.
resource "aws_db_instance" "tourna_math_db" {
  allocated_storage      = 20
  storage_type           = "gp2"
  engine                 = "postgres"
  engine_version         = "15.3"
  instance_class         = "db.t4g.micro"
  db_name                = "tourna_math_db"
  username               = "admin_user" # Can't say "admin" here as that's reserved in Postgres.
  password               = jsondecode(data.aws_secretsmanager_secret_version.db_creds.secret_string)["db_admin_user_password"]
  parameter_group_name   = "default.postgres15"
  skip_final_snapshot    = true
  vpc_security_group_ids = [aws_security_group.tourna_math_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.tourna_math_private_db_subnet_group.name

  tags = {
    Name = "TournaMaths-DB"
  }
}
