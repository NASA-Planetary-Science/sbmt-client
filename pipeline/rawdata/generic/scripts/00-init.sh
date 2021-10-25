#-------------------------------------------------------------------------------
# 00-init.sh
#
# This script segment is a template that is required for initializing any/all
# SBMT data processing operations.
#
# Tailor numbered script segments to perform whatever processing steps are
# needed for this particular operation. Only include required segments and
# any optional segments you need. Invoke:
#
# $SBMTROOT/pipeline/rawdata/generic/runDataProcessing.sh
#
# with no arguments to run all numbered scripts in the current directory in
# numerical/lexical order.
#
# See runDataProcessing.sh for full documentation about how processing works,
# what environment variables are defined, assumptions made, etc.
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Set environment variables.
#-------------------------------------------------------------------------------
# The identifier of this processing run, typically "redmine-XXXX". No spaces.
processingId="redmine-2362"

#
#-------------------------------------------------------------------------------
# Do not remove or comment out this block. It prevents direct invocation,
# which would malfunction due to an incomplete environment/toolkit.
# These import scripts are designed to be run by runDataProcessing.sh.
#-------------------------------------------------------------------------------
if test "$invokedByRunner" != true; then
  check 1 "This script must be invoked by runDataProcessing.sh"
fi
#-------------------------------------------------------------------------------
