#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice
# installation! Must be at least SPICE version N0064.
SPICE_DIR=/project/nearsdc/software/spice/cspice/
# SPICE_DIR=/Users/turnerj1/software/spice/cspice 

g++ -g -Wall create_info_files.cpp -I $SPICE_DIR/include $SPICE_DIR/lib/cspice.a -o create_info_files
