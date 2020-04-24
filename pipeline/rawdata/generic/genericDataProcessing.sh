#-------------------------------------------------------------------------------
# Instructions for processing SBMT deliveries
#-------------------------------------------------------------------------------
# Do not invoke this script directly! Edit it as necessary to work for a
# specific body/model/mission/data delivery, then pass the path to this
# script as an argument to runDataProcessing.sh, which will execute this
# script to process that delivery.
#
# The following variables will be properly set by runDataProcessing.sh
# before this script is run, so you may safely use them in this script:
#
#       rawDataTop: directory where this script is located, used as the
#                   the top of the "raw" area for this delivery.
#     processedTop: top of this corresponding processed area for this
#                   delivery. This is usually parallel to rawDataTop.
#      sbmtCodeTop: where sbmt and saavtk are checked out and built.
#
# Functions defined in dataProcessingFunctions.sh are available in this script.
# There's a convention that functions ending "AsNecessary" are safe to
# call if this script is executed multiple times because they will skip
# execution if the step was previously performed successfully. Similarly,
# all copy operations (functions named with the word "Copy") are performed
# with rsync, so these will not actually copy the files again if this
# script is executed multiple times.
# 
# Functions with the word "Optional" in the name indicate that the
# function will not throw an error if the operation cannot be completed,
# for example if this delivery does not include the item in question.
#
# Otherwise, this script will exit whenever an error is encountered so that
# the developer may correct the problem and then execute this script again.
#
# Copy the files runDataProcessing.sh and dataProcessingFunctions.sh into the
# same directory where this script is located, and run the copy of
# runDataProcessing.sh so that the processing scripts used are archived
# along with the rest of the delivery.
#
# Fill out the block below to describe the delivery and what you customized
# to make it work. Then update the remainder of the script as needed.
#-------------------------------------------------------------------------------
# Processing Info
#-------------------------------------------------------------------------------
# Developer: James Peachey
# Redmine issue #: 2107
# Notes:
#
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# This block must be updated for each delivery.
#-------------------------------------------------------------------------------

# This is the full path to the delivery as provided by a scientist. This may
# or may not fully comply with all SBMT guidelines for layout and naming.
deliveryTop="/project/sbmtpipeline/deliveries/didymosa/20200420/didymosA-DRA-v01A"

# This is the *relative* path to the top output directory as it will be
# defined in the raw, processed and deployed locations.
# Must be all lower case and contain no underscores or whitespace.
outputTopPath="didymosa/didymosa-dra-v01a"

# The identifier of the SBMT model. For a given body, this uniquely identifies
# the model. This may not include any whitespace. If no items being imported
# are associated with a specific model, this may be set to an empty string,
# but it should not be removed. This is used for processing plate colorings.
modelId="DidymosA-DRA-v01A"

# The identifier of the body as it appears in the SBMT. If no items being
# imported are associated with a specific body, this may be set to an
# empty string, but it should not be removed.
# This is used for processing plate colorings.
bodyId="65803 Didymos"

# Code branches used for SAAVTK/SBMT checkout. Many deliveries will work with
# any recent versions of these packages, so this frequently can just be the
# standard main development branches.
saavtkBranch="saavtk1dev-redmine-2107"
sbmtBranch="sbmt1dev-redmine-2107"
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Check out and build saavtk and sbmt. Comment these out if you don't need
# the SBMT client for this delivery. The SBMT client is used for processing
# plate colorings, and/or images.
checkoutCodeIfNecessary $sbmtCodeTop $saavtkBranch $sbmtBranch
buildCodeIfNecessary

#-------------------------------------------------------------------------------
# Delivery to raw data.
#-------------------------------------------------------------------------------
srcTop="$deliveryTop"
destTop="$rawDataTop"

# Copy any/all standard model files.
copyStandardModelFiles

# Copy and tailor this for each imager. Comment out if there are no imagers.
copyOptionalDir draco imaging/draco
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Raw data to processed.
#-------------------------------------------------------------------------------
srcTop="$rawDataTop"
destTop="$processedTop"

# Copy any/all standard model files.
copyStandardModelFiles

# Copy and tailor this for each imager. Comment out if there are no imagers.
copyOptionalDir draco imaging/draco

# Process plate colorings.
discoverPlateColorings
#-------------------------------------------------------------------------------

# Done.
