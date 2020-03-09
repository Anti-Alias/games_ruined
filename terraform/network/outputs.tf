output "ruined_vpc_id" {
    value = aws_vpc.ruined-vpc.id
}

output "ruined_subnet_id" {
    value = aws_subnet.ruined-subnet.id
}

output "ruined_private_subnet_id" {
    value = aws_subnet.ruined-private-subnet.id
}