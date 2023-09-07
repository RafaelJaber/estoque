#!/bin/sh
sudo docker run -e "SPRING_PROFILES_ACTIVE=prod" -p8080:8080 purchase-control/api