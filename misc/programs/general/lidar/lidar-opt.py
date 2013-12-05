#!/usr/bin/env python

# *** EXPORT CONTROLLED (12/2013) ***

import os
import sys
import subprocess
import math



if (len(sys.argv) < 3):
    print "Usage: run-lidar-opt.py <-eros|-itokawa|/path/to/VTK/file> track0.txt [track1.txt ...]"
    sys.exit(1)


# Compute total number on lines in provided list of files
def num_lines_in_input(inputfile):
    f = open(inputfile)
    num_lines = sum(1 for line in f)
    f.close()
    return num_lines


def run_lidar_min(vtkfile, kernelfile, outputfile, inputfile):
    number_points = num_lines_in_input(inputfile)
    startId = '0'
    stopId = str(number_points)
    # dname is folder containing this script
    abspath = os.path.abspath(__file__)
    dname = os.path.dirname(abspath)
    command = dname +'/lidar-optimize-track '+vtkfile+' '+kernelfile+' '+outputfile+' '+inputfile
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()


def concat_output_tracks(files):
    # Concatenate all output files into a single file
    command = 'cat ' + ' '.join(files)
    concatOutput = os.path.dirname(files[0])+'/all-tracks-optimized.txt'
    command = command + ' > ' + concatOutput
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()


def run_lidar_compute_track_stats(vtkfile, kernelfile, outputfile, files):
    # dname is folder containing this script
    abspath = os.path.abspath(__file__)
    dname = os.path.dirname(abspath)
    command = dname +'/lidar-compute-track-stats '+vtkfile+' '+kernelfile+' '+outputfile+' '+' '.join(files)
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()

###########################################################################


if sys.argv[1] == '--eros' or sys.argv[1] == '-eros':
    VTKFILE=os.environ['HOME']+'/.neartool/cache/2/EROS/ver512q.vtk'
elif sys.argv[1] == '--itokawa' or sys.argv[1] == '-itokawa':
    VTKFILE=os.environ['HOME']+'/.neartool/cache/2/ITOKAWA/ver512q.vtk'
else:
    VTKFILE=sys.argv[1]

# Check to make sure shape model file exists
if os.path.exists(VTKFILE) == False:
    print "Error: Shape model file does not exist. This program uses the shape model file (highest resolution) available in the SBMT cache."
    print "Please run the SBMT and display the highest resolution shape model of Eros or Itokawa before continuing."
    sys.exit(1)

# dname is folder containing this script
abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
KERNEL=dname + "/naif0010.tls"

inputFiles = sys.argv[2:]

outputFiles = []
inputOutputFiles = []
for f in inputFiles:
    INPUT=f
    output=INPUT+'-optimized.txt'
    outputFiles.append(output)
    run_lidar_min(VTKFILE, KERNEL, output, INPUT)
    inputOutputFiles.append(INPUT)
    inputOutputFiles.append(output)

concat_output_tracks(outputFiles)

errorcombinedfile = os.path.dirname(inputFiles[0])+'/track-errors.csv'
run_lidar_compute_track_stats(VTKFILE, KERNEL, errorcombinedfile, inputOutputFiles)
