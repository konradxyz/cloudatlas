#1/bin/bash
set -eax
cd ..
rm -rf fake_ca
mkdir fake_ca
./ca.sh fake_ca --create-zone /
./ca.sh fake_ca --create-zone /uw
./ca.sh fake_ca --create-zone /uw/04

./ca.sh fake_ca --create-certificate /uw/04
cp fake_ca/uw/04/singletonZoneAuthentication certs/uw04_fake


ssh orange05 "cd V/SR/cloudatlas; ./demo/run_machine.sh demo/deploy_configs/uw_04_fake.ini"



