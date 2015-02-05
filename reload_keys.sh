#!/bin/bash

set -eax

rm -rf base
mkdir base

./ca.sh --create-zone /
./ca.sh --create-zone /a
./ca.sh --create-zone /a/b
./ca.sh --create-zone /a/b/c

./ca.sh --create-certificate /a/b/c

./ca.sh --create-cc /a/b/c
cp base/a/b/c/singletonZoneAuthentication ~/.cloudatlas
