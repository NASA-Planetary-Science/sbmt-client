#-------------------------------------------------------------------------------
# 07-deploy.sh
#
# This script segment is a template for deploying all processed products.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Set environment variables.
#-------------------------------------------------------------------------------
srcTop="$processedTop"
destTop="$deployedTop"
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
skipSection="true"
for modelId in \
  "ideal-impact4-ra-20210211-v01" \
  ; do

  for dir in \
      dart/leia/$modelId \
      dart/luke/$modelId \
      didymos/$modelId \
      dimorphos/$modelId \
  ; do

    copyDir "$dir" "$dir-$processingId"
    updateRelativeLink "$destTop/$dir-$processingId" "$destTop/$dir" "$processingId"
    updateRelativeLink "$destTop/$dir"-$processingId "$serverTop/$dir" "$processingId"

  done
done

destTop="$serverTop"

skipSection="false"
for dir in \
  proprietary/allBodies-9.1 \
  published/allBodies-9.1 \
  ; do

    copyDir "$dir" "$dir-$processingId"
    updateRelativeLink "$destTop/$dir-$processingId" "$destTop/$dir" "$processingId"

done
#-------------------------------------------------------------------------------
