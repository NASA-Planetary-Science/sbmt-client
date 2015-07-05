This program takes a shape model, an initial position and velocity at
a specified time and computes the trajectory of an object propagated
forward in time with these initial conditions by integrating the
equations of motion.

The state is propagated forward in time based on the gravitational
force of the shape model using the supplied density, where the
gravitational force is computed using the Werner method.

The propagated trajectory is saved out to a file, with the following
columns:

1. time (UTC)
2. x position in body fixed coordinates
3. y position in body fixed coordinates
4. z position in body fixed coordinates
5. x velocity in body fixed coordinates
6. y velocity in body fixed coordinates
7. z velocity in body fixed coordinates
8. x acceleration in body fixed coordinates
9. y acceleration in body fixed coordinates
10. z acceleration in body fixed coordinates
11. potential in body fixed coordinates



Usage:

  prop -b <body> -s <pltfile> -k <kernelfiles> -d <density> -ip <px>,<py>,<pz> -iv <vx>,<vy>,<vz> -o <outputfile>

where:

  -d <density> is the density of the body (in g/cm^3), Default: 1.0
  -ip <px>,<py>,<pz> is the initial position in body fixed coordinates. Default: 0.0,0.0,0.0
  -iv <vx>,<vy>,<vz> is the initial velocity in body fixed coordinates. Default: 0.0,0.0,0.0
  -b <body> is a supported body such as EROS, ITOKAWA, PHOBOS, or DEIMOS. Default: none
  -t <max-time> max time in seconds to propagate forward to. Must be positive. Default: 100.0
  -dt <interval> time step in seconds. Default: 1.0
  -s <pltfile> path to shape model in PLT format: Default: none
  -k <kernelfile> path to SPICE metakernel file: Default: none
  -o <outputfile> output trajectory file in body fixed coordinates. Default: output.txt



Example:

./prop -b EROS -s ver64q.tab -k kernels.txt -ip 3.4E-4,-0.00168,5.312 -iv .003,.003,.003 -d 2.67 -t 1000. -dt 20. -e output.txt

This will compute a trajectory with initial position
3.4E-4, -0.00168, 5.312, initial velocity .003,.003,.003 moving in the
vicinity of the Eros asteroid whose shape model is stored in the file
ver64q.tab and has a density of 2.67 g/cm^3. The SPICE metakernel file
is kernels.txt and propagation is computed for 1000.0 seconds with a
time step of 20 seconds. The output trajectory is saved to output.txt.



Additional notes:

- The initial time of the simulation is always assumed to be at
  ephemeris time of zero.
- The propagation automatically stops when the algorithm detects that
  the trajectory has gone inside the shape model. When this happens
  the algorithm backtracks and iteratively computes the exact time the
  trajectory hits the asteroid. Therefore the final time printed in
  the output file is the time the trajectory hits the asteroid and is
  NOT a multiple of the time step (which would usually be slightly
  AFTER the trajectory hits the asteroid).
- The acceleration and potential columns in the output file do not
  include effects due to rotation of the asteroid.
- Required SPICE kernels: Internally the equations of motion are
  solved in inertial coordinates and SPICE is used to convert between
  body-fixed and inertial coordinates. Therefore, the metakernel
  specified with the -k option must include at a minimum these SPICE
  kernels:
        1. a leap second kernel (e.g. naif0010.tls)
        2. PCK kernel of the asteroid (e.g. pck00010.tpc)
        3. SPK kernel of the asteroid (e.g. EROS80.BSP)
        4. planetary SPK (e.g. de421.bsp)
