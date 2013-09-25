#!/bin/sh

# To compile, you will need the C SPICE library available from
# http://naif.jpl.nasa.gov. Download and install this library. Then
# the following command can be used to compile the program.  You will
# need to change the SPICE_DIR variable in this script to the
# location where you installed SPICE.


# Change to location where SPICE is installed
SPICE_DIR=/project/nearsdc/software/spice/cspice

gcc -o ola-level1-to-level2 ola-level1-to-level2.c -O2 -ansi -pedantic -Wall -Wextra \
    -I$SPICE_DIR/include $SPICE_DIR/lib/cspice.a -lm
