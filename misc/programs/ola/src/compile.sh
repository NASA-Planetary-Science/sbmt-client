#!/bin/sh

# To compile, you will need the C SPICE library available from
# http://naif.jpl.nasa.gov. Download and install this library. Then
# the following commands can be used to compile the programs.  You will
# need to change the SPICE_DIR variable in this script to the
# location where you installed SPICE.


# Change to location where SPICE is installed
SPICE_DIR=/project/nearsdc/software/spice/cspice

gcc -o ola-level0-to-level1 ola-level0-to-level1.c -O2 -std=c99 -pedantic -Wall -Wextra \
    -I$SPICE_DIR/include $SPICE_DIR/lib/cspice.a -lm
gcc -o ola-level1-to-ck ola-level1-to-ck.c -O2 -std=c99 -pedantic -Wall -Wextra \
    -I$SPICE_DIR/include $SPICE_DIR/lib/cspice.a -lm
gcc -o ola-level1-to-level2 ola-level1-to-level2.c -O2 -std=c99 -pedantic -Wall -Wextra \
    -I$SPICE_DIR/include $SPICE_DIR/lib/cspice.a -lm
