#!/usr/bin/python

import glob
import os

files = glob.glob('*.DAT')

raw2isis = 'raw2isis'
isis2fits = 'isis2fits'

for f in files:
    print f


    if os.path.getsize(f) == 262144:
        nsamples = '512'
        nlines = '512'
    elif os.path.getsize(f) == 1048576:
        nsamples = '1024'
        nlines = '1024'
    elif os.path.getsize(f) == 640000:
        nsamples = '800'
        nlines = '800'
    else:
        continue


    name = os.path.splitext(f)

    if not os.path.exists('../SUMFILES/' + name[0] + '.SUM'):
        print 'no sum file for ' + name[0]
        continue

    command = raw2isis + ' from='+f + ' to='+name[0]+'.cub' + ' samples='+nsamples + ' lines='+nlines
    print command
    os.system(command)
    command = isis2fits + ' from='+name[0]+'.cub' + ' to='+name[0] + ' bittype=8BIT' + ' stretch=NONE'
    print command
    os.system(command)
    os.system('mv ' + name[0] + '.fits ' + name[0]+'.FIT')
