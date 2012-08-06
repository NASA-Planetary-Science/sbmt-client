#!/usr/bin/python

# This program converts a PLT file to OBJ format. To run this program
# on the command line, type the name of PLT file as the first argument
# and the name of the desired output OBJ file as the second argument.
#
# Example:
#  ./plt2obj.py model.plt model.obj


import sys

if (len(sys.argv) != 3):
    sys.stderr.write("Usage: ./plt2obj.py <inputfile> <outputfile>\n")
    sys.exit(1)

infile = sys.argv[1]
outfile = sys.argv[2]

fin = open(infile,'r')
fout = open(outfile,'w')

# Some plt files list the number of points and plates on the first
# line. Others list the number of points only and list the number of
# plates after all the points have been listed. Account for both of
# these possibilities.
firstLine = fin.readline().split()
numPoints = ""
numPlates = ""

if len(firstLine) == 1:
    numPoints = firstLine[0]
else:
    numPoints = firstLine[0]
    numPlates = firstLine[1]

for i in range(int(numPoints)):
    line = fin.readline()
    p = line.split()
    fout.write("v " + p[1] + " " + p[2] + " " + p[3] + "\n")

if len(firstLine) == 1:
    numPlates = fin.readline().split()[0]
	
for i in range(int(numPlates)):
    line = fin.readline()
    p = line.split()
    fout.write("f " + p[1] + " " + p[2] + " " + p[3] + "\n")

fin.close()
fout.close()
