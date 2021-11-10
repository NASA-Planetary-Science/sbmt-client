#-------------------------------------------------------------------------------
# 06-clean.sh
#
# This script segment is a template for cleaning up byproducts of the
# processing that need not be kept or deployed.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------
rm -f $processedTop/*/*/*/aamanifest.txt
rm -f $processedTop/*/*/*/missing-info.txt
#-------------------------------------------------------------------------------
