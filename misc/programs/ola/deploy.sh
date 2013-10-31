#!/bin/sh

ROOT=ola-pipeline-1.0.0
rm -rf $ROOT
mkdir $ROOT
mkdir $ROOT/src

cp README.txt $ROOT
cp src/compile.sh src/*.c src/*.h src/*.py $ROOT/src

mkdir $ROOT/data
mkdir $ROOT/data/exe
mkdir $ROOT/data/level0sci
mkdir $ROOT/data/level1sci
mkdir $ROOT/data/ck
mkdir $ROOT/data/level2
mkdir $ROOT/data/spice
mkdir $ROOT/data/tmp

cp -RL data/spice $ROOT/data
rm -f $ROOT/data/spice/fk/orx_nadir.tf-* $ROOT/data/spice/ck/OLA* $ROOT/data/spice/spice.tar.gz $ROOT/data/spice/kernels.txt

cp data/level0sci/OLASCIL020194.TAB $ROOT/data/level0sci
cp data/level0-filelist.txt $ROOT/data
cp data/spice-kernels.txt $ROOT/data

rm -f $ROOT.tar.gz
tar czf $ROOT.tar.gz $ROOT
