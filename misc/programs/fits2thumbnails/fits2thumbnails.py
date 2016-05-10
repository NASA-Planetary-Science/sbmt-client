#!/usr/bin/python

# This script was adapted from Eli's original make_image_gallery.py script. This
# script simply makes the thumbnail and fullsize jpeg images for the *.FIT files
# listed in valid image file list.

import glob
import os
import sys

path = os.path.abspath(os.path.dirname(sys.argv[0]))
file_glob = "*.FIT"

def make_thumbnails( input_dir, output_dir, valid_file_list):

	# Create the output directory in case it doesn't exist.
	os.system("mkdir -p " + output_dir)

	# Change directory to the input directory and get the list of files to process.
	os.chdir(input_dir)
	files = sorted(glob.glob(file_glob))

	for f in files:
		print "processing " + f
		if valid_file_list != None:
			if not any(valid_file in f for valid_file in valid_file_list):
				continue
		thumbnail = f+"-small.jpeg"
		command = "convert -resize 200x200 " + f + " " + output_dir+"/"+thumbnail
		print command
		os.system(command)

		fullsize = f+".jpeg"
		command = "convert " + f + " " + output_dir+"/"+fullsize
		print command
		os.system(command)

    
##########################################################################


#input_dir = "/path/to/images_dir"
#output_dir = "/path/to/gallery_dir"
#file_list = "/path/to/list/of/files/to/process"

input_dir = sys.argv[1]
output_dir = sys.argv[2]

valid_file_list = None
if len(sys.argv) >= 4:
	with open(sys.argv[3]) as f:
		valid_file_list = [line.strip() for line in f]

make_thumbnails(input_dir, output_dir, valid_file_list)
