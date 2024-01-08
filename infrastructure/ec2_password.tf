resource "aws_secretsmanager_secret" "ec2_password" {
  name = "ec2_password_secret"
}

resource "aws_secretsmanager_secret_version" "ec2_password_version" {
  secret_id     = aws_secretsmanager_secret.ec2_password.id
  secret_string = jsonencode({
    password = random_password.password.result
  })
}

resource "random_password" "password" {
  length  = 20
  special = true
}
