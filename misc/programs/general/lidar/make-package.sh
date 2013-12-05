#!/bin/sh

# Creates tar file of lidar-opt program for distribution.
# This script must be run within folder containing this script.

if [ "$(uname)" == "Darwin" ]; then
    os_suffix=macosx
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    os_suffix=linux
fi

root_dir=/tmp/lidar-opt
package_dir=lidar-opt-2.1.0-$os_suffix
tar_file=$package_dir.tar.gz

mkdir -p $root_dir/$package_dir

cp lidar-opt.py ../build/lidar-optimize-track ../build/lidar-compute-track-stats /project/nearsdc/spice-kernels/orex/LSK/naif0010.tls $root_dir/$package_dir
cp README-lidar-opt.txt $root_dir/$package_dir/README.txt
if [ "$(uname)" == "Darwin" ]; then
    cp ../../../scripts/convert-mapmaker-cube ../../../scripts/search-lidar $root_dir/$package_dir
fi

cd $root_dir/

gtar czf $tar_file $package_dir
