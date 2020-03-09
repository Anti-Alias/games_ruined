module "environment-dev" {
  source = "./environment"
  ssh_cidr_block = var.ssh_cidr_block
  env = "dev"
}

module "environment-stage" {
  source = "./environment"
  ssh_cidr_block = var.ssh_cidr_block
  env = "stage"
}

# Defining provider and state location
provider "aws" {
  profile = "default"
  region  = "us-east-2"
}

terraform {
  backend "s3" {
    bucket = "games-ruined-terraform-backend"
    key    = "state"
    region = "us-east-2"
  }
}