#!/usr/bin/python

# This program converts a plt file to vtk format. To run this program
# on the command line, type the name of .plt file as the first
# argument. The converted file with a vtk extension will be created in
# the same folder.
#
# Example:
#  ./plt2vtk.py model.plt
#
# The file model.vtk will be created in the same folder


import sys

infile = sys.argv[1]
outfile = infile[:-4] + ".vtk"

fin = open(infile,'r')
fout = open(outfile,'w')

# Some plt files list the number of points and plates on the first
# line. Others list the number of points only and list the number of
# plates after all the points. Account for both of these
# possibilities.
firstLine = fin.readline().split()
numPoints = ""
numPlates = ""

if len(firstLine) == 1:
    numPoints = firstLine[0]
else:
    numPoints = firstLine[0]
    numPlates = firstLine[1]

fout.write("# vtk DataFile Version 2.0\n")
fout.write("vtkfile\n")
fout.write("ASCII\n")
fout.write("DATASET POLYDATA\n")
fout.write("POINTS " + numPoints + " float\n")

for i in range(int(numPoints)):
    line = fin.readline()
    p = line.split()
    fout.write(p[1] + " " + p[2] + " " + p[3] + "\n")

if len(firstLine) == 1:
    numPlates = fin.readline().split()[0]
	
fout.write("POLYGONS " + numPlates + " " + str(int(numPlates)*4) + "\n")

# Note that the point indices are 1-based in the plt model format but
# vtk requires 0-based. Therefore subract 1 from each index.
for i in range(int(numPlates)):
    line = fin.readline()
    p = line.split()
    fout.write("3 %d %d %d\n" % (int(p[1])-1, int(p[2])-1, int(p[3])-1))
