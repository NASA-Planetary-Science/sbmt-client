#!/bin/sh

SPICE_DIR=/project/nearsdc/software/spice/cspice/

g++ -Wall sun_earth_pos.cpp -I $SPICE_DIR/include $SPICE_DIR/lib/cspice.a -o sun_earth_pos
