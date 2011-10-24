#!/bin/sh

# IMPORTANT!! Change the next 2 lines to the path of your c-spice
# and lbfgs installation! Must be at least SPICE version N0064.
SPICE_DIR=/project/nearsdc/software/spice/alpha_dsk_c
LBFGS_DIR=/project/nearsdc/software/liblbfgs/install

#gcc -Wall -O2 lidar-min-rigid-translation.c \
#    -I$SPICE_DIR/include \
#    -I$SPICE_DIR/src/dsklib_c \
#    -I$LBFGS_DIR/include \
#    -L$LBFGS_DIR/lib \
#    $SPICE_DIR/lib/dsklib_c.a $SPICE_DIR/lib/cspice.a $SPICE_DIR/lib/csupport.a \
#    -llbfgs -lm \
#    -o lidar-min-rigid-translation

#gcc -Wall -O2 lidar-min-polynomial-fit.c optimize.c closest_point_dsk.c icp.c \
#    -I$SPICE_DIR/include \
#    -I$SPICE_DIR/src/dsklib_c \
#    -I$LBFGS_DIR/include \
#    -L$LBFGS_DIR/lib \
#    $SPICE_DIR/lib/dsklib_c.a $SPICE_DIR/lib/cspice.a $SPICE_DIR/lib/csupport.a \
#    $LBFGS_DIR/lib/liblbfgs.a -lm \
#    -o lidar-min-polynomial-fit

gcc -Wall -O3 lidar-min-icp.c optimize.c closest_point_dsk.c icp.c \
    -I$SPICE_DIR/include \
    -I$SPICE_DIR/src/dsklib_c \
    -I$LBFGS_DIR/include \
    -L$LBFGS_DIR/lib \
    $SPICE_DIR/lib/dsklib_c.a $SPICE_DIR/lib/cspice.a $SPICE_DIR/lib/csupport.a \
    $LBFGS_DIR/lib/liblbfgs.a -lm \
    -o lidar-min-icp
