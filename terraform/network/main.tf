# VPC and subnets
resource "aws_vpc" "ruined-vpc" {
  cidr_block = "10.0.0.0/16"
  instance_tenancy = "default"
  tags = {
    Name = "ruined-vpc"
  }
}

resource "aws_subnet" "ruined-subnet-a" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.0.0/24"
  availability_zone = "us-east-2a"
  tags = {
    Name = "ruined-subnet-a"
  }
}

resource "aws_subnet" "ruined-subnet-b" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.1.0/24"
  availability_zone = "us-east-2b"
  tags = {
    Name = "ruined-subnet-b"
  }
}

resource "aws_subnet" "ruined-subnet-c" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.2.0/24"
  availability_zone = "us-east-2c"
  tags = {
    Name = "ruined-subnet-c"
  }
}

resource "aws_subnet" "ruined-subnet-priv-a" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.3.0/24"
  availability_zone = "us-east-2a"
  tags = {
    Name = "ruined-subnet-priv-a"
  }
}

resource "aws_subnet" "ruined-subnet-priv-b" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.4.0/24"
  availability_zone = "us-east-2b"
  tags = {
    Name = "ruined-subnet-priv-b"
  }
}

resource "aws_subnet" "ruined-subnet-priv-c" {
  vpc_id = aws_vpc.ruined-vpc.id
  cidr_block = "10.0.5.0/24"
  availability_zone = "us-east-2c"
  tags = {
    Name = "ruined-subnet-priv-c"
  }
}

# Internet gateway for VPC
resource "aws_internet_gateway" "ruined-igw" {
  vpc_id = aws_vpc.ruined-vpc.id
  tags = {
    Name = "ruined-igw"
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
    Name = "ruined-route-table"
  }
}

resource "aws_route_table_association" "ruined-association-a" {
  subnet_id = aws_subnet.ruined-subnet-a.id
  route_table_id = aws_route_table.ruined-route-table.id
}

resource "aws_route_table_association" "ruined-association-b" {
  subnet_id = aws_subnet.ruined-subnet-b.id
  route_table_id = aws_route_table.ruined-route-table.id
}

resource "aws_route_table_association" "ruined-association-c" {
  subnet_id = aws_subnet.ruined-subnet-c.id
  route_table_id = aws_route_table.ruined-route-table.id
}