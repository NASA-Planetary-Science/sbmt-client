#!/usr/bin/python

# This program fixes an ellipses by shifting all the points by the specified amount.
#
# Example:
#  ./fixobj.py input output xshift yshift zshift

import sys

if (len(sys.argv) != 6):
    sys.stderr.write("Usage: ./fixobj.py <inputfile> <outputfile>\n")
    sys.exit(1)

infile = sys.argv[1]
outfile = sys.argv[2]
x = float(sys.argv[3])
y = float(sys.argv[4])
z = float(sys.argv[5])

fin = open(infile,'r')
fout = open(outfile,'w')

lines = fin.readlines()

for line in lines:
    p = line.split()
    fout.write("%s\t%s\t%.16f\t%.16f\t%.16f\t" % (p[0], p[1], float(p[2])+x, float(p[3])+y, float(p[4])+z))
    fout.write("\t".join(p[5:])+"\r\n")

fin.close()
fout.close()
