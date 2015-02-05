#!/bin/bash

set -eax
cd ..

rm -rf client_fake
mkdir client_fake

./ca.sh client_fake --create-zone /
./ca.sh client_fake --create-zone /uw
./ca.sh client_fake --create-zone /uw/03

./ca.sh client_fake --create-cc /uw/03 cc/clientAuthenticationFake 



java -cp "$PWD/bin:$PWD/lib/*"  pl.edu.mimuw.cloudatlas.CCA.Main cc/clientAuthenticationFake localhost "&sum_fake" "SELECT sum(level + cardinality) AS num"

