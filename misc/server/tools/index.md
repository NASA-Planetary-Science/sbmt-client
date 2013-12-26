% Small Body Mapping Tools
%
%

<!--- To convert this markdown to HTML use this command (pandoc required)
pandoc -t html -s index.md -o index.html
-->

This page contains a version of the SBMT with extra command line
tools. See the README files in the doc folder for explanations of
these tools.

Currently only Mac OS X 10.8 or higher is supported. If other
platforms are desired, let us know.

Download: [sbmt-extras-2013.12.11-macosx-x64.zip](releases/sbmt-extras-2013.12.11-macosx-x64.zip)

The following additional tools are included:

1. Camera Position and Orientation Estimation - This tool can be used
   to estimate the position and orientation of a camera at the time it
   acquired a specific image.
2. Gravity Estimation - This tool estimates the gravitational
   potential, gravitational acceleration, elevation, and slope of a
   general polyhedron. It implements the algorithms of Werner and Scheeres
   as well as that of Andy Cheng.
3. Lidar Track Optimization - This tool can be used to determine the
   optimal translation that minimizes the error of a lidar track to the
   asteroid.
4. Trajectory Propagation - This tool uses a shape model, an initial
   position and velocity at a specified time and computes the trajectory
   of an object propagated forward in time with these initial conditions
   by integrating the equations of motion.

<!---
## Previous Releases

Previous releases of these tools can be found [here](releases).
-->
