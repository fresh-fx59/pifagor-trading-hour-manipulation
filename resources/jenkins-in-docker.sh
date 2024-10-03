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


apt-get -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin


# install jenkins

mkdir docker-jenkins
cd docker-jenkins
docker network create jenkins


cat > docker-compose.yaml << EOF
name: jenkins-docker-project
services:
    jenkins:
        container_name: jenkins-docker
        privileged: true
        networks:
            jenkins:
                aliases:
                    - docker
        environment:
            - DOCKER_TLS_CERTDIR=/certs
        volumes:
            - jenkins-docker-certs:/certs/client
            - jenkins-data:/var/jenkins_home
        ports:
            - 2376:2376
        image: docker:dind
        command: --storage-driver overlay2
    myjenkins-blueocean:
        container_name: jenkins-blueocean
        restart: on-failure
        networks:
            - jenkins
        environment:
            - DOCKER_HOST=tcp://docker:2376
            - DOCKER_CERT_PATH=/certs/client
            - DOCKER_TLS_VERIFY=1
        ports:
            - 8080:8080
            - 50000:50000
        volumes:
            - jenkins-data:/var/jenkins_home
            - jenkins-docker-certs:/certs/client:ro
        image: myjenkins-blueocean:2.479-jdk17

networks:
    jenkins:
        external: true
        name: jenkins
volumes:
    jenkins-docker-certs:
        external: true
        name: jenkins-docker-certs
    jenkins-data:
        external: false
        name: jenkins-data
EOF

cat > Dockerfile << EOF
FROM jenkins/jenkins:2.479-jdk17
USER root
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc \
  https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) \
  signed-by=/usr/share/keyrings/docker-archive-keyring.asc] \
  https://download.docker.com/linux/debian \
  \$(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli
USER jenkins
RUN jenkins-plugin-cli --plugins "blueocean docker-workflow"
EOF

docker volume create jenkins-data
docker volume create jenkins-docker-certs

echo "building docker image"
docker build -t myjenkins-blueocean:2.479-jdk17 .

docker compose up -d