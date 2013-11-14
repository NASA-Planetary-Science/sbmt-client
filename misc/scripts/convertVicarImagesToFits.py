#!/usr/bin/python

import glob
import os

files = glob.glob('*.IMG')

raw2isis = 'vicar2isis'
isis2fits = 'isis2fits'

for f in files:
    print f


    if os.path.getsize(f) == 74760:
        #nsamples = '256'
        #nlines = '256'
        bytesPerPixel = 1
    elif os.path.getsize(f) == 140968:
        #nsamples = '256'
        #nlines = '256'
        bytesPerPixel = 2
    elif os.path.getsize(f) == 278184:
        #nsamples = '512'
        #nlines = '512'
        bytesPerPixel = 1
    elif os.path.getsize(f) == 540768:
        #nsamples = '512'
        #nlines = '512'
        bytesPerPixel = 2
    elif os.path.getsize(f) == 1077344:
        #nsamples = '1024'
        #nlines = '1024'
        bytesPerPixel = 1
    elif os.path.getsize(f) == 2127944:
        #nsamples = '1024'
        #nlines = '1024'
        bytesPerPixel = 2
    else:
        continue


    name = os.path.splitext(f)

    if bytesPerPixel == 1:
        command = raw2isis + ' from='+f + ' to='+name[0]+'.cub'
        print command
        os.system(command)
        command = isis2fits + ' from='+name[0]+'.cub' + ' to='+name[0][:-2] + ' bittype=8BIT' + ' stretch=NONE'
        print command
        os.system(command)
        os.system('mv ' + name[0][:-2] + '.fits ' + name[0][:-2]+'.FIT')
    else:
        command = raw2isis + ' from='+f + ' to='+name[0]+'.cub'
        print command
        os.system(command)
        command = isis2fits + ' from='+name[0]+'.cub' + ' to='+name[0][:-2] + ' bittype=32BIT'
        print command
        os.system(command)
        os.system('mv ' + name[0][:-2] + '.fits ' + name[0][:-2]+'.FIT')
