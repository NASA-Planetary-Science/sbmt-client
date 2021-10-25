#-------------------------------------------------------------------------------
# 08-generateDatabases.sh
#
# This script segment is a template for gnerating image databases.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------

# This symbolic link is needed because the database generator appends "/data"
# to the root URL, but this level of directory is not used in deliveries.
createRelativeLink $processedTop $processedTop/data

for modelId in \
  "ideal-impact4-ra-20210211-v01" \
  ; do

  for bodyId in \
    didymos \
    dimorphos \
    ; do

    instrument=draco
    generateDatabaseTable ${instrument^^} SPICE
  done
done
#-------------------------------------------------------------------------------
