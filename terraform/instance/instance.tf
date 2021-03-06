# EC2 Instances
resource "aws_instance" "simple-http" {
  subnet_id = var.ruined_subnet_id
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  user_data     = <<-EOF
                #!/bin/bash
                echo "Hello, World" > index.html
                nohup busybox httpd -f -p 80 &
                EOF
  tags = {
    Name = "${var.env}-simple-http"
  }
  vpc_security_group_ids = [
    aws_security_group.http-sg.id,
    aws_security_group.ssh.id
  ]
  key_name = aws_key_pair.http-key.key_name
}

resource "aws_security_group" "http-sg" {
  name = "${var.env}-http-sg"
  vpc_id = var.ruined_vpc_id
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Security groups
resource "aws_security_group" "ssh" {
  vpc_id = var.ruined_vpc_id
  name = "${var.env}-ssh"
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_cidr_block]
  }
}

# Key pairs
resource "aws_key_pair" "http-key" {
  key_name   = "${var.env}-http-key"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDlUC/Mf1RJW5txLAModsocsVcclraLQ5T0M6rOgXWVTrimvDAmomtQhkMAQi5GY6Hvkl4b777pn5EK+FMH+Hmj5ww2Pkamq1V0IuldVApfMqLDKxMKxxQ9heKyqDrv9iEFuBexMGZw0cE79Ki8Br0SOrahxIurdJVbrANeSGP6MQ82y34UaTGnNZYKe1hSIJJQ158nT64NCQMd+j72bS8Ap8HaezOn/AfzVOkgWkUeCvtj1y+/qY7S1O6xHx70ScI9Kj+Ex1lZwfFYG0NeFTt3CeZn4yqbAlfE43z2KmifiOdP128PMhbeW7ArqpBLHy0pCD6zFaSUHj1Gux9RBZyv william@william-desktop"
}