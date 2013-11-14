#!/usr/bin/python

import glob
import os
import sys


files = glob.glob('*.fit')
template = "tmp.SUM"

for f in files:
    print f


    name = os.path.splitext(f)
    print name
    sumfile = name[0] + ".SUM"
    os.system("cp " + template + " " + sumfile)
