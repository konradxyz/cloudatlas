#!/bin/bash

set -eax

cp agent.ini ~/agent.ini
sed -i s@__PATHNAME__@$1@ ~/agent.ini
