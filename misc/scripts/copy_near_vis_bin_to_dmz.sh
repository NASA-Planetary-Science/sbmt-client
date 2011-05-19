#!/bin/bash

# This program copies the compiled java bytecode to the dmz so it can be run there.

cd `dirname $0`

# Delete existing bytecode
ssh mirage rm -rf /project/nearsdc/src/near_vis/trunk/bin
scp -r ../../bin mirage:/project/nearsdc/src/near_vis/trunk/
