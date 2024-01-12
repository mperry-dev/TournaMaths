################ Database.
# We're not doing backups here, deletion protection, SSL communication or encryption at rest, as just a pet project and keeping things simple.
resource "aws_db_instance" "tournamaths_db" {
  identifier                  = "tournamath-db"
  allocated_storage           = 20
  storage_type                = "gp2"
  engine                      = "postgres"
  engine_version              = "16.1"
  instance_class              = "db.t4g.micro"
  db_name                     = "tournamaths_db"
  username                    = "admin_user" # Can't say "admin" here as that's reserved in Postgres.
  manage_master_user_password = true
  parameter_group_name        = "default.postgres16"
  skip_final_snapshot         = true
  vpc_security_group_ids      = [aws_security_group.tournamaths_rds_sg.id]
  db_subnet_group_name        = aws_db_subnet_group.tournamaths_private_db_subnet_group.name

  storage_encrypted  = true
  ca_cert_identifier = "rds-ca-rsa2048-g1"

  apply_immediately = true # For convenience changes applied immediately, but should be careful
}
