#!/bin/bash

set -eax

screen -d -m ./demo/agent_with_config.sh $1 
screen -d -m ./demo/nodeclient.sh
sleep 1
screen -d -m ./demo/webclient.sh 
