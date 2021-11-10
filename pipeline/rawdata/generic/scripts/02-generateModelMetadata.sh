#-------------------------------------------------------------------------------
# 02-generateModelMetadata.sh
#
# This script segment is a template for generating complete sets of model
# metadata. This segment is necessary if adding a new model, adding new
# types of data to an existing model, or generating image databases.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Set environment variables.
#-------------------------------------------------------------------------------
# 
metadataTop="$processedTop"
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Generate metadata.
#-------------------------------------------------------------------------------
generateModelMetadata $metadataTop
#-------------------------------------------------------------------------------
