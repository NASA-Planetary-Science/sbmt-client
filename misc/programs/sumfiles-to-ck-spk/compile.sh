#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice installation!
SPICE_DIR=/project/nearsdc/software/spice/cspice

g++ process_sumfiles.cpp -ansi -O2 -pedantic -Wall -Wextra -I $SPICE_DIR/include $SPICE_DIR/lib/cspice.a -o process_sumfiles
