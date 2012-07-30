#!/usr/bin/python

import os
import sys
import subprocess
import math

# Change to folder containing this script
abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)

print sys.argv

if (len(sys.argv) < 2):
    print "Please enter path to lidar file"
    sys.exit(1)

def reclat(rectan):
    llr = [0.0, 0.0, 0.0] # longitude, latitude, radius

    vmax = max(abs(rectan[0]), max( abs(rectan[1]), abs(rectan[2]) ) )

    if ( vmax > 0.):
        x1 = rectan[0] / vmax
        y1 = rectan[1] / vmax
        z1 = rectan[2] / vmax
        llr[2] = vmax * math.sqrt( x1*x1 + y1*y1 + z1*z1 ) # radius
        llr[1] = math.atan2(z1, math.sqrt( x1*x1 + y1*y1 ) ) # latitude

        if ( x1 == 0. and y1 == 0.):
            llr[0] = 0. # longitude
        else:
            llr[0] = math.atan2(y1, x1) # longitude

    return llr

def reformat_input(input_file):
    f = open(input_file)
    new_input = input_file+'-reformatted'
    f2 = open(new_input, 'w')
    lines = f.readlines()
    for line in lines:
        fields = line.split()
        utc = fields[0]
        xtarget = float(fields[1])*1000.0
        ytarget = float(fields[2])*1000.0
        ztarget = float(fields[3])*1000.0
        scpos = [float(fields[4]), float(fields[5]), float(fields[6])]
        llr = reclat(scpos)
        sclon = llr[0]*180.0/math.pi
        sclat = llr[1]*180.0/math.pi
        scrad = llr[2]*1000.0
        output_line = '0 0 0 0 %s 0 0 0 %.16e %.16e %.16e 0 0 0 %.16e %.16e %.16e\n' % (utc, sclon, sclat, scrad, xtarget, ytarget, ztarget)
        f2.write(output_line)
    f.close()
    f2.close()
    return new_input


# Compute total number on lines in provided list of files
def num_lines_in_input(input_file):
    f = open(input_file)
    num_lines = sum(1 for line in f)
    f.close()
    print "total number of lines " + str(num_lines)
    return num_lines


def run_lidar_min():
    number_points = num_lines_in_input(INPUT)
    startId = '0'
    stopId = str(number_points)
    ii = '0'
    command = './build/lidar-min-icp '+BODY+' '+DSKFILE+' '+startId+' '+stopId+' '+KERNEL+' '+OUTPUT+' '+INPUT
    print command
    p = subprocess.Popen(command, shell=True)
    p.wait()

def do_all():
    global INPUT
    INPUT = reformat_input(INPUT)
    run_lidar_min()


###########################################################################

BODY='EROS'
DSKFILE=os.environ['HOME']+'/.neartool/cache/2/EROS/ver512q.vtk'
KERNEL='./naif0009.tls'
INPUT=sys.argv[1]
OUTPUT=INPUT+'-output'

do_all()
