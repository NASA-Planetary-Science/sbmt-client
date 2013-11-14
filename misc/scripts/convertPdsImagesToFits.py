#!/usr/bin/python

import glob
import os
import sys

# setup isis
#ISISROOT = os.environ["ISISROOT"]
#sys.path.append(ISISROOT + "/scripts")
#import isis3Startup
#isis3Startup.setisis()

files = glob.glob('*.img')

pds2isis = 'pds2isis'
isis2fits = 'isis2fits'

for f in files:
    print f


    bytesPerPixel = 2

    name = os.path.splitext(f)
    print name
    if bytesPerPixel == 1:
        command = pds2isis + ' from='+f + ' to='+name[0]+'.cub'
        print command
        os.system(command)
        command = isis2fits + ' from='+name[0]+'.cub' + ' to='+name[0] + ' bittype=8BIT' + ' stretch=NONE'
        print command
        os.system(command)
        os.system('mv ' + name[0] + '.fits ' + name[0]+'.FIT')
        os.system('rm -f ' + name[0] + '.cub')
    else:
        command = pds2isis + ' from='+f + ' to='+name[0]+'.cub'
        print command
        os.system(command)
        command = isis2fits + ' from='+name[0]+'.cub' + ' to='+name[0] + ' bittype=32BIT'
        print command
        os.system(command)
        os.system('mv ' + name[0] + '.fits ' + name[0]+'.FIT')
        os.system('rm -f ' + name[0] + '.cub')
