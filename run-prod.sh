#!/bin/sh
sudo docker run -e "SPRING_PROFILES_ACTIVE=prod" -p8080:8080 purchase-control/api


docker container run --name estoque_api --hostname estoqueapi.giganet.psi.br -e "SPRING_PROFILES_ACTIVE=prod" --network vlan1810 --ip 172.26.10.254 -v /etc/localtime:/etc/localtime:ro -v /etc/timezone:/etc/timezone:ro -d --restart always --cpus 4 --memory 1024mb rafaeljaber/docker-estoque:289e1c551745 && \