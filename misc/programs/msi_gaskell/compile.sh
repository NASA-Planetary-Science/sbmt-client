#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice installation!
SPICE_DIR=/home/kahneg1/programs/spice/c/cspice/

g++ process_sumfiles.cpp -I $SPICE_DIR/include $SPICE_DIR/lib/cspice.a -o process_sumfiles
