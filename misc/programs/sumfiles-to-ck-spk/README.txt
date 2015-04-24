------------------------------------------
CK and SPK Kernel Generation from Sumfiles
------------------------------------------

This program generates CK and SPK kernels of a specific instrment from
a list of sumfiles (usually provided by Bob Gaskell as a byproduct of
his SPC code).

To generate the kernels several files are needed, which we provide:

process_sumfiles.cpp - C++ source code of program
compile.sh -           shell script to compile C++ code
kernels.txt -          Spice metakernel file
sumfilelist.txt -      list of sumfiles
msopcksetup -          msopcksetup file
mkspksetup -           mkspksetup file

In addition you need to download the C version of spice as well as the
msopck and mkspk programs which can be found at
http://naif.jpl.nasa.gov/naif/utilities.html. You will also need a C++
compiler (g++).

Here are the steps needed to generate the kernels:


(1) Compile C++ code:

To compile the code you need to open up the compile.sh file and change the
SPICE_DIR variable to the folder containing your C spice
installation. Then run the script: e.g.

./compile.sh

and the file process_sumfiles will be created


(2) Run process_sumfiles

Run the process_sumfiles executable. This program takes 3
arguments:

The first argument is the name of a file containing a list of spice
kernel files to use in spice's metakernal file format. See the
kernels.txt file included as an example (not sure if all of those
kernels are really needed). You will need to modify the paths in that
file.

The second arguments is the name of a file containing a list of
sumfiles to process, one file per line. See the file sumfilelist.txt
included for an example.

The third argument is the name of the SPICE frame of the asteroid,
with respect to which the orientation matrix in the sumfile is given,
e.g. IAU_EROS.

Then to run the program type:

./process_sumfiles kernels.txt sumfilelist.txt IAU_EROS

Two new files will be generated in the current directory called
"msopckinputdata" and "mkspkinputdata".

(The msopckinputdata file will be used as input to the msopck
program. Each line in the msopckinputdata file contains 10 values. The
first is the time in UTC and the next 9 numbers define the orientation
matrix at that time. The mkspkinputdata file will be used as input to
the mkspk program. Each line in the msopckinputdata file contains 7
values. The first is the time in UTC and the next 3 numbers define the
spacecraft position in body fixed coordinates at that time and the
final 3 numbers define the spacecraft velocity at that time.)


(3) Edit the msopcksetup file

Open up the msopcksetup file (included) and edit the lines beginning
with, if necessary:

LSK_FILE_NAME (path to leap second kernel),
SCLK_FILE_NAME (path to spacraft clock kernel),
PRODUCER_ID (your name)
INSTRUMENT_ID (id of instrument).

You shouldn't need to modify the other lines.


(4) Edit the mkspksetup file

Open up the mkspksetup file (included) and edit the lines beginning
with, if necessary:

        OBJECT_ID         = -93
        OBJECT_NAME       = 'NEAR'
        CENTER_ID         = 2000433 
        CENTER_NAME       = 'EROS'
        REF_FRAME_NAME    = 'IAU_EROS'
        PRODUCER_ID       = 'JHUAPL'
        LEAPSECONDS_FILE  = '/project/nearsdc/spice-kernels/near/LSK/NAIF0007.TLS'

You shouldn't need to modify the other lines.


(5) Run the msopck program to generate the CK kernel:

Download the program from http://naif.jpl.nasa.gov/naif/utilities.html
and run it. It takes 3 arguments. The first is the
msopcksetup file which you edited in Step 3. The second is the
msopckinputdata file you generated in Step 2. The third is the name
you would like the new CK kernel file to have. For example:

./msopck msopcksetup msopckinputdata ckfile

The file ckfile will be generated which is the CK kernel file storing
the orientation of the instrument relative to J2000.

Note that if you want to rerun this program, make sure to delete the
CK file first since the msopck program simply appends to this file
every time it's run and does not delete it first.

(6) Run the mkspk program to generate the SPK kernel:

Download the program from http://naif.jpl.nasa.gov/naif/utilities.html
and run it, for example, like this:

mkspk -setup mkspksetup -input mkspkinputdata -output spkfile

The file spkfile will be generated which is the SPK kernel file.

Note that if you want to rerun this program, make sure to delete the
SPK file first since the mkspk program simply appends to this file
every time it's run and does not delete it first.
