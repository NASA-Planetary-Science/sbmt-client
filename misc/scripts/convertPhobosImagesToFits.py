#!/usr/bin/python

import glob
import os

files = glob.glob('*.DAT')

raw2isis = 'raw2isis'
isis2fits = 'isis2fits'

for f in files:
    print f


    if f.startswith('V'):
        nsamples = '1204'
        nlines = '1056'
    else:
        nsamples = '510'
        nlines = '298'


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
