#!/usr/bin/python

import os
import sys
import subprocess
import math

# Change to folder containing this script
abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)


if (len(sys.argv) < 3):
    print "Usage: run-lidar-opt.py <-eros|-itokawa> track0.txt [track1.txt ...]"
    sys.exit(1)


# Compute total number on lines in provided list of files
def num_lines_in_input(input_file):
    f = open(input_file)
    num_lines = sum(1 for line in f)
    f.close()
    return num_lines


def run_lidar_min():
    number_points = num_lines_in_input(INPUT)
    startId = '0'
    stopId = str(number_points)
    command = './lidar-min-icp '+BODY+' --single-track-mode=yes '+VTKFILE+' '+startId+' '+stopId+' '+KERNEL+' '+OUTPUT+' '+INPUT
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()


def concat_output_tracks(files):
    # Concatenate all output files into a single file
    command = 'cat '
    for f in files:
        INPUT=f
        OUTPUT=INPUT+'-optimized.txt'
        command = command + OUTPUT + ' '
    CONCAT_OUTPUT = os.path.dirname(files[0])+'/all-tracks-optimized.txt'
    command = command + ' > ' + CONCAT_OUTPUT
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()


###########################################################################


if sys.argv[1] == '--eros' or sys.argv[1] == '-eros':
    BODY='EROS'
    VTKFILE=os.environ['HOME']+'/.neartool/cache/2/EROS/ver512q.vtk'
elif sys.argv[1] == '--itokawa' or sys.argv[1] == '-itokawa':
    BODY='ITOKAWA'
    VTKFILE=os.environ['HOME']+'/.neartool/cache/2/ITOKAWA/ver512q.vtk'
else:
    print "Error: first option must be either -eros or -itokawa"
    sys.exit(1)

# Check to make sure shape model file exists
if os.path.exists(VTKFILE) == False:
    print "Error: Shape model file does not exist. This program uses the shape model file (highest resolution) available in the SBMT cache."
    print "Please run the SBMT and display the highest resolution shape model of Eros or Itokawa before continuing."
    sys.exit(1)

KERNEL='./naif0010.tls'
inputFiles = sys.argv[2:]

for f in inputFiles:
    INPUT=f
    OUTPUT=INPUT+'-optimized.txt'
    run_lidar_min()

concat_output_tracks(inputFiles)
