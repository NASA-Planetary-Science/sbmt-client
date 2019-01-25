#!/usr/bin/python

import glob
import os
import sys

path = os.path.abspath(os.path.dirname(sys.argv[0]))


def make_webpage(file_glob, input_dir, output_dir, title, valid_file_list):

    # Create the output directory in case it doesn't exist.
    os.system("mkdir -p " + output_dir)

    # Copy the javascript resources to the output directory
    os.system("cp " + path+"/main.js " + path+"/jquery.js " + output_dir)

    # Change directory to the input directory and get the list of files to process.
    os.chdir(input_dir)
    files = sorted(glob.glob(file_glob))

    # Start writing the web page that will display the gallery to the output directory.
    index_html = output_dir + "/index.html"
    fout = open(index_html, 'w')

    header = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>"""
    header += title
    header += """</title>
<meta name="description" content="Easiest jQuery Tooltip Ever">
<script src="jquery.js" type="text/javascript"></script>
<script src="main.js" type="text/javascript"></script>
</meta>


<style>
body {
	margin:0;
	padding:40px;
	background:#fff;
	font:80% Arial, Helvetica, sans-serif;
	color:#555;
	line-height:180%;
}

h1{
	font-size:180%;
	font-weight:normal;
	color:#555;
}
h2{
	clear:both;
	font-size:160%;
	font-weight:normal;
	color:#555;
	margin:0;
	padding:.5em 0;
}
a{
	text-decoration:none;
	color:#f30;
}
p{
	clear:both;
	margin:0;
	padding:.5em 0;
}
pre{
	display:block;
	font:100% "Courier New", Courier, monospace;
	padding:10px;
	border:1px solid #bae2f0;
	background:#e3f4f9;
	margin:.5em 0;
	overflow:auto;
	width:800px;
}

img{border:none;}
ul,li{
	margin:0;
	padding:0;
}
li{
	list-style:none;
	float:left;
	display:inline;
	margin-right:10px;
}



/*  */

#preview{
	position:absolute;
	border:1px solid #ccc;
	background:#333;
	padding:5px;
	display:none;
	color:#fff;
	}

/*  */
</style>
</head>
<body>
"""

    fout.write(header)

    fout.write("<h1>" + title + "</h1>\n")
    fout.write("<ul>\n")

    for f in files:
        #print "processing " + f
        if valid_file_list != None:
            if not any(valid_file in f for valid_file in valid_file_list):
                continue
        
        thumbnail = f+"-small.jpeg"
        #command = "convert -resize 200x200 " + f + " " + output_dir+"/"+thumbnail
        #print command
        #os.system(command)

        fullsize = f+".jpeg"
        #command = "convert " + f + " " + output_dir+"/"+fullsize
        #print command
        #os.system(command)

        fout.write("<li><a href=\""+fullsize+"\" class=\"preview\" title=\""+fullsize+"\"><img src=\""+thumbnail+"\" alt=\"gallery thumbnail\" /></a></li>\n")

    fout.write("</ul>\n")
    fout.write("</body>\n</html>")

    fout.close()

##########################################################################



#file_glob = "*.FIT"
#input_dir = "./images"
#output_dir = "./output"
#title = "My Images"
#file_list = "files_to_process.txt"
file_glob = sys.argv[1]
input_dir = sys.argv[2]
output_dir = sys.argv[3]
title = sys.argv[4]

valid_file_list = None
if len(sys.argv) >= 6:
	with open(sys.argv[5]) as f:
		valid_file_list = [line.strip() for line in f]

make_webpage(file_glob, input_dir, output_dir, title, valid_file_list)
