################ Allow Codedeploy to interact with EC2 (being a bit loose with permissions as easier and this is a pet project)
resource "aws_iam_role" "codedeploy_role" {
  name = "codedeploy-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "codedeploy.amazonaws.com"
        },
      },
    ],
  })
}

resource "aws_iam_role_policy_attachment" "codedeploy_attachment" {
  role       = aws_iam_role.codedeploy_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
}

################ Allow EC2 to interact with Codedeploy (being a bit loose with permissions as easier and this is a pet project)
resource "aws_iam_role" "ec2_role" {
  name = "ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      },
    ],
  })
}

resource "aws_iam_role_policy_attachment" "codedeploy_ec2_attach" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2RoleforAWSCodeDeploy"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "ec2-profile"
  role = aws_iam_role.ec2_role.name
}

################ Allow EC2 to interact with S3
resource "aws_iam_policy" "s3_read_access" {
  name = "s3-read-access"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = [
          "s3:GetObject",
          "s3:ListBucket"
        ],
        Effect = "Allow",
        Resource = [
          "arn:aws:s3:::tournamaths",
          "arn:aws:s3:::tournamaths/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "s3_read_access_attach" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.s3_read_access.arn
}

################ Cloudwatch
resource "aws_iam_policy" "cloudwatch_agent_policy" {
  name        = "CloudWatchAgentPolicy"
  description = "Policy to allow EC2 to send logs and metrics to CloudWatch"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "cloudwatch:PutMetricData",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "logs:DescribeLogGroups",
          "logs:CreateLogStream",
          "logs:CreateLogGroup"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cloudwatch_agent_attach" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.cloudwatch_agent_policy.arn
}

################ EC2 password permissions - needs to access SecretsManager
resource "aws_iam_policy" "secrets_manager_policy" {
  name = "secrets_manager_policy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = "secretsmanager:GetSecretValue",
        Resource = aws_secretsmanager_secret.ec2_password.arn
      },
      {
        Effect   = "Allow",
        Action   = "secretsmanager:GetSecretValue",
        Resource = data.aws_secretsmanager_secret.db_creds_secret.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "secrets_manager_policy_attach" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.secrets_manager_policy.arn
}

################ Allow EC2 to describe DB
resource "aws_iam_policy" "ec2_examining_db_policy" {
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : "rds:DescribeDBInstances",
        "Resource" : aws_db_instance.tourna_math_db.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ec2_examining_db_policy_attach" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.ec2_examining_db_policy.arn
}
