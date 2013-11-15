#!/usr/bin/python

import glob
import os

files = glob.glob('*.SUM')

for f in files:

    if f.startswith('ge'):
        continue

    fin = open(f,'r')
    firstLine = fin.readline().split()
    secondLine = fin.readline().split()
    thirdLine = fin.readline().split()

    #  only do it for images of size 2048x2048
    if thirdLine[0] != '2048' or thirdLine[1] != '2048':
        continue

    print firstLine[0]+'.FIT '+secondLine[0]+'-07-'+secondLine[2]+'T'+secondLine[3]
