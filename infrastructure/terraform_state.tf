terraform {
  backend "s3" {
    bucket = "tournamaths"
    key    = "tournamaths.tfstate"
    region = "us-east-1"
  }
}
