#!/usr/bin/python

import glob
import os

files = glob.glob('*.DAT')

raw2isis = 'raw2isis'
isis2fits = 'isis2fits'

for f in files:
    print f


    if os.path.getsize(f) == 65536:
        nsamples = '256'
        nlines = '256'
        bytesPerPixel = 1
    elif os.path.getsize(f) == 131072:
        nsamples = '256'
        nlines = '256'
        bytesPerPixel = 2
    elif os.path.getsize(f) == 262144:
        nsamples = '512'
        nlines = '512'
        bytesPerPixel = 1
    elif os.path.getsize(f) == 524288:
        nsamples = '512'
        nlines = '512'
        bytesPerPixel = 2
    elif os.path.getsize(f) == 1048576:
        nsamples = '1024'
        nlines = '1024'
        bytesPerPixel = 1
    elif os.path.getsize(f) == 2097152:
        nsamples = '1024'
        nlines = '1024'
        bytesPerPixel = 2
    elif os.path.getsize(f) == 640000:
        nsamples = '800'
        nlines = '800'
        bytesPerPixel = 1
    else:
        continue


    name = os.path.splitext(f)

    if not os.path.exists('../SUMFILES/' + name[0] + '.SUM'):
        print 'no sum file for ' + name[0]
        continue

    if bytesPerPixel == 1:
        command = raw2isis + ' from='+f + ' to='+name[0]+'.cub' + ' samples='+nsamples + ' lines='+nlines
        print command
        os.system(command)
        command = isis2fits + ' from='+name[0]+'.cub' + ' to='+name[0] + ' bittype=8BIT' + ' stretch=NONE'
        print command
        os.system(command)
        os.system('mv ' + name[0] + '.fits ' + name[0]+'.FIT')
    else:
        command = raw2isis + ' from='+f + ' to='+name[0]+'.cub' + ' samples='+nsamples + ' lines='+nlines + ' bittype=UNSIGNEDWORD'
        print command
        os.system(command)
        command = isis2fits + ' from='+name[0]+'.cub' + ' to='+name[0] + ' bittype=32BIT'
        print command
        os.system(command)
        os.system('mv ' + name[0] + '.fits ' + name[0]+'.FIT')
