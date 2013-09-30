% OLA Tools
%
%

<!--- To convert this markdown to HTML use this command (pandoc required)
pandoc -t html -s index.md -o index.html
-->

## Convert OLA Level 1 lidar data to Level 2

This program is intended to convert OLA level 1 data to level 2 data.
Since the format of OLA level 1 and 2 data has not yet been finalized,
the program currently works with Hayabusa lidar data (EDR) only and
converts it to a format that will be similar to OLA level 2
data. Additional details as well as usage instructions can be found in
the README.txt file contained in the following zip archive.

Download: [ola-level1-to-level2-v1.0.zip](releases/ola-level1-to-level2-v1.0.zip)


## Lidar Track Optimization

This tool can be used to determine the optimal translation that
minimizes the error of a lidar track to the asteroid. Currently lidar
data from the NEAR or Hayabusa missions are supported. Please see the
README.txt file in the zip archive for usage instructions.

Download: [lidar-opt-1.0.0.zip](releases/lidar-opt-1.0.0.zip)
