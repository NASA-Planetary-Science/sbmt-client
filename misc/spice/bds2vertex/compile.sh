#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice
# installation! Must be the alpha_dsk_c version.
SPICE_DIR=/project/nearsdc/software/spice/alpha_dsk_c

gcc bds2vertex.c -Wall -I $SPICE_DIR/include -I $SPICE_DIR/src/dsklib_c $SPICE_DIR/lib/dsklib_c.a $SPICE_DIR/lib/cspice.a -o bds2vertex
