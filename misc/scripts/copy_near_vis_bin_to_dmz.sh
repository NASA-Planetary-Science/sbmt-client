#!/bin/bash

# This program copies the compiled java bytecode to the dmz so it can be run there.
# Needs to be run from the top level folder of the code repository

# Delete existing bytecode
ssh mirage rm -rf /project/nearsdc/src/near_vis/trunk/bin
scp -r ../../bin mirage:/project/nearsdc/src/near_vis/trunk/
