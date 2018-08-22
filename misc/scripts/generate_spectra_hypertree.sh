#!/bin/bash

cd `dirname $0`

INSTRUMENT=$1
TYPE=$2
BODY=$3
MAX=$4

# generate the bounding boxes and write to bounding box file
./run-on-linux.sh edu.jhuapl.sbmt.model.spectrum.hypertree.SpectrumBoundsCalculator $INSTRUMENT $TYPE $BODY > calculateBounds.log

# generate the hypertree
./run-on-linux.sh edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeGenerator $INSTRUMENT $TYPE $BODY $MAX TEST > generateTree.log

#condense the hypertree into a bounds file .spectra
./run-on-linux.sh edu.jhuapl.sbmt.model.spectrum.hypertree.SpectrumHypertreeCondenser > condenseTree.log

# remove the temp files and copy the tree to actual paths
rm -f bounds_*.bounds

# copy hypertree to actual location
INS=$(echo "$INSTRUMENT" | tr '[:upper:]' '[:lower:]')
TYP=$(echo "$TYPE" | tr '[:upper:]' '[:lower:]')
BOD=$(echo "$BODY" | tr '[:upper:]' '[:lower:]')
mkdir /project/sbmt2/sbmt/data/servers/multi-mission/test/$BOD/osirisrex/$INS/$TYP/hypertree
mv temp_hypertree/* /project/sbmt2/sbmt/data/servers/multi-mission/test/$BOD/osirisrex/$INS/$TYP/hypertree/
rm -rf temp_hypertree/
# add mission as argument? Won't always be osirisrex 
