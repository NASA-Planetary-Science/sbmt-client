#!/bin/bash

cd `dirname $0`

# the name of the tree (i.e. default or preliminary)
NAME=$1
# start date of tree
START=$2
# stop date of tree
STOP=$3

# make directory to store tree and empty if it already exists
TREEDIR=/project/sbmt2/prod/bennu/shared/ola/search/trees/$NAME
mkdir $TREEDIR
rm -rf $TREEDIR/*

# generate the hypertree
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeGenerator /project/sbmt2/prod/bennu/shared/ola/dataDirList.txt $TREEDIR OLA $START $STOP

#condense the hypertree into a bounds file .spectra
./run-on-linux.sh edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeCondensor $TREEDIR



