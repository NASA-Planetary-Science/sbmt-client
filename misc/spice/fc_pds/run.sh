#!/bin/bash

# Script for running the create_info_files program

# For Mac
rm -rf /Users/kahneg1/projects/near/data/dawn/infofiles
mkdir -p /Users/kahneg1/projects/near/data/dawn/infofiles
cd /Users/kahneg1/projects/near/data/dawn/infofiles
/Volumes/Untitled/near/source/gitsvn/trunk/misc/spice/fc_pds/create_info_files \
    /Volumes/Untitled/near/source/gitsvn/trunk/misc/spice/fc_pds/kernels.txt \
    /Users/kahneg1/projects/near/data/dawn/dawn_searchresults.csv

# # For linux
# rm -rf /home/kahneg1/src/near/data/dawn/infofiles
# mkdir -p /home/kahneg1/src/near/data/dawn/infofiles
# cd /home/kahneg1/src/near/data/dawn/infofiles
# /media/KANGURU2.0/near/source/gitsvn/trunk/misc/spice/fc_pds/create_info_files \
#     /media/KANGURU2.0/near/source/gitsvn/trunk/misc/spice/fc_pds/kernels.txt \
#     /home/kahneg1/src/near/data/dawn/dawn_searchresults_1108180220.csv
