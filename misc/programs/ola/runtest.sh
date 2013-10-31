#!/bin/sh

./deploy.sh
cd ola-pipeline-1.0.0
cd src
./compile.sh
cp * ../data/exe/
cp /project/nearsdc/software/spice/cspice/exe/msopck ../data/exe/
cd ../data
../src/ola-pipeline.py . level0-filelist.txt spice-kernels.txt spice/lsk/naif0010.tls spice/sclk/ORX_SCLKSCET.00000.example.tsc spice/fk/orx_ola_v000.tf

cd ../../not-delivered
./compile.sh
cd -
../../not-delivered/ola-test-level2 level2/OLASCIL220194.TAB aaa-track.txt
