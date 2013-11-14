#!/usr/bin/python

# This program converts the NEAR files to PCD format for easy loading into  plt file to vtk format. To run this program
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
outfile = sys.argv[2]

fin2 = open(infile,'r')
fout = open(outfile,'w')

filelist = fin2.readlines()


# We need to go through all the files twice. The first is to count
# the number of points which we need for the header. In the second
# pass we actually write out the data.

totalNumberOfPoints = 0

for f in filelist:

    print f
    f = f.split()[0]
    fin = open(f,'r')
    for line in fin:
        # Skip first 2 lines which are the header
        if line.startswith('l') or line.startswith('L'):
            continue
        splitLine = line.split()
        # Skip noise
        if splitLine[7] == '1':
            continue
        totalNumberOfPoints += 1
    fin.close()

# Write out header
fout.write("# .PCD v.7 - Point Cloud Data file format\n")
fout.write("VERSION .7\n")
fout.write("FIELDS x y z\n")
fout.write("SIZE 4 4 4\n")
fout.write("TYPE F F F\n")
fout.write("COUNT 1 1 1\n")
fout.write("WIDTH " + str(totalNumberOfPoints) + "\n")
fout.write("HEIGHT 1\n")
fout.write("VIEWPOINT 0 0 0 1 0 0 0\n")
fout.write("POINTS " + str(totalNumberOfPoints) + "\n")
fout.write("DATA ascii\n")

for f in filelist:

    print f
    f = f.split()[0]
    fin = open(f,'r')
    for line in fin:
        # Skip first 2 lines which are the header
        if line.startswith('l') or line.startswith('L'):
            continue
        splitLine = line.split()
        # Skip noise
        if splitLine[7] == '1':
            continue

        x = float(splitLine[14]) / 1000.0
        y = float(splitLine[15]) / 1000.0
        z = float(splitLine[16]) / 1000.0
        fout.write(str(x) + " " + str(y) + " " + str(z) + "\n")
    fin.close()

fout.close
fin2.close()
