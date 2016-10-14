#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice
# installation! Must be at least SPICE version N0064.
SPICE_DIR=/project/nearsdc/software/spice/cspice/

g++ -Wall getTargetState.c getSpacecraftState.c timeHistory.c -I $SPICE_DIR/include $SPICE_DIR/lib/cspice.a -o timeHistory
