#1/bin/bash


ssh orange09 "cd V/SR/cloudatlas; ./demo/run_machine.sh demo/deploy_configs/pjwstk_01.ini"

ssh orange08 "cd V/SR/cloudatlas; ./demo/run_machine.sh demo/deploy_configs/uw_01.ini"
ssh orange07 "cd V/SR/cloudatlas; ./demo/run_machine.sh demo/deploy_configs/pjwstk_02.ini"
ssh orange06 "cd V/SR/cloudatlas; ./demo/run_machine.sh demo/deploy_configs/uw_02.ini"
ssh orange04 "cd V/SR/cloudatlas; ./demo/run_machine.sh demo/deploy_configs/uw_03.ini"
