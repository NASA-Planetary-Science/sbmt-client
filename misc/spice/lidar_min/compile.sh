#!/bin/sh

# IMPORTANT!! Change the next line to the path of your c-spice
# installation! Must be at least SPICE version N0064.
SPICE_DIR=/project/nearsdc/software/spice/alpha_dsk_c
LBFGS_DIR=/project/nearsdc/software/liblbfgs/install

gcc -Wall -O2 lidar-min.c \
    -I$SPICE_DIR/include \
    -I$SPICE_DIR/src/dsklib_c \
    -I$LBFGS_DIR/include \
    -L$LBFGS_DIR/lib \
    $SPICE_DIR/lib/dsklib_c.a $SPICE_DIR/lib/cspice.a $SPICE_DIR/lib/csupport.a \
    -llbfgs -lm \
    -o lidar-min
