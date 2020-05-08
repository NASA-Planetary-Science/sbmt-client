#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice
# installation! Must be at least SPICE version N0064.
SPICE_DIR=/project/sbmtpipeline/software/spice/cspice

g++ -g -Wall create_info_files.cpp getSpacecraftState.c getTargetState.c getFov.c -I $SPICE_DIR/include $SPICE_DIR/lib/cspice.a -o create_info_files
