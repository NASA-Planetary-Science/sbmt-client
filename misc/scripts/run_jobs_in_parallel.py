#!/usr/bin/python

# This program can be used to run multiple commands in parallel. It
# takes 2 parameters. The first is a file containing a list of
# commands to run, one command per line. The second is an integer
# specifying the maximum number of processes to run at a time. It
# works by generating a makefile and running the makefile with the -j
# option.
#
# Example:
#  ./run_jobs_in_parallel.py file_containing_commands 5
#
# This will run all the command in the file at a maximum of 5 commands at a time


import sys
import os
import tempfile

infile = sys.argv[1]
maxProc = sys.argv[2]

fin = open(infile,'r')

lines = fin.readlines()

print lines

temp = tempfile.mkstemp(prefix='paralleljobs')
fout = open(temp[1],'w')

fout.write("all : ")

for i in range(len(lines)):
    fout.write("job%d " % i)

fout.write("\n")

for i in range(len(lines)):
    fout.write("job%d :\n" % i)
    fout.write("\t" + lines[i])

fin.close()
fout.close()

# Using nohup makes this script exit immediately with the jobs still running in the background.
print     "/usr/bin/nohup make -k -f " + temp[1] + " -j " + maxProc + " all > /dev/null 2>&1 &"
os.system("/usr/bin/nohup make -k -f " + temp[1] + " -j " + maxProc + " all > /dev/null 2>&1 &")
