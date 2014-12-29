#!/bin/bash

set -eax

DATE=`date +"%Y%m%d%H%M%S"`

tar -jcvf cloudatlas_kz306486_kp306410_$DATE.tar.gz src build.xml *.policy agent.sh nodeclient.sh webclient.sh signer.sh README config web templates 
