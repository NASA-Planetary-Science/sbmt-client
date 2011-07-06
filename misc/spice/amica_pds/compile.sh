#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice
# installation! Must be at least SPICE version N0064.
SPICE_DIR=/home/kahneg1/programs/spice/c/cspice/

g++ create_info_files.cpp -I $SPICE_DIR/include $SPICE_DIR/lib/cspice.a -o create_info_files
