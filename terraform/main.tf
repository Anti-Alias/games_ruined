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

resource "aws_instance" "simple-http" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  user_data     = <<-EOF
                #!/bin/bash
                echo "Hello, World" > index.html
                nohup busybox httpd -f -p 80 &
                EOF
  tags = {
    Name = "simple-http"
  }
  vpc_security_group_ids = [
    aws_security_group.http-sg.id,
    aws_security_group.ssh.id
  ]
  key_name = "http-key"
}

resource "aws_security_group" "ssh" {
  name = "ssh"
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_cidr_block]
  }
}

resource "aws_security_group" "http-sg" {
  name = "http-sg"
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_key_pair" "http-key" {
  key_name   = "http-key"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDlUC/Mf1RJW5txLAModsocsVcclraLQ5T0M6rOgXWVTrimvDAmomtQhkMAQi5GY6Hvkl4b777pn5EK+FMH+Hmj5ww2Pkamq1V0IuldVApfMqLDKxMKxxQ9heKyqDrv9iEFuBexMGZw0cE79Ki8Br0SOrahxIurdJVbrANeSGP6MQ82y34UaTGnNZYKe1hSIJJQ158nT64NCQMd+j72bS8Ap8HaezOn/AfzVOkgWkUeCvtj1y+/qY7S1O6xHx70ScI9Kj+Ex1lZwfFYG0NeFTt3CeZn4yqbAlfE43z2KmifiOdP128PMhbeW7ArqpBLHy0pCD6zFaSUHj1Gux9RBZyv william@william-desktop"
}