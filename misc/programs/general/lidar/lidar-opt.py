#!/usr/bin/env python

import os
import sys
import subprocess
import math



if (len(sys.argv) < 3):
    print "Usage: run-lidar-opt.py <-eros|-itokawa> track0.txt [track1.txt ...]"
    sys.exit(1)


# Compute total number on lines in provided list of files
def num_lines_in_input(inputfile):
    f = open(inputfile)
    num_lines = sum(1 for line in f)
    f.close()
    return num_lines


def run_lidar_min(inputfile, body, vtkfile, kernelfile, outputfile):
    number_points = num_lines_in_input(inputfile)
    startId = '0'
    stopId = str(number_points)
    # dname is folder containing this script
    abspath = os.path.abspath(__file__)
    dname = os.path.dirname(abspath)
    command = dname +'/lidar-min-icp '+body+' --single-track-mode=yes '+vtkfile+' '+startId+' '+stopId+' '+kernelfile+' '+outputfile+' '+inputfile
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()


def run_lidar_compute_track_stats(vtkfile, outputfile, files):
    # dname is folder containing this script
    abspath = os.path.abspath(__file__)
    dname = os.path.dirname(abspath)
    command = dname +'/lidar-compute-track-stats '+vtkfile+' '+outputfile+' '+' '.join(files)
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()

def write_track_stats(vtkfile, beforefiles, afterfiles):
    # write a file containing track error statistics both before and after the optimization
    errorbeforefile = os.path.dirname(beforefiles[0])+'/errors-before.csv'
    errorafterfile = os.path.dirname(afterfiles[0])+'/errors-after.csv'
    errorcombinedfile = os.path.dirname(afterfiles[0])+'/track-errors.csv'
    run_lidar_compute_track_stats(VTKFILE, errorbeforefile, beforefiles)
    run_lidar_compute_track_stats(VTKFILE, errorafterfile, afterfiles)
    # Now load these 2 files and combine them
    fb = open(errorbeforefile)
    fa = open(errorafterfile)
    fcombined = open(errorcombinedfile, "w")

    beforelines = fb.readlines();
    afterlines = fa.readlines();

    count = 0
    for bline, aline in zip(beforelines, afterlines):
        if count == 0:
            fcombined.write("track,min distance before,max distance before,rms before,min distance after,max distance after,rms after\n")
        else:
            bline = bline.rstrip()
            fields = aline.split(',')
            fcombined.write(bline + ',' + ','.join(fields[1:]))
        count = count + 1

    fb.close()
    fa.close()
    fcombined.close()

    os.remove(errorbeforefile)
    os.remove(errorafterfile)


def concat_output_tracks(files):
    # Concatenate all output files into a single file
    command = 'cat ' + ' '.join(files)
    concatOutput = os.path.dirname(files[0])+'/all-tracks-optimized.txt'
    command = command + ' > ' + concatOutput
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
    BODY='EROS'
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
for f in inputFiles:
    INPUT=f
    output=INPUT+'-optimized.txt'
    outputFiles.append(output)
    run_lidar_min(INPUT, BODY, VTKFILE, KERNEL, output)

concat_output_tracks(outputFiles)

write_track_stats(VTKFILE, inputFiles, outputFiles)
