module "instance" {
  source = "./instance"
  ssh_cidr_block = var.ssh_cidr_block
}

module "network" {
  source = "./network"
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