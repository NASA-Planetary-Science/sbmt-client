#-------------------------------------------------------------------------------
# 01-import.sh
#
# This script segment is a template for importing files from deliveries into
# rawdata and from rawdata into processed. This segment is usually necessary,
# but strictly speaking it is optional.
#
# See 00-init.sh for more info about how the numbered script segments work.
#-------------------------------------------------------------------------------
#
#-------------------------------------------------------------------------------
# Set environment variables.
#-------------------------------------------------------------------------------
# The full path to the delivery as provided by a scientist. This may
# or may not fully comply with all SBMT guidelines for layout and naming.
deliveryTop="/project/sbmtpipeline/deliveries-dart"
#-------------------------------------------------------------------------------


#-------------------------------------------------------------------------------
# Copy (rsync) delivery to rawdata.
#-------------------------------------------------------------------------------
srcTop="$deliveryTop"
destTop="$rawDataTop"

# Copy delivered files to rawdata. "copyDir ." copies all files. Edit/copy to
# copy only part(s) of what was delivered. Don't replace this with symbolic
# links; this copy permanently archives the data as delivered.
#
# SPICE: all ideal kernels and metakernels are the same, and all errors kernels
# and metakernels are the same. Just copy once for each distinct case.
copyDir "ideal_impact1-20200629-v01/spice/draco" "spice/dart/ideal-impacts-20200629-v01"
copyDir "ideal_impact1-20200629-v01/spice/leia" "spice/licia/ideal-impacts-20200629-v01"
copyDir "ideal_impact1-20200629-v01/spice/luke" "spice/licia/ideal-impacts-20200629-v01"
copyDir "errors_impact1-20200629-v01/spice/draco" "spice/dart/errors-impacts-20200629-v01"
copyDir "errors_impact1-20200629-v01/spice/leia" "spice/licia/errors-impacts-20200629-v01"
copyDir "errors_impact1-20200629-v01/spice/luke" "spice/licia/errors-impacts-20200629-v01"

# Images for DRACO labeled "didymos" and "dimorphos" are the same images. Just
# copy once for each distinct case.
# Ideal...
copyDir "ideal_impact1-20200629-v01/didymos/draco" "dart/draco/ideal-impact1-20200629-v01"
copyDir "ideal_impact2-20200629-v01/didymos/draco" "dart/draco/ideal-impact2-20200629-v01"
copyDir "ideal_impact3-20200629-v01/didymos/draco" "dart/draco/ideal-impact3-20200629-v01"
copyDir "ideal_impact4-20200629-v01/didymos/draco" "dart/draco/ideal-impact4-20200629-v01"
copyDir "ideal_impact5-20200629-v01/didymos/draco" "dart/draco/ideal-impact5-20200629-v01"
# Errors...
copyDir "errors_impact1-20200629-v01/didymos/draco" "dart/draco/errors-impact1-20200629-v01"
copyDir "errors_impact2-20200629-v01/didymos/draco" "dart/draco/errors-impact2-20200629-v01"
copyDir "errors_impact3-20200629-v01/didymos/draco" "dart/draco/errors-impact3-20200629-v01"
copyDir "errors_impact4-20200629-v01/didymos/draco" "dart/draco/errors-impact4-20200629-v01"
copyDir "errors_impact5-20200629-v01/didymos/draco" "dart/draco/errors-impact5-20200629-v01"

# Images for LEIA labeled "didymos" and "dimorphos" are the same images. Just
# copy once for each distinct case.
# Ideal...
copyDir "ideal_impact1-20200629-v01/didymos/leia" "dart/leia/ideal-impact1-20200629-v01"
copyDir "ideal_impact2-20200629-v01/didymos/leia" "dart/leia/ideal-impact2-20200629-v01"
copyDir "ideal_impact3-20200629-v01/didymos/leia" "dart/leia/ideal-impact3-20200629-v01"
copyDir "ideal_impact4-20200629-v01/didymos/leia" "dart/leia/ideal-impact4-20200629-v01"
copyDir "ideal_impact5-20200629-v01/didymos/leia" "dart/leia/ideal-impact5-20200629-v01"
# Errors...
copyDir "errors_impact1-20200629-v01/didymos/leia" "dart/leia/errors-impact1-20200629-v01"
copyDir "errors_impact2-20200629-v01/didymos/leia" "dart/leia/errors-impact2-20200629-v01"
copyDir "errors_impact3-20200629-v01/didymos/leia" "dart/leia/errors-impact3-20200629-v01"
copyDir "errors_impact4-20200629-v01/didymos/leia" "dart/leia/errors-impact4-20200629-v01"
copyDir "errors_impact5-20200629-v01/didymos/leia" "dart/leia/errors-impact5-20200629-v01"

# Images for LUKE labeled "didymos" and "dimorphos" are the same images. Just
# copy once for each distinct case.
# Ideal...
copyDir "ideal_impact1-20200629-v01/didymos/luke" "dart/luke/ideal-impact1-20200629-v01"
copyDir "ideal_impact2-20200629-v01/didymos/luke" "dart/luke/ideal-impact2-20200629-v01"
copyDir "ideal_impact3-20200629-v01/didymos/luke" "dart/luke/ideal-impact3-20200629-v01"
copyDir "ideal_impact4-20200629-v01/didymos/luke" "dart/luke/ideal-impact4-20200629-v01"
copyDir "ideal_impact5-20200629-v01/didymos/luke" "dart/luke/ideal-impact5-20200629-v01"
# Errors...
copyDir "errors_impact1-20200629-v01/didymos/luke" "dart/luke/errors-impact1-20200629-v01"
copyDir "errors_impact2-20200629-v01/didymos/luke" "dart/luke/errors-impact2-20200629-v01"
copyDir "errors_impact3-20200629-v01/didymos/luke" "dart/luke/errors-impact3-20200629-v01"
copyDir "errors_impact4-20200629-v01/didymos/luke" "dart/luke/errors-impact4-20200629-v01"
copyDir "errors_impact5-20200629-v01/didymos/luke" "dart/luke/errors-impact5-20200629-v01"
#-------------------------------------------------------------------------------


#-------------------------------------------------------------------------------
# Copy or link area(s) from rawdata (and/or elsewhere) to processed area.
#-------------------------------------------------------------------------------
srcTop="$rawDataTop"
destTop="$processedTop"

# Copy or link files from rawdata to processed. Creating links for all of
# outputTop links all files. Edit/copy this line to link parts of what was
# delivered.
syncDir "$srcTop/spice" "$destTop/spice"
syncDir "$srcTop/spice/licia" "$destTop/spice/licia-corrected"
syncDir "$srcTop/dart" "$destTop/dart"
