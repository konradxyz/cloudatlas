#!/bin/bash

set -eax
rm -rf ~/.cloudatlas
cp -rf config ~/.cloudatlas
sed -i s@/example/zone/name@$1@ ~/.cloudatlas/agent.ini
