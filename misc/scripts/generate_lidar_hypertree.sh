#!/bin/bash

cd `dirname $0`

INSTRUMENT=$1
TYPE=$2
BODY=$3
MAX=$4

# generate the hypertree
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator /project/sbmt2/sbmt/data/bodies/ryugu/shared/lidar/browse/dataDirList.txt test_hypertree/ 1 32 -1 LASER

#condense the hypertree into a bounds file .spectra
./run-on-linux.sh edu.jhuapl.sbmt.model.lidar.hyperoctree.FSHyperTreeCondenser
