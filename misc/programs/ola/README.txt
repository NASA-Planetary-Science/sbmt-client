========
Overview
========

This program is intended to convert OLA level 1 data to level 2 data.
This program takes raw lidar data (i.e. a file only containing times
in UTC and range) as well as a SPICE metakernel file and computes a
new file which contains the actual target point of the lidar in body
fixed coordinates.

Since the format of OLA level 1 and 2 data has not yet been finalized,
the current program currently works with Hayabusa lidar data (EDR)
only and converts it to a format that will be similar to OLA level 2
data. This data is available at
http://sbn.psi.edu/pds/resource/haylidar.html in the folder data/edr/.

In particular, this program assumes an input file with the following
columns:
1. MET
2. UTC
3. range

The output file will contain the following columns:
1. MET
2. UTC
3. range
4. x target position in body fixed coordinates
5. y target position in body fixed coordinates
6. z target position in body fixed coordinates
7. longitude of target position in body fixed coordinates
8. latitude of target position in body fixed coordinates
9. radius of target position in body fixed coordinates

Currently the input and output is in ascii though this will need to
change to binary.

Note that if there is no SPK of CK data at a given time, then that
line is skipped in the output file.

==============
How to Compile
==============

Prerequisites: You will need the SPICE library and a C compiler in
order to compile this program.

The folder containing this README file also contains a compile.sh
shell script which can be used to compile this program. Please open
that file and read the comments at the top for instructions on using
the script.

=====
Usage
=====

Usage: ola-level1-to-level2 <kernel-metafile> <input-file> <output-file>

Where,
<kernel-metafile> - path to the SPICE kernel metafile
<input-file> - path to input level 1 lidar data
<output-file> - path to desired output level 2 lidar data

Example:

./ola-level1-to-level2 kernels.txt edr20050930_20051028.tab output.txt
