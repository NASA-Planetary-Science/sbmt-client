#!/usr/bin/python

import glob
import os
import sys


files = glob.glob('*.FIT')

for f in files:
    print f


    name = os.path.basename(f)
    outfile = 'cropped/' + name
    print outfile
    command = 'convert ' + name + ' -crop ' + outfile
    print command
    #os.system("cp " + template + " " + sumfile)
