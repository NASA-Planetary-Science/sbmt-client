#!/bin/bash

cd `dirname $0`

TREEDIR=/project/sbmt2/sbmt/data/servers/multi-mission/test/ryugu/shared/laser/search/hypertree

mkdir $TREEDIR
rm -rf $TREEDIR/*
# generate the hypertree
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator /project/sbmt2/sbmt/data/bodies/ryugu/shared/lidar/browse/dataDirList.txt $TREEDIR 1 32 -1 LASER

#condense the hypertree into a bounds file .spectra
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.laser.Hayabusa2LaserFSHyperTreeCondenser $TREEDIR



