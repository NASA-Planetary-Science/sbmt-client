#!/bin/bash

cd `dirname $0`

# generate the bounding boxes and write to bounding box file
./run_java_program.sh edu.jhuapl.sbmt.model.spectrum.hypertree.SpectrumBoundsCalculator /project/sbmt2/sbmt/data/servers/multi-mission/test/earth/osirisrex/otes/l2/otes_l2_bounds.txt /earth/osirisrex/otes/l2 OTES > calculateBounds.log

# generate the hypertree
./run_java_program.sh edu.jhuapl.sbmt.boundedobject.hyperoctree.BoundedObjectHyperTreeGenerator /earth/osirisrex/otes/l2/otes_l2_bounds.txt /earth/osirisrex/otes/l2/hypertree/ 25 -6378.13720703125 6378.13720703125 -6378.13720703125 6378.13720703125 -6356.75244140625 6356.75244140625 > generateTree.log

#condense the hypertree into a bounds file .spectra
./run_java_program.sh edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeCondensor /earth/osirisrex/otes/l2/hypertree/ > condenseTree.log