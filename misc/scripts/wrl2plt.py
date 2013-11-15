#!/usr/bin/python

# This program converts the Lutetia WRL plate model to PLT format. To run this program
# on the command line, type the name of Lutetia shape model as the first argument
# and the name of the desired output PLT file as the second argument.
#
# Example:
#  ./lutetiawrl2plt.py lutetia_3e6_cart.wrl lutetia_3e6_cart.plt


import sys

if (len(sys.argv) != 3):
    sys.stderr.write("Usage: ./lutetiawrl2plt.py <inputfile> <outputfile>\n")
    sys.exit(1)

infile = sys.argv[1]
outfile = sys.argv[2]

fin = open(infile,'r')
fout = open(outfile,'w')

# Skip the first 5 lines
skippedLine = fin.readline()
skippedLine = fin.readline()
skippedLine = fin.readline()
skippedLine = fin.readline()
skippedLine = fin.readline()

# It is assumed the 6th line is of the form "#   10242   20480"
line = fin.readline()
p = line.split()
numPoints = int(p[1])
numPlates = int(p[2])

fout.write(str(numPoints) + " " + str(numPlates) + "\n")

for i in range(int(numPoints)):
    line = fin.readline()
    p = line.split()
    fout.write(str(i+1) + " " + p[0] + " " + p[1] + " " + p[2] + "\n")

# Skip the next 3 lines
skippedLine = fin.readline()
skippedLine = fin.readline()
skippedLine = fin.readline()

for i in range(int(numPlates)):
    line = fin.readline()
    p = line.split()
    fout.write("%d %d %d %d\n" % (i+1, int(p[0])+1, int(p[1])+1, int(p[2])+1))

fin.close()
fout.close()
