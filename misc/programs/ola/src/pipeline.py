#!/usr/bin/python

# This program takes OLA level 0 data and generates OLA level 1 and
# level 2 data. It is assumed the following directory structure is
# setup where ROOT is the root directoyry structure passed in as a
# command line parameter to this file
#
# ROOT/level0sci - contains level 0 science files
# ROOT/level1sci - contains level 1 science files
# ROOT/spice - contains SPICE kernel file and related data
# ROOT/level2 - contains level 2 files
# ROOT/exe - contains executables called by this script
# ROOT/tmp - contains temperory files


import sys
import os


# folders used by the pipeline
rootDir = sys.argv[1]
level0SciDir = rootDir + "/level0sci/"
level1SciDir = rootDir + "/level1sci/"
spiceDir = rootDir + "/spice/"
level2Dir = rootDir + "/level2/"
exeDir = rootDir + "/exe/"
tmpDir = rootDir + "/tmp/"

# file paths used by the pipeline which are the same for all days
kernelFile = spiceDir + "kernels.txt"
lskKernelFile = spiceDir + "lsk/naif0010.tls"
sclkKernelFile = spiceDir + "sclk/ORX_SCLKSCET.00000.example.tsc"
framesKernelFile = spiceDir + "fk/orx_ola_v000.tf"

# Load the files we need to process and return them as a list
def loadFiles():
    infile = rootDir + "/level0-filelist.txt"
    fin = open(infile,'r')
    filelist = fin.readlines()
    return filelist


# Run the level 0 to level 1 to level 2 processing for a single level 0 file
def process(level0SciBaseFileName):
    # First, using the name of the level 0 file, construct the names of the other files we will be generating
    level0SciFile = level0SciDir + level0SciBaseFileName
    level1SciFile = level1SciDir + level0SciBaseFileName[0:7] + '1' + level0SciBaseFileName[8:]
    level1SciLabel = level1SciDir + level0SciBaseFileName[0:7] + '1' + level0SciBaseFileName[8:-4] + ".XML"
    msopckInputDataFile = tmpDir + "msopckInputData-" + level0SciBaseFileName[8:-4] + ".txt"
    msopckInputSetupFile = tmpDir + "msopckInputSetup-" + level0SciBaseFileName[8:-4] + ".txt"
    msopckOutputCkFile = spiceDir + "ck/OLA" + level0SciBaseFileName[8:-4] + ".bc"
    level2File = level2Dir + level0SciBaseFileName[0:7] + '2' + level0SciBaseFileName[8:]
    level2Label = level2Dir + level0SciBaseFileName[0:7] + '2' + level0SciBaseFileName[8:-4] + ".XML"

    # First run level 0 to level 1 conversion
    command = exeDir + "ola-level0-to-level1 " + kernelFile + " " + level0SciFile + " " + level1SciFile + " " + level1SciLabel
    print command
    #os.system(command)

    # Next run level 1 to CK conversion. This generates a file which is used as input to msopck
    command = exeDir + "ola-level1-to-ck " + level1SciFile + " " + lskKernelFile + " " + sclkKernelFile + " " + framesKernelFile + " " + msopckInputDataFile + " " + msopckInputSetupFile
    print command
    os.system(command)

    # Next run msopck to generate a CK kernel file. Remove the CK first in case it already exists since msopck will append to it rather than overwrite it.
    try:
        os.remove(msopckOutputCkFile)
    except OSError:
        pass
    command = exeDir + "msopck " + msopckInputSetupFile + " " + msopckInputDataFile + " " + msopckOutputCkFile
    print command
    os.system(command)

    # Finally run level 1 to level 2 conversion
    command = exeDir + "ola-level1-to-level2 " + kernelFile + " " + msopckOutputCkFile + " " + level1SciFile + " " + level2File + " " + level2Label
    print command
    os.system(command)


filelist = loadFiles()
for f in filelist:
    process(f.rstrip())

