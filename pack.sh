#!/bin/bash

set -eax

DATE=`date +"%Y%M%d%H%M%S"`

tar -cvvf cloudatlas_kz306486_kp306410_$DATE.tar.gz src build.xml *.policy agent.sh nodeclient.sh webclient.sh querysigner.sh README lib config web templates 
