#!/bin/sh
sudo docker run -e "SPRING_PROFILES_ACTIVE=homologation" -p8080:8080 purchase-control/api