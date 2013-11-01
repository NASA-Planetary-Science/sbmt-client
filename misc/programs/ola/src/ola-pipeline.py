#!/usr/bin/python

# This program takes OLA level 0 data and generates OLA level 1 and
# level 2 data. It takes a single argument, namely the path to the
# root of the data hierarchy used for processing


import sys
import os
import tempfile
import datetime


if len(sys.argv) != 1:
    print "Error: This script takes no arguments."
    sys.exit()

rootDataDir = "."

level0FileList = rootDataDir + "/SciData/OLA/level0-filelist.txt"
kernelFile = rootDataDir + "/SPICE/spice-kernels.mk"


ckDir = rootDataDir + "/SPICE/Kernels/CK/"
tmpDir = tempfile.mkdtemp()

# Load the files we need to process and return them as a list
def loadFiles(infile):
    fin = open(infile,'r')
    filelist = fin.readlines()
    return filelist


# Run the level 0 to level 1 to level 2 processing for a single level 0 file
def process(level0SciBaseFileName):

    # create the L1 and L2 folders (L0 should already exist)
    YYYY = "20" + level0SciBaseFileName[21:23]
    MM = level0SciBaseFileName[23:25]
    DD = level0SciBaseFileName[25:27]

    year = int(YYYY)
    month = int(MM)
    day = int(DD)

    DOY = datetime.date(year, month, day).strftime("%j")

    level0SciDir = rootDataDir + "/SciData/OLA/" + YYYY + "/" + DOY + "/L0/"
    level1SciDir = rootDataDir + "/SciData/OLA/" + YYYY + "/" + DOY + "/L1/"
    level2Dir = rootDataDir + "/SciData/OLA/" + YYYY + "/" + DOY + "/L2/"

    if not os.path.exists(level1SciDir): os.mkdir(level1SciDir)
    if not os.path.exists(level2Dir): os.mkdir(level2Dir)


    # First, using the name of the level 0 file, construct the names of the other files we will be generating
    level0SciFile = level0SciDir + level0SciBaseFileName
    level1SciFile = level1SciDir + level0SciBaseFileName[0:12] + '1' + level0SciBaseFileName[13:]
    level1SciLabel = level1SciDir + level0SciBaseFileName[0:12] + '1' + level0SciBaseFileName[13:-4] + ".xml"
    msopckInputDataFile = tmpDir + "msopckInputData-" + level0SciBaseFileName[14:-4] + ".txt"
    msopckInputSetupFile = tmpDir + "msopckInputSetup-" + level0SciBaseFileName[14:-4] + ".txt"
    msopckOutputCkFile = ckDir + level0SciBaseFileName[0:8] + level0SciBaseFileName[14:-4] + ".bc"
    level2File = level2Dir + level0SciBaseFileName[0:12] + '2' + level0SciBaseFileName[13:]
    level2Label = level2Dir + level0SciBaseFileName[0:12] + '2' + level0SciBaseFileName[13:-4] + ".xml"

    # First run level 0 to level 1 conversion
    command = "ola-level0-to-level1 " + kernelFile + " " + level0SciFile + " " + level1SciFile + " " + level1SciLabel
    print command
    os.system(command)

    # Next run level 1 to CK conversion. This generates a file which is used as input to msopck
    command = "ola-level1-to-ck " + level1SciFile + " " + kernelFile + " " + msopckInputDataFile + " " + msopckInputSetupFile
    print command
    os.system(command)

    # Next run msopck to generate a CK kernel file. Remove the CK first in case it already exists since msopck will append to it rather than overwrite it.
    try:
        os.remove(msopckOutputCkFile)
    except OSError:
        pass
    command = "msopck " + msopckInputSetupFile + " " + msopckInputDataFile + " " + msopckOutputCkFile
    print command
    os.system(command)

    # Finally run level 1 to level 2 conversion
    command = "ola-level1-to-level2 " + kernelFile + " " + msopckOutputCkFile + " " + level1SciFile + " " + level2File + " " + level2Label
    print command
    os.system(command)


filelist = loadFiles(level0FileList)
for f in filelist:
    process(f.rstrip())

