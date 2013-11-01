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
(such as the SPICE meta-kernel file and a list of level 0 files to
process).

The above 3 programs are written in C and make up the building blocks
of the OLA pipeline. In addition, a python script is provided (called
ola-pipeline.py) which is responsible for taking the level 0 data file
and running the above programs to generate the level 1, level 2 and CK
kernel files. The python script acts as the glue which ties together
the above programs. The user does not execute one of the above 3
programs directly but instead only needs to run the ola-pipeline.py
script to run the pipeline.

The software is organized as follows. The top level folder contains
this README.txt file. The 'src' folder contains the C and python code.
The 'data' folder contains sample data for testing the code. Note that
the SPOC will setup the directory structure required to run the
pipeline. To test our code however, the data folder contains an
example hierarchy which contains all the necessary files and folders.


===================
System Requirements
===================

- UNIX platform such as a recent Linux distribution or Mac OS X
- Recent GCC compiler
- Python (tested with version 2.7)
- CSPICE library (available at http://naif.jpl.nasa.gov/naif/toolkit.html)


============
Instructions
============

Steps to compile the programs and run the pipeline with fake data we
have provided:

1. Change into the src folder.

2. Open the compile.sh script in a text editor and change the
   SPICE_DIR variable at the top of the script to point to the
   location where you installed the CSPICE library.

3. Run the compile.sh script. This will create 3 executables in the
   src folder: ola-level0-to-level1, ola-level1-to-ck, and
   ola-level1-to-level2.

4. Add these 3 binary executables and the ola-pipeline.py script to
   your PATH. The ola-pipeline.py is main driver program which runs
   the pipeline.

5. The pipeline requires the msopck program available from the CSPICE
   toolkit. Add this file to your PATH.

6. You are now ready to run the pipeline. Change into the data folder
   (it will not work from another directory) and run the
   ola-pipeline.py script. In an operational setting, the data folder
   will be located somewhere else.

   Note that the script assumes that the level 0 files to be processed
   are located in the file SciData/OLA/level0-filelist.txt, one file
   per line (filename only, not folder in which it is in). It is the
   responsibility of the upstream modules of the pipeline to populate
   this file. This script also assumes that a spice meta-kernel file
   is located at SPICE/spice-kernels.mk. It is the responsibility of
   the upstream modules of the pipeline to make sure this file is up
   to date.

   On completion of this command using the provided data, the L1
   folder will contain the files orx_ola_scil1_t00002_200720.tab and
   orx_ola_scil1_t00002_200720.xml, the CK folder will contain the
   file orx_ola_t00002_200720.bc and the L2 folder will contain the
   files orx_ola_scil2_t00002_200720.tab and
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
