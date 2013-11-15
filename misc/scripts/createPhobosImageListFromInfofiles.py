#!/usr/bin/python

import glob
import os

files = glob.glob('*.INFO')

for f in files:

    fin = open(f,'r')

    name = os.path.splitext(f)

    firstLine = fin.readline().split()

    print name[0]+'.FIT '+firstLine[2]
