#!/usr/bin/python

import glob
import os

# Note: This script creates image lists for both fixedList and generalPhp queries

# Setup
sumFilePath = '/project/nearsdc/misc/comet/67P_CGW/SUMFILES/'
generalQueryPath = '/GASKELL/67P_V2/IMAGING/images/'

# Function to convert .SUM file month abbrev. to numerical label for imagelist
def convertMonthLabel(abbrev):
    if abbrev == 'JAN':
        return '-01-'
    elif abbrev == 'FEB':
        return '-02-'
    elif abbrev == 'MAR':
        return '-03-'
    elif abbrev == 'APR':
        return '-04-'
    elif abbrev == 'MAY':
        return '-05-'
    elif abbrev == 'JUN':
        return '-06-'
    elif abbrev == 'JUL':
        return '-07-'
    elif abbrev == 'AUG':
        return '-08-'
    elif abbrev == 'SEP':
        return '-09-'
    elif abbrev == 'OCT':
        return '-10-'
    elif abbrev == 'NOV':
        return '-11-'
    elif abbrev == 'DEC':
        return '-12-'
    else:
        print 'ERROR: Month abbreviation ' + abbrev + ' not recognized!'


# Main function
files = glob.glob(sumFilePath + '*.SUM')

# Open output files for writing
fixedQueryFile = open('imagelist.txt', 'w')
generalQueryFile = open('imagelist-fullpath.txt', 'w')

for f in files:

    # Skip files that start w. ge for some reason, all the other imagelists do it too
    if f.startswith('ge'):
        continue

    # Read and split first three lines of the SUM file
    fin = open(f,'r')
    firstLine = fin.readline().split()
    secondLine = fin.readline().split()
    thirdLine = fin.readline().split()

    # Only do it for images of size 2048x2048
    if thirdLine[0] != '2048' or thirdLine[1] != '2048':
        continue

    # Convert month label
    monthLabel = convertMonthLabel(secondLine[1])
    
    # Write line to fixed query image list
    fixedQueryFile.write(firstLine[0] + '.FIT ' + secondLine[0] + monthLabel + secondLine[$

    # Write line for general query image list
    generalQueryFile.write(generalQueryPath + firstLine[0] + '.FIT' + '\n')

# Close the files and flush buffers
fixedQueryFile.close()
generalQueryFile.close()
    