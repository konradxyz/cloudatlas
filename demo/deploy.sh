#!/bin/bash

set -eax
cd ..

rm -rf base
mkdir base #todo: add proper command to ca - init?

./ca.sh --create-zone /
./ca.sh --create-zone /uw
./ca.sh --create-zone /uw/01
./ca.sh --create-zone /uw/02
./ca.sh --create-zone /uw/03

./ca.sh --create-zone /pjwstk
./ca.sh --create-zone /pjwstk/01
./ca.sh --create-zone /pjwstk/02

./ca.sh --create-certificate /uw/01
./ca.sh --create-certificate /uw/02
./ca.sh --create-certificate /uw/03


./ca.sh --create-certificate /pjwstk/01
./ca.sh --create-certificate /pjwstk/02


rm -rf certs
mkdir certs

cp base/uw/01/singletonZoneAuthentication certs/uw01
cp base/uw/02/singletonZoneAuthentication certs/uw02
cp base/uw/03/singletonZoneAuthentication certs/uw03

cp base/pjwstk/01/singletonZoneAuthentication certs/pjwstk01
cp base/pjwstk/02/singletonZoneAuthentication certs/pjwstk02
