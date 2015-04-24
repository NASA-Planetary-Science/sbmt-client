#!/bin/sh

# Creates tar file of sumfile-to-ck program for distribution.
# This script must be run within folder containing this script.

root_dir=/tmp/sumfiles-to-ck
package_dir=sumfiles-to-ck-2.0.0
tar_file=$package_dir.tar.gz

mkdir -p $root_dir/$package_dir

cp README.txt compile.sh kernels.txt msopcksetup process_sumfiles.cpp sumfilelist.txt $root_dir/$package_dir

cd $root_dir/

tar czf $tar_file $package_dir
