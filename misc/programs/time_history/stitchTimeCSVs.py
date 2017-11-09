import sys
import re
import glob
import os

# This function is a helper function for stitching together
# multiple time history CSVs.  If there are breaks in the time history,
# the timeHistory function in C++ will not be able to construct the CSV.
# however, if you construct multiple CSVs with multiple continuous intervals,
# you will be able to stitch them together using this function
# inputs:
#     directory: directory of CSVs
#     fileName: the fileName to save the full CSV
def stitchTimeCSVs(directory, fileName):

	# create file for writing
	fullFile = open(fileName,'w') 

	# copy first over
	# for the remaining, ignore the header and write every other line
	firstFile = True;
	for filename in sorted(glob.glob(directory + '/*timeHistory*.csv')): # ensure we only read CSVs, and in the correct order
		currFile = open(filename, 'r')
		if firstFile: # for first file, copy header
			for line in currFile:
				fullFile.write(line)
			firstFile = False;
		else: # ignore header
			for i, line in enumerate(currFile):
				if i != 0:
				 	fullFile.write(line)
		currFile.close()


	fullFile.close()





if __name__ == "__main__":
	if len(sys.argv) != 3:
		print 'error! you need to provide the directory of CSVs to be stitched together and the outputFileName'
		exit(0)
	directory = sys.argv[1]
	fileName = sys.argv[2]

	stitchTimeCSVs(directory, fileName)
