#!/bin/sh

ROOT=ola-pipeline-1.0.0
rm -rf $ROOT
mkdir $ROOT
mkdir $ROOT/src

cp README.txt $ROOT
cp src/compile.sh src/*.c src/*.h src/*.py $ROOT/src

SPICE=$ROOT/data/SPICE/Kernels

mkdir -p $ROOT/data/SciData/OLA/2020/202/L0
mkdir -p $SPICE

cp -RL data/spice/Kernels/* $SPICE
rm -f $SPICE/FK/orx_nadir.tf-* $SPICE/CK/orx_ola_* $SPICE/spice.tar.gz $SPICE/kernels.txt

cp data/SciData/OLA/2020/202/L0/orx_ola_scil0_t00002_200720.tab $ROOT/data/SciData/OLA/2020/202/L0
cp data/SciData/OLA/level0-filelist.txt $ROOT/data/SciData/OLA/
cp data/SPICE/spice-kernels.mk $ROOT/data/SPICE

rm -f $ROOT.tar.gz
tar czf $ROOT.tar.gz $ROOT
