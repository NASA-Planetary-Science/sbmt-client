#!/usr/bin/python

# This program fixes an OBJ file which is 0 based rather than 1 based. To run this program
# on the command line, type the name of input OBJ file as the first argument
# and the name of the desired output OBJ file as the second argument.
#
# Example:
#  ./fixobj.py model.obj model-fixed.obj

import sys

if (len(sys.argv) != 3):
    sys.stderr.write("Usage: ./fixobj.py <inputfile> <outputfile>\n")
    sys.exit(1)

infile = sys.argv[1]
outfile = sys.argv[2]

fin = open(infile,'r')
fout = open(outfile,'w')

lines = fin.readlines()

for line in lines:
    if line.startswith("f"):
        p = line.split()
        fout.write("f %d %d %d\r\n" % (int(p[1])+1, int(p[2])+1, int(p[3])+1))
    else:
        fout.write(line)

fin.close()
fout.close()
