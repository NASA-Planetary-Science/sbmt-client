#!/usr/bin/python

import os

f1 = open('fcimages_pds.csv', 'r')

lines1 = f1.readlines();

for line1 in lines1:
    line1 = line1.strip()
#    os.system("gzip -c /project/dawn/daily/fc/FITS/" + line1 + " > images-gzipped/" + line1 + ".gz")
    os.system("cp -p /project/dawn/daily/fc/FITS/" + line1 + " images-gzipped/")
