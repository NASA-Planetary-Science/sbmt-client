===============================================================================
sbmt/misc/server/README.txt
===============================================================================
This folder contains the source files for creating the static html files for
the internal and external SBMT websites. 


Requirements:
-------------
 - tbd ----make sure 'nanoc' and 'pandoc' are in your path


To build the external website locally for testing, do the following:
--------------------------------------------------------------------

  cd sbmt/misc/server/public
  nanoc
  open output/index.html in your favorite browswer.


To build the internal website locally for testing, do the following:
-------------------------------------------------------------------

  cd sbmt/misc/server/internal
  nanoc
  open output/index.html in your favorite browswer.


KNOWN ISSUES as of 3/31/2016 when testing locally.
--------------------------------------------------

The 'Tutorial' link on the left column of the internal website pages is broken. 
This is because the SBMT-tutorial.pdf file is hardcoded to be accessible in the 
current directory.  But, it actually lives in the /disks/d0180/htdocs-sbmt/internal
directory, and it does not exist for the external website. But, you can work
around this problem for testing purposes simply by creating a symbolic link to 
the SBMT-tutorial.pdf file in the sbmt/misc/server/internal/output directory.

The 'Releases' link on the left column of the internal website pages is broken.
This is because the html file expects there to be a 'releases' folder in the 
current directory. But, it actually lives in the /project/nearsdc folder. You
can work around this problem for testing purposes by creating a symbolic link
to the /project/nearsdc/releases folder in the sbmt/misc/server/internal/output 
directory.

The 'Data' link on the left column of the internal website pages is broken.
This is because the html file expects there to be a 'sbmt/data' folder in the 
current directory. But, it actually lives in the
/disks/d0180/htdocs-sbmt/internal/sbmt folder. You
can work around this problem for testing purposes by creating a symbolic link
to the /disks/d0180/htdocs-sbmt/internal/sbmt folder in the sbmt/misc/server/internal/output 
directory.

The 'build.sh' script in each of the sbmt/misc/server/internal and 
sbmt/misc/server/public folders do not work, and I don't believe they
are needed.


How it works
------------

The internal and external websites are static websites created and managed
using 'nanoc' (http://nanoc.ws/). 'nanoc' requires a 'nanoc.yaml' file and 
a 'Rules' file to be in the current directory. 'nanoc' uses the 'pandoc' 
program and a ruby gem called 'pandoc-ruby' to convert the markdown files 
(*.md) to *.html files. And then all of the html files are saved in the 
'output' directory (nanoc creates this directory if it doesn't already exist).


How to deploy to the webpages to the live website
-------------------------------------------------

Once the new html files are created in the sbmt/misc/server/public/output and
sbmt/misc/server/internal/output directories, you will need to use 'sed' to 
insert the release date into the index.html and installation.html pages in 
both the public/output and internal/output directories as shown, where  
'TODAYSDATE' is the date string used to make the client release zip files.

sed -i "" "s/VERSIONXXXXXX/${TODAYSDATE}/g" output/index.html output/installation.html

After inserting the release date into those files, then you will need to copy
the contents of the sbmt/misc/server/sbmt/public/output directory to
/disks/d0180/htdocs-sbmt/ directory, and copy the contents of the 
sbmt/misc/server/sbmt/internal/output to /disks/d0180/htdocs-sbmt/internal
directory.


VAM TBD Need to figure out what to do with or describe the tools and query directories.
