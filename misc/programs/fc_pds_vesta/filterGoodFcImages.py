#!/usr/bin/env python

# This script takes a file listing all FC images (1 per line) and
# removes images that should not be indexed in the database. These
# include duplicate images, images of level 1C processing, and images
# of level 1A for which there is corresponding 1B image. It prints to
# standard images only the images which should be indexed.
#
# Usage: filterGoodFcImages.py <path-to-file-containing-list-of-FC-images>

import sys
import os

infile = sys.argv[1]
fin = open(infile,'r')

lines = fin.readlines()

baselines={}
for line in lines:
    baselines[os.path.basename(line)[:12]] = line[:-1]

for line in baselines:
    if line[4] == 'C':
        continue

    if line[4] == 'A':
        changedToB = line[:4] + 'B' + line[5:]
        if changedToB in baselines:
            continue

    print baselines[line]
