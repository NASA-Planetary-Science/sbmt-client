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

Refer to the relevant ICDs which explain the pipeline and these programs
in great detail.

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
   from the data folder.

2. Change into the src folder.

3. Open the compile.sh script in a text editor and change the
   SPICE_DIR variable at the top of the script to point to the
   location where you installed the CSPICE library.

4. Run the compile.sh script. This will create 3 executables in the
   src folder: ola-level0-to-level1, ola-level1-to-ck, and
   ola-level1-to-level2

5. Copy these 3 binary executables to the exe folder within the DATA folder.

6. (optional) Copy the ola-pipeline.py script to any location (or run
   it from the location you extracted it to). The ola-pipeline.py is
   main driver program which runs the pipeline.

7. The pipeline requires the msopck program available from the CSPICE
   toolkit. Copy this program from the CSPICE toolkit into the exe
   folder within the DATA folder.

8. You are now ready to run the pipeline. Run the ola-pipeline.py
   script (see step 6). It takes 6 command line arguments, all of them
   required, in the following order:

     1. <data-dir> - path to DATA folder.

     2. <level0-filelist> - path to file containing list of OLA Level
           0 files to process. These files must be present in the
           level0 folder with the DATA folder. Only the level 0
           filenames should be listed, not the full path (i.e. omit
           the folder from the name).  The pipeline looks in this file
           to determine which files to process.

     3. <spice-kernel-meta-file> - path to SPICE meta-kernel
           file. This file should contain all the binary and text
           kernels needed to compute the spacecraft position and
           pointing at any given time.

     4. <spice-lsk-kernel-file> - path to SPICE leap second
           kernel. Note even though a SPICE meta kernel file has
           already been provided, the leap second kernel file must be
           specified explicitly because it is needed by the msopck
           program.

     5. <spice-sclk-kernel-file> - path to SPICE spacecraft clock
           kernel. Note even though a SPICE meta kernel file has
           already been provided, the spacecraft clock kernel file
           must be specified explicitly because it is needed by the
           msopck program.


     6. <spice-frames-kernel-file> - path to SPICE frames kernel. This
           kernel must define the ORX_OLA_HIGH and ORX_OLA_LOW
           frames. Note even though a SPICE meta kernel file has
           already been provided, the frames kernel file must be
           specified explicitly because it is needed by the msopck
           program.


=======
Example
=======

To run the pipeline from within the data folder we have provided, you
could run the following:

../src/ola-pipeline.py . level0-filelist.txt spice-kernels.txt spice/lsk/naif0010.tls spice/sclk/ORX_SCLKSCET.00000.example.tsc spice/fk/orx_ola_v000.tf

Since we are already within the DATA we use a '.' for the data
directory. The second argument, level0-filelist.txt, is a file which
we have provided and it contains the level 0 binary. The third
argument is the spice meta kernel file we have provided and the
remaining 3 arguments are the leap second, spacecraft clock and frames
kernel, respectively.

On completion of this command, the level1 folder will contain the
files OLASCIL120194.TAB and OLASCIL120194.XML, the ck folder will
contain the file OLA20194.bc and the level2 folder will contain the
files OLASCIL220194.TAB OLASCIL220194.XML


======================================
Explanation of Contents of DATA Folder
======================================

These folders need to be created inside the DATA prior to running the
pipeline.

ROOT/exe       - executables compiled as described above go here
ROOT/level0sci - level 0 science data should be placed here prior to running the pipeline
ROOT/level1sci - level 1 science data is placed here
ROOT/level2    - level 2 data is placed here
ROOT/ck        - OLA ck spice kernel files are placed here
ROOT/tmp       - temporary files go here

Note that the data folder we have provided contains a spice folder
which contains all the SPICE files needed by the pipeline. This folder
can be located anywhere and we have placed it in the data forlder for
convenience, since the SPICE meta-kernel file which must be provided
as a command line argument can contain to paths to kernels anywhere on
disk.


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
