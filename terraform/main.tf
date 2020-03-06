provider "aws" {
    profile = "default"
    region = "us-east-2"
}

resource "aws_instance" "simple-http" {
    ami = "ami-0c55b159cbfafe1f0"
    instance_type = "t2.micro"
    user_data = <<-EOF
                "#!/bin/bash"
                echo "Hello, World" > index.html
                nohup busybox httpd -f -p 8080 &
                EOF
    tags = {
        Name = "simple-http"
    }
    vpc_security_group_ids = [
        "${aws_security_group.http-sg.id}",
        "${aws_security_group.ssh.id}"
    ]
}

resource "aws_security_group" "ssh" {
    name = "ssh"
    ingress {
        from_port = 22
        to_port = 22
        protocol = "tcp"
        cidr_blocks = []
    }
}

resource "aws_security_group" "http-sg" {
    name = "http-sg"
    ingress {
        from_port = 8080
        to_port = 8080
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }
}