################ VPC.
resource "aws_vpc" "tournamaths_vpc" {
  cidr_block = "10.0.0.0/16" # A private IP address range

  tags = {
    Name = "TournaMaths-VPC"
  }
}

################ Public subnets for the application's EC2 instances.
resource "aws_subnet" "tournamaths_public_subnet_1a" {
  vpc_id            = aws_vpc.tournamaths_vpc.id
  cidr_block        = "10.0.1.0/24" # A private IP address range
  availability_zone = "${var.aws_region}a"

  # Make public subnet
  map_public_ip_on_launch = true

  tags = {
    Name = "TournaMaths-Subnet-1a"
  }
}

resource "aws_subnet" "tournamaths_public_subnet_1b" {
  vpc_id            = aws_vpc.tournamaths_vpc.id
  cidr_block        = "10.0.2.0/24" # A private IP address range
  availability_zone = "${var.aws_region}b"

  # Make public subnet
  map_public_ip_on_launch = true

  tags = {
    Name = "TournaMaths-Subnet-1b"
  }
}

################ Private subnets for database, with a subnet group that the database will be restricted to.
resource "aws_subnet" "tournamaths_private_subnet_1a" {
  vpc_id            = aws_vpc.tournamaths_vpc.id
  cidr_block        = "10.0.3.0/24" # A private IP address range
  availability_zone = "${var.aws_region}a"

  tags = {
    Name = "TournaMaths-Private-Subnet-1a"
  }
}

resource "aws_subnet" "tournamaths_private_subnet_1b" {
  vpc_id            = aws_vpc.tournamaths_vpc.id
  cidr_block        = "10.0.4.0/24" # A private IP address range
  availability_zone = "${var.aws_region}b"

  tags = {
    Name = "TournaMaths-Private-Subnet-1b"
  }
}

resource "aws_db_subnet_group" "tournamaths_private_db_subnet_group" {
  name       = "tournamaths-private-db-subnet-group"
  subnet_ids = [aws_subnet.tournamaths_private_subnet_1a.id, aws_subnet.tournamaths_private_subnet_1b.id]
}

################ ACL for database private subnets - 2nd layer of security to block all traffic not coming into port 5432.

# Define the Network ACL
resource "aws_network_acl" "tournamaths_db_acl" {
  vpc_id = aws_vpc.tournamaths_vpc.id

  # Inbound rule to allow PostgreSQL (port 5432) only from VPC
  ingress {
    protocol   = "tcp"
    rule_no    = 100
    action     = "allow"
    cidr_block = aws_vpc.tournamaths_vpc.cidr_block
    from_port  = 5432
    to_port    = 5432
  }

  # Deny all other inbound traffic
  ingress {
    protocol   = "-1"
    rule_no    = 200
    action     = "deny"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535 # Range is all ports
  }

  # Allow only outbound traffic to VPC
  egress {
    protocol   = "tcp"
    rule_no    = 100
    action     = "allow"
    cidr_block = aws_vpc.tournamaths_vpc.cidr_block
    from_port  = 0
    to_port    = 65535 # Range is all ports
  }

  # Deny all other outbound traffic
  egress {
    protocol   = "-1"
    rule_no    = 200
    action     = "deny"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 65535 # Range is all ports
  }

  tags = {
    Name = "TournaMaths-DB-NACL"
  }
}

# Associate the Network ACL with the private subnets for the database
resource "aws_network_acl_association" "tournamaths_db_acl_association_1a" {
  network_acl_id = aws_network_acl.tournamaths_db_acl.id
  subnet_id      = aws_subnet.tournamaths_private_subnet_1a.id
}

resource "aws_network_acl_association" "tournamaths_db_acl_association_1b" {
  network_acl_id = aws_network_acl.tournamaths_db_acl.id
  subnet_id      = aws_subnet.tournamaths_private_subnet_1b.id
}

################ Internet gateway so EC2 instances can access internet.
resource "aws_internet_gateway" "tournamaths_igw" {
  vpc_id = aws_vpc.tournamaths_vpc.id

  tags = {
    Name = "TournaMaths-IGW"
  }
}

################ Route table with associations to subnets.
resource "aws_route_table" "tournamaths_route_table" {
  vpc_id = aws_vpc.tournamaths_vpc.id

  # Route outbound traffic from EC2 instances through internet gateway
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.tournamaths_igw.id
  }

  tags = {
    Name = "TournaMaths-Route-Table"
  }
}

# route_table_association to public route table means the linked subnets are public.
resource "aws_route_table_association" "tournamaths_route_table_association_1a" {
  subnet_id      = aws_subnet.tournamaths_public_subnet_1a.id
  route_table_id = aws_route_table.tournamaths_route_table.id
}

resource "aws_route_table_association" "tournamaths_route_table_association_1b" {
  subnet_id      = aws_subnet.tournamaths_public_subnet_1b.id
  route_table_id = aws_route_table.tournamaths_route_table.id
}

################ Security groups.
resource "aws_security_group" "tournamaths_alb_sg" {
  name   = "tournamath-alb-sg"
  vpc_id = aws_vpc.tournamaths_vpc.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.tournamaths_vpc.cidr_block] # Can't specify tournamaths_ec2_sg in security group list here as would have a cycle
  }

  tags = {
    Name = "TournaMaths-ALB-SG"
  }
}

resource "aws_security_group" "tournamaths_ec2_sg" {
  name   = "tournamath-ec2-sg"
  vpc_id = aws_vpc.tournamaths_vpc.id

  ingress {
    from_port       = 8080 # Port 8080 since Tomcat listening on port 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.tournamaths_alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1" # -1 means all protocols
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "TournaMaths-EC2-SG"
  }
}

resource "aws_security_group" "tournamaths_rds_sg" {
  name   = "tournamath-rds-sg"
  vpc_id = aws_vpc.tournamaths_vpc.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.tournamaths_ec2_sg.id]
  }

  # Don't really need to restrict traffic here, but leaving without actually means any outbound traffic is allowed - this feels clearer.
  # Restricting to just VPC since cannot implement deny rule in security group.
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.tournamaths_vpc.cidr_block]
  }

  tags = {
    Name = "TournaMaths-RDS-SG"
  }
}

################ ALB, Target-Group, Listener.
resource "aws_lb" "tournamaths_alb" {
  name               = "TournaMaths-ALB"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.tournamaths_alb_sg.id]
  subnets            = [aws_subnet.tournamaths_public_subnet_1a.id, aws_subnet.tournamaths_public_subnet_1b.id]

  enable_deletion_protection = false
}

resource "aws_lb_target_group" "tournamaths_tg" {
  name     = "TournaMaths-TG"
  port     = 8080 # Tomcat (with SpringBoot) listens on port 8080 by default
  protocol = "HTTP"
  vpc_id   = aws_vpc.tournamaths_vpc.id

  health_check {
    enabled  = true
    interval = 30
    path     = "/health_check"
    timeout  = 3
  }
}

resource "aws_lb_listener" "http_listener" {
  load_balancer_arn = aws_lb.tournamaths_alb.arn
  port              = 80
  protocol          = "HTTP"

  # HTTP listener just redirects HTTP requests to the corresponding HTTPS URL
  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "https_listener" {
  load_balancer_arn = aws_lb.tournamaths_alb.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = aws_acm_certificate.tournamaths_cert.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tournamaths_tg.arn
  }

  # Want this listener created after SSL certificate validated, otherwise can get this sort of error:
  # https://stackoverflow.com/questions/72227832/certificate-must-have-a-fully-qualified-domain-name-a-supported-signature-and
  depends_on = [
    aws_acm_certificate_validation.tournamaths_cert_validation
  ]
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
    name                   = aws_lb.tournamaths_alb.dns_name
    zone_id                = aws_lb.tournamaths_alb.zone_id
    evaluate_target_health = true
  }
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

# Because certificates have to be validated, might need to run Terraform multiple times if replace certificate
# https://stackoverflow.com/questions/72227832/certificate-must-have-a-fully-qualified-domain-name-a-supported-signature-and
resource "aws_acm_certificate" "tournamaths_cert" {
  domain_name       = "tournamaths.com"
  validation_method = "DNS"
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
