#!/usr/bin/env python

from spice import *

# This program loads in the concatenated version of dv_hist files and
# tries to find all occurances of thrusts and prints out the thrusts and
# the time they occured so they can be plotted


KERNEL='/project/nearsdc/spice-kernels/hayabusa/kernels.txt'
furnsh(KERNEL)

hayabusaId = bodn2c("HAYABUSA")

# Threshold
T = 0.01
thrustTimes = []

# Find thrusts for September
def findThrustsSep(dv_file):
    f = open(dv_file, 'r')
    lines = f.readlines()

    for line in lines:
        # if the line is empty ignore it
        line = line.strip()
        if line == "":
            continue
        fields = line.split()
        if len(fields) < 6:
            continue

        # Look at columns 2 through 4 to see if there was a significant thrust
        if abs(float(fields[1])) > T or abs(float(fields[2])) > T or abs(float(fields[3])) > T:
            # convert the time to format recognized by spice. Some times have spaces in them. Others do not.
            # This means change the dash to T and the slashes to dashes.
            if len(fields) == 6:
                utc = fields[-1]
                utc = utc[0:4] + "-" + utc[5:7] + "-" + utc[8:10] + "T" + utc[11:]
            else:
                utc = fields[5] + "-" + fields[6] + "-" + fields[7] + "T" + fields[8] + ":" + fields[9] + ":" + fields[10]
            # convert this time to spacecraft clock time
            et = utc2et(utc)
            sct = sce2s(hayabusaId, et)
            thrustTimes.append(et)
            print sct[2:-4], 0, utc, et
 

# Find thrusts for October and November files
def findThrusts(dv_file):
    f = open(dv_file, 'r')
    lines = f.readlines()

    for line in lines:
        # if the line is empty ignore it
        line = line.strip()
        if line == "":
            continue
        fields = line.split()
        if len(fields) < 8:
            continue

        # Look at columns 2 through 7 to see if there was a significant thrust
        if abs(float(fields[1])) > T or abs(float(fields[2])) > T or abs(float(fields[3])) > T or \
           abs(float(fields[4])) > T or abs(float(fields[5])) > T or abs(float(fields[6])) > T:
            # convert the last field in the line, which should be the time, to format
            # recognized by spice. This means change the dash to T and the slashes to dashes
            utc = fields[-1]
            utc = utc[0:4] + "-" + utc[5:7] + "-" + utc[8:10] + "T" + utc[11:]
            # convert this time to spacecraft clock time
            et = utc2et(utc)
            sct = sce2s(hayabusaId, et)
            thrustTimes.append(et)
            print sct[2:-4], 0, utc, et


# Number of seconds before and after each thrust time to ignore
bufferAmount = 600

minSizeForGoodInterval = 86400.0/1.0
goodIntervals=[]

def findGoodIntervals():
    thrustTimes.sort()
    j = 1
    for i in range(0,len(thrustTimes)-1):
        t0 = thrustTimes[i] + bufferAmount
        t1 = thrustTimes[i+1] - bufferAmount
        if t0 < t1 and t1-t0 >= minSizeForGoodInterval:
            utc0 = et2utc(t0, "C", 3)
            utc1 = et2utc(t1, "C", 3)
            print "Good Interval ", j, i, "(", t0, t1, ") (", utc0, utc1, ")", (t1-t0)/86400.0, (t1-t0)/3600.0
            j = j + 1
            goodIntervals.append([t0, t1])

            
dv_file1='dv_hist/dv_hist1.txt'
findThrustsSep(dv_file1)
dv_file2='dv_hist/dv_hist2.dat'
findThrusts(dv_file2)
dv_file3='dv_hist/dv_hist3.dat'
findThrusts(dv_file3)

findGoodIntervals()
print goodIntervals
