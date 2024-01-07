terraform {
  backend "s3" {
    bucket = "tournamaths"
    key    = "tournamaths.tfstate"
    region = var.aws_region
  }
}
