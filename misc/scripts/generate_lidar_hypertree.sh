#!/bin/bash

cd `dirname $0`

# directory where the data is stored
DATADIR=$1
# directory to create the tree
TREEDIR=$2
# OLA, LASER, etc
INSTRUMENT=$3

mkdir $TREEDIR
rm -rf $TREEDIR/*
# generate the hypertree
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator $DATADIR $TREEDIR 1 32 -1 $INSTRUMENT

#condense the hypertree into a bounds file .spectra
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2.Hayabusa2HyperTreeCondenser $TREEDIR



