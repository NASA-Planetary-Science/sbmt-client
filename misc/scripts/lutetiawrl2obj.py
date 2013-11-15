#!/usr/bin/python

# This program converts a PLT file to OBJ format. To run this program
# on the command line, type the name of PLT file as the first argument
# and the name of the desired output OBJ file as the second argument.
#
# Example:
#  ./plt2obj.py model.plt model.obj


import sys

if (len(sys.argv) != 3):
    sys.stderr.write("Usage: ./lutetiawrl2obj.py <inputfile> <outputfile>\n")
    sys.exit(1)

infile = sys.argv[1]
outfile = sys.argv[2]

fin = open(infile,'r')
fout = open(outfile,'w')

# Skip the first 6 lines
firstLines = fin.readline()
firstLines = fin.readline()
firstLines = fin.readline()
firstLines = fin.readline()
firstLines = fin.readline()
firstLines = fin.readline()

numPoints = 1572866
numPlates = 3145728

for i in range(int(numPoints)):
    line = fin.readline()
    p = line.split()
    fout.write("v " + p[0] + " " + p[1] + " " + p[2] + "\n")

# Skip the next 3 lines
firstLines = fin.readline()
firstLines = fin.readline()
firstLines = fin.readline()

for i in range(int(numPlates)):
    line = fin.readline()
    p = line.split()
    fout.write("f %d %d %d\n" % (int(p[0])+1, int(p[1])+1, int(p[2])+1))

fin.close()
fout.close()
