#!/usr/bin/python

import glob
import os

datFilePath = '/project/nearsdc/misc/comet/67P_CGW/IMAGEFILES/'
sumFilePath = '/project/nearsdc/misc/comet/67P_CGW/SUMFILES/'
fitOutputPath = 'images/'

files = glob.glob(datFilePath + '*.DAT')

raw2isis = 'raw2isis'
isis2fits = 'isis2fits'

for f in files:
    print f

    if os.path.getsize(f) == 524288:
        nsamples = '512'
        nlines = '512'
    elif os.path.getsize(f) == 2097152:
        nsamples = '1024'
        nlines = '1024'
    elif os.path.getsize(f) == 8388608:
        nsamples = '2048'
        nlines = '2048'
    else:
        continue

    splitf = os.path.split(f)
    name = os.path.splitext(splitf[1])

    if not os.path.exists(sumFilePath + name[0] + '.SUM'):
        print 'no sum file for ' + name[0]
        continue

    # Note: Different from convertLutetiaImagesToFits because of MSB byte order
    command = raw2isis + ' from='+f + ' to=' + name[0] + '.cub' + ' samples='+ nsamples + ' lines=' + nlines + ' bittype=UNSIGNEDWORD' + ' byteorder=MSB'
    print command
    os.system(command)

    command = isis2fits + ' from=' + name[0] + '.cub' + ' to=' + name[0] + ' bittype=32BIT'
    print command
    os.system(command)

    # Added in this step to remove intermediate .cub files and reduce disk usage
    command = 'rm ' + name[0] + '.cub'
    print command
    os.system(command)

    # Rename from .fits to .FIT and move to desired output location
    command = 'mv ' + name[0] + '.fits ' + fitOutputPath + name[0] + '.FIT'
    print command
    os.system(command)
