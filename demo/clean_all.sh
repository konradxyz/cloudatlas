#!/bin/bash

ssh orange09 "screen -ls | grep Detached | cut -d. -f1 | awk '{print $1}' | xargs kill"
ssh orange08 "screen -ls | grep Detached | cut -d. -f1 | awk '{print $1}' | xargs kill"
ssh orange07 "screen -ls | grep Detached | cut -d. -f1 | awk '{print $1}' | xargs kill"
ssh orange06 "screen -ls | grep Detached | cut -d. -f1 | awk '{print $1}' | xargs kill"
ssh orange04 "screen -ls | grep Detached | cut -d. -f1 | awk '{print $1}' | xargs kill"
ssh orange05 "screen -ls | grep Detached | cut -d. -f1 | awk '{print $1}' | xargs kill"
