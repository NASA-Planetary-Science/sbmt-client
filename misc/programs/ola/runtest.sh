#!/bin/sh

./deploy.sh
cd ola-pipeline-1.0.0
cd src
./compile.sh
export PATH=`pwd`:/project/nearsdc/software/spice/cspice/exe:$PATH
cd ../data
ola-pipeline.py

cd ../../not-delivered
./compile.sh
cd -
../../not-delivered/ola-test-level2 SciData/OLA/2020/202/L2/orx_ola_scil2_t00002_200720_v001.tab aaa-track.txt
cp aaa-track.txt ~/
