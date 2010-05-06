#!/usr/bin/python

# This program converts a Bob Gaskell shape models that can be
# downloaded from
# http://sbn.psi.edu/pds/asteroid/NEAR_A_MSI_5_EROSSHAPE_V1_0.zip to
# vtk format. This program can convert these 4 files: 
#
#   1. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver64q.tab
#   2. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver128q.tab
#   3. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver256q.tab
#   4. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver512q.tab
#
# Thus from the command line type:
# $ ./gaskell2vtk.py NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver64q.tab
# $ ./gaskell2vtk.py NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver128q.tab
# $ ./gaskell2vtk.py NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver256q.tab
# $ ./gaskell2vtk.py NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver512q.tab
# 
# and the following files will be generated:
#
#   1. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver64q.vtk
#   2. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver128q.vtk
#   3. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver256q.vtk
#   4. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver512q.vtk


import sys

infile = sys.argv[1]
outfile = infile[:-4] + ".vtk"

fin = open(infile,'r')
fout = open(outfile,'w')

# Read in the first line which list the number of points and plates
tmp = fin.readline().split()
numPoints = tmp[0]
numPlates = tmp[1]

print numPoints
print numPlates

fout.write("""# vtk DataFile Version 2.0
NEAR-A-MSI-5-EROSSHAPE-V1.0
ASCII
DATASET POLYDATA
POINTS """)
fout.write(numPoints + " float\n")

for i in range(int(numPoints)):
    line = fin.readline()
    p = line.split()
    fout.write(p[1] + " " + p[2] + " " + p[3] + "\n")

fout.write("POLYGONS " + numPlates + " " + str(int(numPlates)*4) + "\n")

# Note that the point indices are 1-based in the Gaskell model but vtk
# requires 0-based. Therefore subract 1 from each index.
for i in range(int(numPlates)):
    line = fin.readline()
    p = line.split()
    fout.write("3 %d %d %d\n" % (int(p[1])-1, int(p[2])-1, int(p[3])-1))
