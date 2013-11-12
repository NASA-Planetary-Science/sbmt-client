#!/usr/bin/env python

# This module contains a function, runJobs, which can be used to run
# multiple commands in parallel. It takes 2 arguments. The first is
# list of commands to run, one command per line. The second is an
# integer specifying the maximum number of processes to run at a
# time. It works by generating a makefile and running the makefile
# with the -j option.
#
# Example:
#  run_jobs_in_parallel.runJobs(command_list, 5)
#
# This will run all the commands at a maximum of 5 commands at a time


import os
import tempfile

def runJobs(commandlist, maxProc):
    print commandlist

    temp = tempfile.mkstemp(prefix='paralleljobs')
    fout = open(temp[1],'w')

    fout.write("all : ")

    for i in range(len(commandlist)):
        fout.write("job%d " % i)

    fout.write("\n")

    for i in range(len(commandlist)):
        fout.write("job%d :\n" % i)
        fout.write("\t" + commandlist[i] + "\n")

    fout.close()

    print     "make -k -f " + temp[1] + " -j " + str(maxProc) + " all > /dev/null"
    #os.system("make -k -f " + temp[1] + " -j " + str(maxProc) + " all > /dev/null")
    os.system("make -k -f " + temp[1] + " -j " + str(maxProc) + " all")
