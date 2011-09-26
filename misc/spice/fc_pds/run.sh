#!/bin/bash

# Script for running the create_info_files program
cd `dirname $0`
DIR=`pwd`

rm -rf /project/nearsdc/data-apl/VESTA/FC/infofiles/
mkdir -p /project/nearsdc/data-apl/VESTA/FC/infofiles
cd /project/nearsdc/data-apl/VESTA/FC/infofiles
$DIR/create_info_files $DIR/kernels.txt /project/nearsdc/data-apl/VESTA/FC/dawn_searchresults.csv
