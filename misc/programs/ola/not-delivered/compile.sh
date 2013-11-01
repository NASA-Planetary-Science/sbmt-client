#!/bin/sh

# To compile, you will need the C SPICE library available from
# http://naif.jpl.nasa.gov. Download and install this library. Then
# the following commands can be used to compile the programs.  You will
# need to change the SPICE_DIR variable in this script to the
# location where you installed SPICE.


# Change to location where SPICE is installed
SPICE_DIR=/project/nearsdc/software/spice/cspice

gcc -o ola-generate-nadir-ck ola-generate-nadir-ck.c -O2 -std=c99 -pedantic -Wall -Wextra \
    -I../src -I$SPICE_DIR/include $SPICE_DIR/lib/cspice.a -lm
gcc -o ola-test-level2 ola-test-level2.c -O2 -std=c99 -pedantic -Wall -Wextra \
    -I../src -I$SPICE_DIR/include $SPICE_DIR/lib/cspice.a -lm
gcc -o ola-test-level1 ola-test-level1.c -O2 -std=c99 -pedantic -Wall -Wextra \
    -I../src -I$SPICE_DIR/include $SPICE_DIR/lib/cspice.a -lm
