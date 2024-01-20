# Setup Web Application Firewall - this can rate-limit per-IP address calling our appliation
# Not gathering metrics since this is a pet project and don't want to spend too muchy
# Could also perform bot control and fraud control: https://aws.amazon.com/waf/pricing/
# Can turn on CAPTCHA here too in future - not turning it on as if was DDOSed it could be quite expensive

# https://stackoverflow.com/questions/65252674/when-using-terraform-with-aws-how-can-i-set-a-rate-limit-on-a-specific-uri-path
resource "aws_wafv2_web_acl" "tournamaths_waf_web_acl" {
  name  = "tournamaths-waf-web-acl"
  scope = "REGIONAL" # or CLOUDFRONT for CloudFront distributions

  default_action {
    allow {}
  }

  rule {
    name     = "RateLimit"
    priority = 1

    action {
      block {}
    }

    statement {
      or_statement {
        statement {
          rate_based_statement {
            limit              = 15 # Limit of requests for a 5-minute time span for login endpoint
            aggregate_key_type = "IP"

            scope_down_statement {
              byte_match_statement {
                field_to_match {
                  uri_path {}
                }
                positional_constraint = "STARTS_WITH"
                search_string         = "/process_login"
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
          }
        }
        statement {
          rate_based_statement {
            limit              = 15 # Limit of requests for a 5-minute time span for registration endpoint
            aggregate_key_type = "IP"

            scope_down_statement {
              byte_match_statement {
                field_to_match {
                  uri_path {}
                }
                positional_constraint = "STARTS_WITH"
                search_string         = "/register"
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
          }
        }
        statement {
          rate_based_statement {
            limit              = 100 # Limit of requests for a 5-minute time span in general
            aggregate_key_type = "IP"
          }
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = false
      metric_name                = "RateLimitForLogin"
      sampled_requests_enabled   = false
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = false
    metric_name                = "tournamaths-waf-web-acl"
    sampled_requests_enabled   = false
  }
}

resource "aws_wafv2_web_acl_association" "tournamaths-waf-alb-association" {
  resource_arn = aws_lb.tournamaths_alb.arn
  web_acl_arn  = aws_wafv2_web_acl.tournamaths_waf_web_acl.arn
}
