------------------------------------------------------------------------------------------------------------------------------------
Script/code: createInfoFiles
Author: James Peachey, based heavily on previous work by Alex Welsh, Russell Turner, Josh Steele, Lil Nguyen and others.
------------------------------------------------------------------------------------------------------------------------------------
This directory contains a top level shell script, createInfoFiles.sh, that builds and runs the compiled C/C++ program
createInfoFiles in the createInfoFiles/ subdirectory. The purpose of the script/program is to generate a set of
INFO files for a provided collection of images, deriving the pointing information for the INFO files from a provided
set of SPICE kernels. This code is based on previous mission-specific code that was used for Osiris-REx, Hayabusa-2
and many other previous planetary body missions. This version is designed to be completely indepdendent of
mission-specific conventions and assumptions. With suitable inputs, this code should work for any mission, subject to
assumptions described in createInfoFiles.md.
------------------------------------------------------------------------------------------------------------------------------------
