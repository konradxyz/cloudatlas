#!/bin/bash

set -eax
cd ..

./cca.sh localhost "&sum2" "SELECT sum(level + cardinality) AS num"

