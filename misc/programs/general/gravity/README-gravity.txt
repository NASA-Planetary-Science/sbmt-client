------------
Introduction
------------
This README file describes how to build and run the gravity and
elevation-slope programs for computing the gravitational acceleration,
potential, elevation, and slope of an arbitrary triangular plate
model.  It implements the algorithm by Werner and Scheeres[1] as well as
the simpler, faster algorithm by Andy Cheng[2].


------------------------
Compilation instructions
------------------------
Precompiled binaries are provided for 64-bit Intel based macs running
10.6 or higher. To build it from source, unzip and change into the folder
called 'source' and run the compile.sh shell script. This will create
the gravity and elevation-slope executables in the current
directory. Note that on Windows platforms you will likely need cygwin
and/or mingw.


----------------------------
Gravity program instructions
----------------------------
This gravity executable takes various options as well as the path to
plate a model (in typical PDS format). It computes the potential,
acceleration, and acceleration magnitude and saves the values to files
in the current directory. You can choose between the Werner and Andy
Cheng's method. You can also choose between evaluating at the center
of each plate, or evaluating at the vertices of the shape model and
then averaging the values at the vertices for each plate. You need to
provide the density (in g/cm^3) and rotation rate (in radians/per
second). It is assumed that the vertices are expressed in kilometers
and on output the potential is expressed in J/kg and the acceleration
in m/s^2. The gravity program takes the following options:

 -d <value>              Density of shape model in g/cm^3 (default is 1)
 -r <value>              Rotation rate of shape model in radians/sec (default is 0)
 --werner                Use the Werner algorithm for computing the gravity (this is the
                         default if neither --werner or --cheng option provided)
 --cheng                 Use Andy Cheng's algorithm for computing the gravity (default is to
                         use Werner method if neither --werner or --cheng option provided)
 --centers               Evaluate gravity directly at the centers of plates (this is the default
                         if neither --centers or -vertices or --file option provided)
 --vertices              Evaluate gravity of each plate by averaging the gravity computed at the
                         3 vertices of the plate (default is to evaluate at centers)
 --file <filename>       Evaluate gravity at points specified in file (default is to evaluate
                         at centers)
 --ref-potential <value> If the --file option is provided, then use this option to specify the reference
                         potential which is needed for calculating elevation. This option is ignored if
                         --file is not provided. If --file is provided but --ref-potential is not
                         provided then no elevation data is saved out.
 --columns <int,int,int> If --file is provided, then this options controls which columns of the file are
                         assumed to contain the x, y, and z coordinates of the points. By default, columns
                         0, 1, and 2 are read. If you wanted, say, columns 3, 4, and 5 instead you would
                         include this option as for example: --columns 3,4,5. Note that they are separated
                         by commas (no spaces) and are zero based. If --file is not provided, then this
                         option is ignored.
 --start-plate <value>
 --end-plate <value>     use these 2 options to specify a range of plates to process. For example if
                         --start-plate is 1000 and --end-plate is 2000, then only plates 1000 through
                         1999 are processed. This is useful for parallelizing large shape models on
                         multiple machines. These options are ignored if --file option is provided

(If you run it with no arguments, this is printed out).

So for example, to compute the gravity at all plate centers using the
Werner method for Itokawa, you would do:

./gravity -d 1.95 -r .000143857148947075 ver64q.tab

where -d 1.95 specifies the density, and -r .000143857148947075
specifies the rotation rate, and ver64q.tab contains the shape
model. There is no need to specify --werner or --centers since these
are the default values. On output you should see the files
ver64q.tab-potential.txt, ver64q.tab-acceleration.txt,
ver64q.tab-acceleration-magnitude.txt in the current directory which
contain the computed potential, acceleration and acceleration
magnitude.

To run using Andy Cheng's method instead, you would type:

./gravity -d 1.95 -r .000143857148947075 --cheng ver64q.tab

Note the addition of the --cheng option.

The program also prints out timing information.

In addition there's an option for computing the potential and
acceleration at arbitrary coordinates rather than at the vertices and
centers of the plates. To use this option, specify the --file options
followed by the path to a file containing a list of coordinates in
body fixed coordinates, with one coordinate per line. For example, the
following is a valid file:
-0.15153        0.08183        0.07523
-0.14726        0.08251        0.07657
-0.14288        0.08309        0.07759
-0.13851        0.08366        0.07864
-0.13436        0.08447        0.08038
By default the first 3 columns of the file are assumed to contain the
x, y, and z coordinates of the points. If the data is contained in
other columns, use the --columns option to specify which columns
should be read. For example if the data is contained in the 4th, 5th,
and 6th columns, you would add this option:
--columns 3,4,5
Note the column numbers are zero based (so 0 is the first columns).

Note also that if the --file option is provided and you also provide a
value for the reference potential with the --ref-potential option,
then the elevation is also computed for each point and is saved out in
a separate file (In the example above, this would be
ver64q.tab-elevation.txt). Note that elevation is only evaluated this
way when you have a list of arbitrary points using the --file
option. To compute elevation and slope at all plate center, use the
elevation-slope program, described below.

Note also to compute the reference potential for an arbitrary shape
model (which you would need to compute elevation at arbitrary points),
run the elevation-slope program described below and the reference
potential value will be printed out to standard output.


------------------------------------
Elevation-slope program instructions
------------------------------------
The gravity program computes the potential and acceleration at every
plate center, but not elevation and slope (unless the --file and
--ref-potential options are used in which case the elevation is
computed as well). The elevation-slope program is used for computing
the elevation and slope at all plate centers. It uses the output files
generated by the gravity program takes exactly 3 arguments as follows:

elevation-slope <platemodel-file> <potential-file> <acceleration-vector-file>

The first argument is the path to the plate model file as in the
gravity program.

The second argument is the potential file which was generated by the
gravity program. For instance, in the example above, this would be the
file ver64q.tab-potential.txt.

The third argument is the acceleration vector (not magnitude) file
which was generated by the gravity program. For instance, in the
example above, this would be the file ver64q.tab-acceleration.txt.

On output, the files ver64q.tab-elevation.txt and ver64q.tab-slope.txt
are generated in the current directory which contain the elevation and
slope, respectively, for each plate.

Thus for example, you could run

./elevation-slope ver64q.tab ver64q.tab-potential.txt ver64q.tab-acceleration.txt

and the files ver64q.tab-elevation.txt and ver64q.tab-slope.txt would
be created in the current directory.

Note again as explained above, that the value of the reference
potential is printed out to standard output when running this
program. You could use this value for the --ref-potential option for
computing elevation at arbitrary points.

------------------------------------
References
------------------------------------
[1] Werner, R.A. and Scheeres, D.J., 1997, Exterior gravitation of a
polyhedron derived and compared with harmonic and mascon gravitation
representations of asteroid 4769 Castalia, Celestial Mechanics and
Dynamical Astronomy, Vol. 65, pp. 313-344.
[2] Cheng, A.F. et al., 2012, Efficient Calculation of Effective
Potential and Gravity on Small Bodies, ACM, 1667, p. 6447.
