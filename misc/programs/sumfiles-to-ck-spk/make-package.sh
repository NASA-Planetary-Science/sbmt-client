#!/bin/sh

# Creates tar file of sumfile-to-ck-spk program for distribution.
# This script must be run within folder containing this script.

root_dir=/tmp/sumfiles-to-ck-spk
package_dir=sumfiles-to-ck-spk-3.0.0
tar_file=$package_dir.tar.gz

mkdir -p $root_dir/$package_dir

cp README.txt compile.sh kernels.txt msopcksetup mkspksetup process_sumfiles.cpp sumfilelist.txt $root_dir/$package_dir

cd $root_dir/

tar czf $tar_file $package_dir
