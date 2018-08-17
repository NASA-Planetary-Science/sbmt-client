#!/bin/bash

cd `dirname $0`

INSTRUMENT=$1
TYPE=$2
BODY=$3

# generate the bounding boxes and write to bounding box file
./run_java_program.sh edu.jhuapl.sbmt.model.spectrum.hypertree.SpectrumBoundsCalculator $INSTRUMENT $TYPE $BODY > calculateBounds.log

# generate the hypertree
./run_java_program.sh edu.jhuapl.sbmt.model.boundedobject.hyperoctree.BoundedObjectHyperTreeGenerator $INSTRUMENT $TYPE $BODY 25 TEST > generateTree.log

#condense the hypertree into a bounds file .spectra
./run_java_program.sh edu.jhuapl.sbmt.model.spectrum.hypertree.SpectrumHypertreeCondenser > condenseTree.log

# remove the temp files and copy the tree to actual paths
rm -f bounds_*.bounds

# move hypertree to actual location

