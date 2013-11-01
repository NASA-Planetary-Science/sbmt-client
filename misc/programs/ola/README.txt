========
Overview
========

This software contains part of the OLA data pipeline. Currently it
consists of the following components:

1. a program for converting OLA science level 0 data to OLA science
level 1 data.
2. a program for generating the input files from the level 1 data
required by the msopck program. The msopck program is used to generate
a CK kernel file of the OLA scanning platform.
3. a program for converting OLA science level 1 data to OLA level 2
data.

Refer to the relevant ICDs which explain the pipeline and these
programs in great detail as well as the required directory structure
and files that need to be present in order for the pipeline to work
(such as the SPICE metakernel file and a list of level 0 files to
process).

The above 3 programs are written in C and make up the building blocks
of the OLA pipleline. In addition, a python script is provided (called
ola-pipeline.py) which is responsible for taking the level 0 data file and
running the above programs to generate the level 1, level 2 and CK
kernel files. The python script acts as the glue which ties together
the above programs. The user does not execute one of the above 3
programs directly but instead only needs to run the python script to
run the pipeline.

The software is organized as follows. The top level folder contains
this this README.txt file. The 'src' folder contains the C and python code.
The 'data' folder contains sample data for testing the code.


============
Instructions
============

Prerequisites: You will need the CSPICE library and a C compiler in
order to compile this program. The CSPICE library is available at
http://naif.jpl.nasa.gov/naif/toolkit.html.

Steps to compile the programs and run the pipeline with fake data we
have provided:

1. (optional) Copy the contents of the data folder to any location on
   disk. The test folder contains the directory structure required by
   the pipeline. The pipeline expects these directories to be
   present. From here onwards we will call this folder the DATA
   folder. This step is optional: you could run the pipeline directly
   from the data folder. See the ICDs for more details about the DATA
   folder and how it is organized.

2. Change into the src folder.

3. Open the compile.sh script in a text editor and change the
   SPICE_DIR variable at the top of the script to point to the
   location where you installed the CSPICE library.

4. Run the compile.sh script. This will create 3 executables in the
   src folder: ola-level0-to-level1, ola-level1-to-ck, and
   ola-level1-to-level2.

5. Copy these 3 binary executables to your PATH.

6. (optional) Copy the ola-pipeline.py script to any location (or run
   it from the location you extracted it to). The ola-pipeline.py is
   main driver program which runs the pipeline.

7. The pipeline requires the msopck program available from the CSPICE
   toolkit. Add this file to your PATH.

8. You are now ready to run the pipeline. Run the ola-pipeline.py
   script (see step 6). It takes a single command line arguments:

     <data-dir> - path to DATA folder.


=======
Example
=======

The SPOC will setup the directory structure required to run the
pipeline. To test our code however, we also provide a folder
hierarchy which contains all the necessary files and folders. This is
located in the 'data' folder.

Within that folder, the file 'level0-filelist.txt' contains a list of
level 0 files that need to processed.

To run the pipeline using this data folder we have provided, you
could run the following (assuming you are within this folder):

../src/ola-pipeline.py .

Since we are already within the DATA folder, we use a '.' for the data
directory.

On completion of this command, the L1 folder will contain the files
orx_ola_scil1_t00002_200720.tab and orx_ola_scil1_t00002_200720.xml,
the CK folder will contain the file orx_ola_t00002_200720.bc and the
L2 folder will contain the files orx_ola_scil2_t00002_200720.tab and
orx_ola_scil2_t00002_200720.xml.


=======
Contact
=======

For questions about this software please contact:

Eli Kahn
eliezer.kahn@jhuapl.edu
(443) 778-2324

or

Olivier Barnouin
olivier.barnouin@jhuapl.edu
(443) 778-7654
