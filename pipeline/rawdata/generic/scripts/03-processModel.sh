#-------------------------------------------------------------------------------
# 03-processModel.sh
#
# This script segment is a template for processing model-related files.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------
destTop="$processedTop"
for dir in  \
    dart/redmine-2335/didymos/ideal-impact4-ra-20210211-v01 \
    dart/redmine-2361/didymos/ideal-impact4-20200629-v01/draco \
    dart/redmine-2337/dimorphos/ideal-impact4-ra-20210211-v01 \
    dart/redmine-2361/dimorphos/ideal-impact4-20200629-v01/draco \
    dart/redmine-2335/dart/leia/ideal-impact4-ra-20210211-v01 \
    dart/redmine-2335/dart/luke/ideal-impact4-ra-20210211-v01 \
    dart/redmine-2361/spice/dart/ideal-impacts-20200629-v01 \
    ; do
  areaToUpdate="$pipelineProcessed/$dir"
  updateTop=`echo $dir | sed 's:.*redmine-..../::' | sed 's:4-20200629:4-ra-20210211:'`
  syncDir "$areaToUpdate" "$destTop/$updateTop"
done

# Need to handle DRACO images slightly differently, without renaming.
for dir in  \
    dart/redmine-2361/dart/draco/ideal-impact4-20200629-v01 \
    ; do
  areaToUpdate="$pipelineProcessed/$dir"
  updateTop=`echo $dir | sed 's:.*redmine-..../::'`
  syncDir "$areaToUpdate" "$destTop/$updateTop"
done
#-------------------------------------------------------------------------------
