#!/bin/sh
rm -rf giganet-purchase-control-api
git clone -b feature/deploy-config https://lucashenrique99:31faf487fd1385596954718317520d1af4cdc246@github.com/lucashenrique99/giganet-purchase-control-api.git
sudo chmod 777 -R giganet-purchase-control-api/
cd giganet-purchase-control-api/purchase-api/
sudo ./mvnw install
sudo chmod 777 -R *
sudo docker build -t purchase-control/api .
sudo sh ./run.sh