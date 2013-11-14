#!/usr/bin/python

import glob
import os

files = glob.glob('*.SUM')

def getMonthAsNumber(mon):
    if mon == 'JAN':
        return '01'
    elif mon == 'FEB':
        return '02'
    elif mon == 'MAR':
        return '03'
    elif mon == 'APR':
        return '04'
    elif mon == 'MAY':
        return '05'
    elif mon == 'JUN':
        return '06'
    elif mon == 'JUL':
        return '07'
    elif mon == 'AUG':
        return '08'
    elif mon == 'SEP':
        return '09'
    elif mon == 'OCT':
        return '10'
    elif mon == 'NOV':
        return '11'
    else:
        return '12'


for f in files:

    if f.startswith('ge'):
        continue

    fin = open(f,'r')
    firstLine = fin.readline().split()
    secondLine = fin.readline().split()

    # Ignore sumfiles with no landmarks
#    if os.path.getsize(f) < 1300:
#        continue

    month = getMonthAsNumber(secondLine[1])
    print firstLine[0]+'.FIT '+secondLine[0]+'-'+month+'-'+secondLine[2]+'T'+secondLine[3]
