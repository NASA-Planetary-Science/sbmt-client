#!/bin/sh

FC_DIR=/project/dawn/daily/fc

rm -rf $FC_DIR/FITS-links
mkdir $FC_DIR/FITS-links

find /disks/d0251/project/dawn/data -path "*fc*vesta*FC*.FIT" -or -path "*fc*vesta*FC*.LBL" | \
    grep --invert-match 'old_versions' | \
    grep --invert-match '/delme/' | \
    xargs ln -s -t $FC_DIR/FITS-links

mkdir -p $FC_DIR/FITS
rm -rf $FC_DIR/FITS-tmp
mv $FC_DIR/FITS $FC_DIR/FITS-tmp
mv $FC_DIR/FITS-links $FC_DIR/FITS
rm -rf $FC_DIR/FITS-tmp
