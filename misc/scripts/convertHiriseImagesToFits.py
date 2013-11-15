#!/usr/bin/python

import glob
import os
import sys

files = glob.glob('*.IMG')

pds2isis = 'hi2isis'
isis2fits = 'isis2fits'

for f in files:
    print f


    bytesPerPixel = 2

    name = os.path.splitext(f)
    print name

    # convert to isis cub
    command = pds2isis + ' from='+f + ' to='+name[0]+'.cub'
    print command
    os.system(command)

    # crop the image
    command = 'crop from=' + name[0]+'.cub to=' + name[0]+'-cropped.cub line=1224 nlines=3072'
    os.system(command)
    print command

    # convert to FITS
    command = isis2fits + ' from='+name[0]+'-cropped.cub' + ' to='+name[0] + ' bittype=32BIT'
    print command
    os.system(command)

    # change extension from .fits to .FIT
    os.system('mv ' + name[0] + '.fits ' + name[0]+'.FIT')
