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
ck_comments.txt -      text that will be written to the comment area
                       of the CK file
spk_comments.txt -     text that will be written to the comment area
                       of the SPK file

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

Program usage is given on the command line if the program is
executed without any parameters:

./process_sumfiles <kernelFiles> <sumfileList> <instrumentFrameName> <spacecraftFrameName> <boolean flipX> <boolean flipY> <boolean flipZ>

The program takes 7 arguments:

The first argument is the name of a SPICE metakernel file. See the
kernels.txt file included as an example. You will need to modify 
the paths in that file.

The second arguments is the name of a file containing a list of
sumfiles to process, one file per line. See the file sumfilelist.txt
included for an example.

The third argument is the SPICE instrument frame name. This can be found
in the SPICE frames kernel.

The fourth argument is the SPICE spacecraft frame name. This can be found
in the SPICE frames kernel.

The last three arguments are boolean values (true or false), specifying
whether to flip the orientation relative the the instrument axis in the
parameter name. The X and Y flips defines the rotation about the boresight. 
The Z flip determines whether the images are oriented looking into the
instrument or looking out. Setting all three flips to false has the 
boresight looking out of the instrument, increasing pixels (instrument X) 
to the right, and increasing lines (instrument Y) down.

Then to run the program type:

./process_sumfiles kernels.txt sumfilelist.txt NEAR_MSI NEAR_SC_BUS_PRIME false false false

Two new files will be generated in the current directory called
"msopckinputdata" and "mkspkinputdata".

(The msopckinputdata file will be used as input to the msopck
program. Each line in the msopckinputdata file contains 10 values. The
first is the time in UTC and the next 9 numbers define the spacecraft
orientation matrix at that time. The mkspkinputdata file will be used 
as input to the mkspk program. Each line in the msopckinputdata file 
contains 7 values. The first is the time in UTC and the next 3 numbers 
define the spacecraft position in body fixed coordinates at that time 
and the final 3 numbers define the spacecraft velocity at that time.)


(3) Edit the msopcksetup file

Open up the msopcksetup file (included) and edit the lines beginning
with, if necessary:

    LSK_FILE_NAME        = 'NAIF0007.TLS'  (path to leapseconds kernel)
    SCLK_FILE_NAME       = 'NEAR_171.TSC'  (path to spacecraft clock kernel)
    PRODUCER_ID          = 'JHUAPL'        (your name or institution)
    INSTRUMENT_ID        = -93000          (NAIF id of the spacecraft frame)
    REFERENCE_FRAME_NAM  = 'IAU_EROS'      (NAIF name of asteroid body-fixed frame)

You shouldn't need to modify the other lines.


(4) Edit the mkspksetup file

Open up the mkspksetup file (included) and edit the lines beginning
with, if necessary:

    OBJECT_ID         = -93        (NAIF id of the spacecraft) 
    OBJECT_NAME       = 'NEAR'     (name of the spacecraft)
    CENTER_ID         = 2000433    (NAIF id of the asteroid)
    CENTER_NAME       = 'EROS'     (name of the asteroid)
    REF_FRAME_NAME    = 'IAU_EROS' (name of the asteroid body-fixed frame)
    PRODUCER_ID       = 'JHUAPL'
    LEAPSECONDS_FILE  = 'NAIF0007.TLS'

You shouldn't need to modify the other lines.


(5) Modify the ck_comments.txt and spk_comments.txt files (included)

At a minimum update the Contacts section with your contact information. 
The text in these files will be written to the comments section of the
output CK and SPK.


(6) Run the msopck program to generate the CK kernel:

Download the program from http://naif.jpl.nasa.gov/naif/utilities.html
and run it. It takes 3 arguments. The first is the
msopcksetup file which you edited in Step 3. The second is the
msopckinputdata file you generated in Step 2. The third is the name
you would like the new CK kernel file to have. For example:

msopck msopcksetup msopckinputdata ckfile

The file ckfile will be generated which is the CK kernel file storing
the orientation of the spacecraft relative to J2000.

Note that if you want to rerun this program, make sure to delete the
CK file first since the msopck program simply appends to this file
every time it's run and does not delete it first.


(7) Run the mkspk program to generate the SPK kernel:

Download the program from http://naif.jpl.nasa.gov/naif/utilities.html
and run it, for example, like this:

mkspk -setup mkspksetup -input mkspkinputdata -output spkfile

The file spkfile will be generated which is the SPK kernel file.

Note that if you want to rerun this program, make sure to delete the
SPK file first since the mkspk program simply appends to this file
every time it's run and does not delete it first.
