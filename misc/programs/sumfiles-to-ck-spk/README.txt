------------------------------------------
CK and SPK Kernel Generation from Sumfiles
------------------------------------------

This program generates CK and SPK kernels of a specific instrument from
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

./process_sumfiles [options]
                   <metakernel> 
                   <sumfileList> 
                   <instrumentFrameName> 
                   <spacecraftName>
                   <spacecraftFrameName> 
                   <bodyName> 
                   <bodyFrameName> 
                   <flipX> 
                   <flipY> 
                   <flipZ>
                   [optionalKernels]

The program takes 10 required arguments, optional arguments, and optional
SPICE kernels, which will be loaded after the metakernel.

[options]
-J2000
Setting the optional flag "-J2000" writes the output SPK position
and velocity in the J2000 reference frame, and the output CK attitude
in the J2000 reference frame. If this option is set, the mkspksetupfile
and msopcksetupfile MUST also specify J2000, see below.
-pupil x y z
This optional vector specifies the spacecraft to pupil (camera focal point) 
(x, y, z) position, given in kilometers in the spacecraft reference frame.
It must be provided if sumfile SCOBJ is the pupil to body vector. If not 
provided, it is assumed that sumfile SCOBJ is the spacecraft to body vector.

<metakernel> 
The first argument is the name of a SPICE metakernel file. See the
kernels.txt file included as an example. You will need to modify 
the paths in that file. Neither a spacecraft SPK nor a spacecraft
CK file is required, although if a spacecraft SPK is specified in
the kernel list and velocity is available at the sumfile timestamp,
that velocity will be output to the SPK generated by this program.

<sumfileList> 
The second arguments is the name of a file containing a list of
sumfiles to process, one file per line. See the file sumfilelist.txt
included for an example.

<instrumentFrameName>
The third argument is the SPICE instrument frame name. This can be found
in the SPICE frames kernel. e.g. NEAR_MSI

<spacecraftName>
The fourth argument is the SPICE spacecraft name. e.g. NEAR

<spacecraftFrameName>
The fifth argument is the SPICE spacecraft frame name. This can be found
in the SPICE frames kernel. e.g. NEAR_SC_BUS_PRIME

<bodyName>
The sixth argument is the name of the body in whose frame the sumfile vectors 
are specified, e.g. EROS.

<bodyFrameName> 
The seventh argument is the name of the body frame in which the sumfile vectors 
are specified, e.g. IAU_EROS.

<flipX> <flipY>  <flipZ>
The next three arguments are integer values specifying the orientation of the 
image. flipI = J sets image axis I to image axis J, where I is an axis name 
(X, Y, or Z), and J is a signed integer number for an axis. The values of
J can be (-1, 1, -2, 2, -3, or 3), indicating the (-X, X, -Y, Y, -Z, Z) axis,
respectively. The user must take care to correctly enter the flipI values
so that the resulting flipped axes form a well-defined, right-handed coordinate
system.
Examples:
   (flipX, flipY, flipZ) = ( 1, 2, 3) does not alter the image orientation.
   (flipX, flipY, flipZ) = ( 2,-1, 1) rotates the image 90 degrees about Z.
   (flipX, flipY, flipZ) = (-2, 1, 1) rotates the image -90 degrees about Z.
   (flipX, flipY, flipZ) = ( 1,-2,-3) rotates the image 180 degrees about X.
   
[optionalKernels]
Any arguments after this are assumed to be SPICE kernels. They will be loaded
after the metakernel and as a result will have precedence.

   
Then to run the program type:

./process_sumfiles kernels.txt sumfilelist.txt NEAR_MSI NEAR NEAR_SC_BUS_PRIME EROS IAU_EROS false false false

Two new files will be generated in the current directory called
"msopckinputdata" and "mkspkinputdata".

(The msopckinputdata file will be used as input to the msopck
program. Each line in the msopckinputdata file contains 10 values. The
first is the time in UTC and the next 9 numbers define the spacecraft
orientation matrix at that time. 
The mkspkinputdata file will be used as input to the mkspk program. Each 
line in the mkspkinputdata file contains 7 values. The first is the time 
in UTC and the next 3 numbers define the spacecraft position in body fixed 
coordinates at that time and the final 3 numbers define the spacecraft 
velocity at that time.)


(3) Edit the msopcksetup file

Open up the msopcksetup file (included) and edit the lines beginning
with, if necessary:

    LSK_FILE_NAME         = 'NAIF0007.TLS'  (path to leapseconds kernel)
    SCLK_FILE_NAME        = 'NEAR_171.TSC'  (path to spacecraft clock kernel)
    PRODUCER_ID           = 'JHUAPL'        (your name or institution)
    INSTRUMENT_ID         = -93000          (NAIF id of the spacecraft frame)
    REFERENCE_FRAME_NAME  = 'IAU_EROS'      (NAIF name of asteroid body-fixed 
                                             frame in which the sumfile vectors
                                             are specified)
       ** OR **           
    REFERENCE_FRAME_NAME  = 'J2000'         (If the "-J2000" optional argument was
                                             used in the call to process_sumfiles)

You shouldn't need to modify the other lines.


(4) Edit the mkspksetup file

Open up the mkspksetup file (included) and edit the lines beginning
with, if necessary:

    OBJECT_ID         = -93        (NAIF id of the spacecraft) 
    OBJECT_NAME       = 'NEAR'     (name of the spacecraft)
    CENTER_ID         = 2000433    (NAIF id of the asteroid)
    CENTER_NAME       = 'EROS'     (name of the asteroid) 
    REF_FRAME_NAME    = 'IAU_EROS' (NAIF name of the asteroid body-fixed frame  
                                    in which the sumfile vectors are specified)
       ** OR **       
    REF_FRAME_NAME    = 'J2000'    (If the "-J2000" optional argument was
                                    used in the call to process_sumfiles)
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
the orientation of the spacecraft relative to the asteroid body-fixed
frame.

Note that if you want to rerun this program, make sure to delete the
CK file first since the msopck program simply appends to this file
every time it's run and does not delete it first.


(7) Run the mkspk program to generate the SPK kernel:

Download the program from http://naif.jpl.nasa.gov/naif/utilities.html
and run it, for example, like this:

mkspk -setup mkspksetup -input mkspkinputdata -output spkfile

The file spkfile will be generated which is the SPK kernel file.
Vectors in the SPK are in the asteroid body-fixed frame.

Note that if you want to rerun this program, make sure to delete the
SPK file first since the mkspk program simply appends to this file
every time it's run and does not delete it first.
