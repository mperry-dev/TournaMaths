################ CodeDeploy app
resource "aws_codedeploy_app" "tournamaths_app" {
  name = "tournamaths-app"
}

################ CodeDeploy deployment group
resource "aws_codedeploy_deployment_group" "tournamaths_deployment_group" {
  app_name              = aws_codedeploy_app.tournamaths_app.name
  deployment_group_name = "tournamaths-deployment-group"
  service_role_arn      = aws_iam_role.codedeploy_role.arn

  autoscaling_groups = [aws_autoscaling_group.tourna_math_asg.name]

  # Load Balancer Integration
  load_balancer_info {
    target_group_info {
      name = aws_lb_target_group.tourna_math_tg.name
    }
  }

  deployment_config_name = "CodeDeployDefault.OneAtATime"
}
