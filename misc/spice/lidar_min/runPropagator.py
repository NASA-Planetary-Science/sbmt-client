#!/usr/bin/python

import os
import subprocess

# Change to folder containing this script
abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)


BODY='ITOKAWA'
VTKFILE='/project/nearsdc/data/ITOKAWA/ver64q.vtk'
PLTFILE='/project/nearsdc/data/ITOKAWA/ver64q.tab'
KERNEL='/project/nearsdc/spice-kernels/hayabusa/kernels.txt'


#INPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf_20050911_20050930_v2.tab'
INPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_all2.tab'
#INPUT='cdr_uf_2005-09-12_v2-b.tab'
#INPUT='cdr_uf_2005-10-31_v2.tab'
#INPUT='cdr_uf_2005-10-31.tab'
#INPUT='cdr_uf2_2005-11-18.tab'
#INPUT='cdr_uf2_2005-10-28.tab'
#INPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf_arc1.tab'
OUTPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/prop-traj.txt'

#startTime='2378384718'
#stopTime='2389142129'
#density='0.00030713'
#pressure='1.21837'
#position='15.77,-3.96316,-1.85106'
#velocity='1.29236e-05,-3.95445e-06,-3.11087e-06'

#startTime='2390000022'
#stopTime='2408657099'
#density='2.33831'
#pressure='1.22556'
#velocity='3.27187e-05,-4.361e-06,-2.53998e-06'
#position='14.2904,-3.00209,-1.68242'

startTime='180977699.76'
stopTime='181141879.654'
density='0.216207'
pressure='1.65806'
position='11.6965,-1.17968,-0.653116'
velocity='9.58572e-06,2.20938e-06,2.43306e-06'

#startTime='181664530.935'
#stopTime='182097704.216'
#density='0.808835'
#pressure='1.14245'
#position='5.19577,0.498368,0.528289'
#velocity='3.22905e-05,1.1679e-05,2.46296e-06'

#startTime='182797687.573'
#stopTime='182970835.462'
#density='2.03'
#pressure='1.22692'
#position='7.02233,0.960101,3.50028'
#velocity='1.2946e-07,-8.94694e-07,-1.05388e-05'

#startTime='182624891.914'
#stopTime='182795287.573'
#density='2.03'
#pressure='1.22692'
#position='7.02233,0.960101,3.50028'
#velocity='1.2946e-07,-8.94694e-07,-1.05388e-05'

startTime='183743545.705'
stopTime='184002083.595'
density='2.03'
pressure='1.22692'
position='3.2248,1.07039,0.486777'
velocity='3.34028e-05,1.59279e-05,5.89719e-06'

# 2005 OCT 31 04:45:39.404 - 2005 NOV 03 03:30:37.462
#startTime='184006003.587'
#stopTime='184260701.645'
#density='2.03'
#pressure='1.22692'
#position='3.2248,1.07039,0.486777'
#velocity='3.34028e-05,1.59279e-05,5.89719e-06'

#command = './propagator -ep -d '+density+' -p '+pressure+' -v '+velocity+' -po '+position+' -b '+BODY+\
#          ' -s '+VTKFILE+' -t '+PLTFILE+' -k '+KERNEL+' -i '+INPUT+' -o '+OUTPUT+' -start '+startTime+' -stop '+stopTime
command = './propagator -ed -d '+density+' -p '+pressure+' -b '+BODY+\
    ' -s '+VTKFILE+' -t '+PLTFILE+' -k '+KERNEL+' -i '+INPUT+' -o '+OUTPUT+' -start '+startTime+' -stop '+stopTime
print command
os.system(command)

os.system("cp " + INPUT + " ~/.neartool/cache/2/ITOKAWA/LIDAR/cdr/cdr_uf_arc.tab")
os.system("cp " + OUTPUT + " ~/.neartool/cache/2/ITOKAWA/LIDAR/cdr/")
os.system("cp " + OUTPUT + "-ref ~/.neartool/cache/2/ITOKAWA/LIDAR/cdr/")
