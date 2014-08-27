#!/bin/bash

# Usage: icq2plt.sh <path-to-ICQ-file>

prev_dir=`pwd`
cd `dirname $0`
dumber=`pwd`/dumber
shape2plates=`pwd`/shape2plates
cd $prev_dir

shapefile=$1
cp $1 SHAPE512.TXT

echo -e "SHAPE512.TXT\nSHAPE256.TXT\n2" | $dumber
echo -e "SHAPE512.TXT\nSHAPE128.TXT\n4" | $dumber
echo -e "SHAPE512.TXT\nSHAPE64.TXT\n8" | $dumber

echo -e "SHAPE512.TXT\nSHAPE512.PLT" | $shape2plates
echo -e "SHAPE256.TXT\nSHAPE256.PLT" | $shape2plates
echo -e "SHAPE128.TXT\nSHAPE128.PLT" | $shape2plates
echo -e "SHAPE64.TXT\nSHAPE64.PLT" | $shape2plates
