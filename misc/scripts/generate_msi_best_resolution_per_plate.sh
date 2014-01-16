#!/bin/bash

cd `dirname $0`

MSI_DIR=/project/nearsdc/data/MSI
MSI_FIT_FILES=/project/nearsdc/data/internal/allMsiFiles.txt

# Create a list of all fit files
find -L $MSI_DIR -name "*.FIT" -type f | sort > $MSI_FIT_FILES


./run_java_program.sh edu.jhuapl.near.server.MSIBestResolutionPerPlate $MSI_FIT_FILES 0 > msi-best-resolution-per-plate.log 2>&1
