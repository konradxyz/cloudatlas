#!/bin/bash
PWD=/media/sf_repo
java -cp "$PWD/bin:$PWD/lib/*"  pl.edu.mimuw.cloudatlas.agent.Main ~/agent.ini
