data "aws_secretsmanager_secret" "db_creds_secret" {
  name = "DB_Creds_Secret"
}

data "aws_secretsmanager_secret_version" "db_creds" {
  secret_id = data.aws_secretsmanager_secret.db_creds_secret.id
}

resource "aws_vpc" "tourna_math_vpc" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "TournaMaths-VPC"
  }
}

resource "aws_subnet" "public_subnet" {
  vpc_id     = aws_vpc.tourna_math_vpc.id
  cidr_block = "10.0.1.0/24"

  tags = {
    Name = "TournaMaths-Public-Subnet"
  }
}

resource "aws_internet_gateway" "tourna_math_igw" {
  vpc_id = aws_vpc.tourna_math_vpc.id

  tags = {
    Name = "TournaMaths-IGW"
  }
}

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

resource "aws_route_table_association" "tourna_math_route_table_association" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.tourna_math_route_table.id
}

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

resource "aws_lb" "tourna_math_alb" {
  name               = "TournaMaths-ALB"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.tourna_math_sg.id]
  subnets            = [aws_subnet.public_subnet.id]

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

resource "aws_launch_configuration" "tourna_math_lc" {
  name          = "TournaMaths-LC"
  image_id      = "ami-053b0d53c279acc90"  # Ubuntu Server 22.04 LTS (HVM), SSD Volume Type (provided by Ubuntu)
  instance_type = "t2.micro"
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

resource "aws_autoscaling_group" "tourna_math_asg" {
  desired_capacity     = 2
  max_size             = 5
  min_size             = 1
  health_check_grace_period = 300
  health_check_type        = "EC2"
  force_delete             = true
  launch_configuration     = aws_launch_configuration.tourna_math_lc.name
  vpc_zone_identifier      = [aws_subnet.public_subnet.id]

  tag {
    key = "Name"
    value = "TournaMaths-ASG"
    propagate_at_launch = true
  }
}

resource "aws_db_instance" "tourna_math_db" {
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "postgres"
  engine_version       = "13.3"
  instance_class       = "db.t2.micro"
  name                 = "tourna_math_db"
  username             = "foo"
  password             = jsondecode(data.aws_secretsmanager_secret_version.db_creds.secret_string)["password"]
  parameter_group_name = "default.postgres13"
  skip_final_snapshot  = true
  vpc_security_group_ids = [aws_security_group.tourna_math_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.tourna_math_db_subnet_group.name

  tags = {
    Name = "TournaMaths-DB"
  }
}

resource "aws_db_subnet_group" "tourna_math_db_subnet_group" {
  name       = "TournaMaths-DB-Subnet-Group"
  subnet_ids = [aws_subnet.public_subnet.id]

  tags = {
    Name = "TournaMaths-DB-Subnet-Group"
  }
}
