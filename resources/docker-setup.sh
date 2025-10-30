#!/bin/sh

#echo "install zabbix"
#wget -O - http://zabbix.repo.timeweb.ru/zabbix-install.sh | bash

#echo "install qemu"
#apt update && apt install qemu-guest-agent -y


echo "installing docker"
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do apt-get remove $pkg; done


apt-get update -y
apt-get install -y ca-certificates curl
install -y -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update -y


apt-get -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin clickhouse-client

ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/id_rsa > /dev/null 2>&1
sudo adduser --disabled-password --gecos "" jenkins && sudo usermod -aG docker jenkins
