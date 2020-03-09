module "instance" {
  source = "../instance"
  env = var.env
  ssh_cidr_block = var.ssh_cidr_block
  ruined_vpc_id = module.network.ruined_vpc_id
  ruined_subnet_id = module.network.ruined_subnet_id
  ruined_private_subnet_id = module.network.ruined_private_subnet_id
}

module "network" {
  source = "../network"
  env = var.env
}