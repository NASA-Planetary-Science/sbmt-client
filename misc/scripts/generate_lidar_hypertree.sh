#!/bin/bash

cd `dirname $0`

<<<<<<< HEAD
# directory where the data is stored
DATADIR=$1
# directory to create the tree
TREEDIR=$2
# OLA, LASER, etc
INSTRUMENT=$3
=======
#TREEDIR=/project/sbmt2/sbmt/data/servers/multi-mission/test/ryugu/shared/lidar/search/hypertree

# This is run during the raw2processed script, so this needs to get generated in the processed area
TREEDIR=/project/sbmtpipeline/processed/ryugu/latest/shared/lidar/search/hypertree

>>>>>>> refs/heads/sbmt1dev

mkdir $TREEDIR
rm -rf $TREEDIR/*
# generate the hypertree
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator $DATADIR $TREEDIR 1 32 -1 $INSTRUMENT

#condense the hypertree into a bounds file .spectra
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2.Hayabusa2HyperTreeCondenser $TREEDIR



