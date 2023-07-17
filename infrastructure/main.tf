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
  vpc_id     = aws_vpc.tourna_math_vpc.id
  cidr_block = "10.0.1.0/24"
  availability_zone = "us-east-1a"

  tags = {
    Name = "TournaMaths-Subnet-1a"
  }
}

resource "aws_subnet" "tourna_math_subnet_1b" {
  vpc_id     = aws_vpc.tourna_math_vpc.id
  cidr_block = "10.0.2.0/24"
  availability_zone = "us-east-1b"

  tags = {
    Name = "TournaMaths-Subnet-1b"
  }
}

################ Private subnets for database, with a subnet group that the database will be restricted to.
resource "aws_subnet" "tourna_math_private_subnet_1a" {
  vpc_id     = aws_vpc.tourna_math_vpc.id
  cidr_block = "10.0.3.0/24"
  availability_zone = "us-east-1a"

  tags = {
    Name = "TournaMaths-Private-Subnet-1a"
  }
}

resource "aws_subnet" "tourna_math_private_subnet_1b" {
  vpc_id     = aws_vpc.tourna_math_vpc.id
  cidr_block = "10.0.4.0/24"
  availability_zone = "us-east-1b"

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
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
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
    enabled = true
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

################ Lunch configuration.
resource "aws_launch_configuration" "tourna_math_lc" {
  name          = "TournaMaths-LC"
  image_id      = "ami-053b0d53c279acc90"  # Ubuntu Server 22.04 LTS (HVM), SSD Volume Type (provided by Ubuntu)
  instance_type = "t2.micro"  # A cheap instance which unlike t3.micro, doesn't have unlimited bursting, so is safer cost-wise.
  security_groups = [aws_security_group.tourna_math_sg.id]

  lifecycle {
    create_before_destroy = true
  }

  user_data = <<-EOF
              #!/bin/bash
              echo "Hello, TournaMaths" > index.html
              nohup busybox httpd -f -p 80 &
              EOF
}

################ Autoscaling group for application.
resource "aws_autoscaling_group" "tourna_math_asg" {
  desired_capacity     = 2
  max_size             = 5
  min_size             = 1
  health_check_grace_period = 300
  health_check_type        = "EC2"
  force_delete             = true
  launch_configuration     = aws_launch_configuration.tourna_math_lc.name
  vpc_zone_identifier      = [aws_subnet.tourna_math_subnet_1a.id, aws_subnet.tourna_math_subnet_1b.id]

  tag {
    key = "Name"
    value = "TournaMaths-ASG"
    propagate_at_launch = true
  }
}

################ Database.
resource "aws_db_instance" "tourna_math_db" {
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "postgres"
  engine_version       = "15.3"
  instance_class       = "db.t4g.micro"
  db_name              = "tourna_math_db"
  username             = "admin_user"  # Can't say "admin" here as that's reserved in Postgres.
  password             = jsondecode(data.aws_secretsmanager_secret_version.db_creds.secret_string)["db_admin_user_password"]
  parameter_group_name = "default.postgres15"
  skip_final_snapshot  = true
  vpc_security_group_ids = [aws_security_group.tourna_math_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.tourna_math_private_db_subnet_group.name

  tags = {
    Name = "TournaMaths-DB"
  }
}
