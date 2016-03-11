#!/bin/bash

# This script is meant to be run as a daily cron job. It checks to see
# if there are any new New Horizons images and makes them available to
# the SBMT.

set -e
set -o pipefail
export JAVA_HOME=/project/nearsdc/software/java/x86_64/jdk1.6.0_35
export PATH=${JAVA_HOME}/bin:${PATH}
export PATH=/project/nearsdc/software/apache-ant/latest/bin:${PATH}

DIR=/project/nearsdc/nh-sbmt

# run git clone on kenny since that machine has a decent version of git
rm -rf $DIR/sbmt
ssh kenny "cd $DIR; git clone http://hardin:8080/scm/git/sbmt"

# Compile SBMT
cd $DIR/sbmt
./misc/scripts/build-sbmt-extras.sh

# Copy over any new LORRI fits data
mkdir -p /project/nearsdc/data/NEWHORIZONS/LORRI/images2
find /project/lorri/data/flight/level2/2015 -name "*fit" | xargs cp -au -t /project/nearsdc/data/NEWHORIZONS/LORRI/images2

# Generate the lorri info files
# Compile program for generating lorri infofiles
cd $DIR/sbmt/misc/programs/lorri_pds/
./compile.sh
./run-pluto.sh

# Compile program for generating mvic infofiles
###cd $DIR/sbmt/misc/programs/mvic_pds/
###./compile.sh
###./run-pluto.sh
