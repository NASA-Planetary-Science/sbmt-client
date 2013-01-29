#!/usr/bin/env python

import os
import process_dv_file
import run_jobs_in_parallel
from socket import gethostname


# Change to folder containing this script
abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)


BODY='ITOKAWA'
VTKFILE='/project/nearsdc/data/ITOKAWA/ver512q.vtk'
PLTFILE='/project/nearsdc/data/ITOKAWA/ver64q.tab'
KERNEL='/project/nearsdc/spice-kernels/hayabusa/kernels.txt'
INPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_all_v2.tab'

goodIntervals = process_dv_file.goodIntervals[5:]

f2 = open('optimization-output.txt-'+gethostname(), 'w')

def funcToMinimize(x):
    density=str(x[0])
    pressure=str(x[1])
    totalError=0.0
    commandlist=[]
    i = 0
    for interval in goodIntervals:
        t0 = str(interval[0])
        t1 = str(interval[1])
        statsfile = 'stats-' + str(i) + '.txt-' + gethostname()
        OUTPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/prop-traj-' + str(i) + '.txt-' + gethostname()
        command = './build/prop-fit -ev -d '+density+' -p '+pressure+' -b '+BODY+\
                  ' -s '+VTKFILE+' -t '+PLTFILE+' -k '+KERNEL+' -i '+INPUT+' -o '+OUTPUT+\
                  ' -start '+t0+' -stop '+t1+' -e '+statsfile
        print command
        commandlist.append(command)
        i = i + 1

    run_jobs_in_parallel.runJobs(commandlist, 3)

    numIntervals=0
    for i in range(len(goodIntervals)):

        # Load in the error files and compute mean error
        try:
            statsfile = 'stats-' + str(i) + '.txt-' + gethostname()
            f = open(statsfile, 'r')
            lines = f.readlines()
            error = float(lines[0])
            totalError += error
            numIntervals = numIntervals + 1
            f.close()
        except IOError as e:
            print 'Oh dear. No stats file'


    if numIntervals == 0:
        print "no valid intervals!"
        return 1.0e100

    meanError = totalError/numIntervals

    f2.write("Density, pressure: " + str(x) + " Total Error: " + str(totalError) + " Mean Error: " + str(meanError) + "\n")
    f2.flush()

    print "total error : ", totalError
    print "mean error : ", meanError

    return totalError

x0 = [2.25, 1.66666666666666666666]
error = funcToMinimize(x0)

#for i in range(0,2):
#    for j in range(1,10):
#        x0 = [i*(1.0/4.0), j*(1.0/3.0)]
#        error = funcToMinimize(x0)

f2.close()
