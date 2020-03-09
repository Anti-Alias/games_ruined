# VPC and subnets
resource "aws_vpc" "ruined-vpc" {
  cidr_block = "10.0.0.0/16"
  instance_tenancy = "default"
  tags = {
    Name = "${var.env}-ruined-vpc"
  }
}

resource "aws_subnet" "ruined-subnet" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.0.0/24"
  availability_zone = "us-east-2a"
  tags = {
    Name = "${var.env}-ruined-subnet"
  }
}

resource "aws_subnet" "ruined-private-subnet" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.3.0/24"
  availability_zone = "us-east-2a"
  tags = {
    Name = "${var.env}-ruined-subnet-priv"
  }
}

# Internet gateway for VPC
resource "aws_internet_gateway" "ruined-igw" {
  vpc_id = aws_vpc.ruined-vpc.id
  tags = {
    Name = "${var.env}-ruined-igw"
  }
}

# Routing table
resource "aws_route_table" "ruined-route-table" {
  vpc_id = aws_vpc.ruined-vpc.id
  route {
    cidr_block = "0.0.0.0/32"
    gateway_id = aws_internet_gateway.ruined-igw.id
  }
  tags = {
    Name = "${var.env}-ruined-route-table"
  }
}

resource "aws_route_table_association" "ruined-association" {
  subnet_id = aws_subnet.ruined-subnet.id
  route_table_id = aws_route_table.ruined-route-table.id
}