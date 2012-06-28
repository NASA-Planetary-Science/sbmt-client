Usage: ./cam-est -w <width> -h <height> -f <focal-length> <inputfile> <output-sumfile>

  -w <width>          the pixel width of the image
  -h <height>         the pixel height of the image
  -f <focal-length>   the focal length of the camera in pixels. For
                      example, if the focal length is 1 millimeter
                      and the pixel size is 1 micron, then the
                      focal length is 1000.
  <inputfile>         path to file containing the corresponding
                      image/model points as well as a starting
                      guess. The format of this file is explained
                      below.
  <output-sumfile>    generated sumfile containing estimated position
                      and orientation of camera. The sumfile is in the
                      same format used by Bob Gaskell.                      

This program estimates the position and orientation of a camera when
an image was acquired using a set of corresponding 2D image points and
3D model points, as well as an initial guess. In addition, the user
must supply the pixel width and height of the image and the focal
length of the camera in pixels.

The initial guess as well as the corresponding image and pixel points
must be placed in an input file with the following format: The first
line must contain the initial guess of the spacecraft position
expressed in Cartesian coordinates, where each of the 3 coordinates
are separated by spaces. The second line must contain the initial
guess of the spacecraft orientation expressed as a quaternion, where
the 4 values defining the quaternion are separated by spaces. The
third line and on-wards should contain the corresponding 2D image and
3D model points. The model points must be expressed in Cartesian
coordinates (separated by spaces), and the image points should be
expressed in pixel space (separated by spaces). Fractional pixel
coordinates are allowed. There is no limit to the number of
corresponding points, but at least 3 point pairs should be provided
for best results.

For example, here is a valid input file:

-0.147 0.717 -0.03815
0.450 0.5695 -0.418 0.5456
0 1023
-0.0221 0.0786 -0.06867
1023 1023
-0.02798 0.127 -0.0051943
1023 0
0.044641 0.09883 -0.00663
0 0
0.06889 0.006342 -0.077
78.17 845.1
-0.00946 0.07667 -0.0642

In this example, the initial guess for the camera position is -0.147
0.717 -0.03815, the initial guess for the camera orientation is 0.450
0.5695 -0.418 0.5456, image pixel 0 1023 is assumed to correspond with
model point -0.0221 0.0786 -0.06867, image pixel 1023 1023 corresponds
with model point -0.02798 0.127 -0.0051943, pixel 1023 0 corresponds
with model point 0.044641 0.09883 -0.00663, pixel 0 0 corresponds with
model point 0.06889 0.006342 -0.077, and pixel 78.17 845.1 corresponds
to model point -0.00946 0.07667 -0.0642.

Note that in this example the model points could have preceded the
image points instead.

If the above example input is saved to a file named input.txt, then to
the run the program, you might type:

./cam-est -w 1024 -h 1024 -f 10029.45948178711 input.txt sumfile.txt

When the program finishes, the file sumfile.txt will contain the
position and orientation of the camera in Bob Gaskell's sumfile
format.

Getting input data from SBMT: The Small Body Mapping Tool could be
used to obtain the input data needed by this program. Here's how to
get each of the 3 types of input:
1. Shape model points: To get the position of a shape model point,
simply click on the point in the asteroid and the point will be
printed in the Java Console (If the Java Console is not visible, you
will need to show it using the Java preferences). 
2. Pixel coordinates: To get the pixel coordinates of an image point,
you will need to load the image into the tool (using a dummy
sumfile--since obviously we don't have one yet!). Then open up a 2D
view of the image by right-clicking on the image in the list and
clicking Properties. Then, you can click anywhere in the image and the
pixel coordinates will be printed to the Java Console.
3. Initial Guess: To get an initial guess, navigate around the tool
until the view in the renderer approximates the view in the
image. Then from the File menu, click Camera, and the position and
orientation of the camera will be printed to the console. It is not
necessary to be very precise here. Any decent initial guess should
provide good convergence. In addition, make sure the vertical field as
shown in the Camera dialog matches the field of view of the actual
camera, since otherwise, the position printed out will be
incorrect. If the field of view is incorrect, enter the correct field
of view, and then reopen the Camera dialog and new position and
orientation information will be printed to the Java Console.

Focal Lengths for various cameras (in pixels):
Hayabusa's AMICA: 10029.45948178711
NEAR's MSI:       10407.030418092796
Galileo's SSI:    98493.37270341207

Vertical field of view for various cameras (in degrees):
Hayabusa's AMICA: 5.839083
NEAR's MSI:       2.9505
Galileo's SSI:    0.465815


Contact: Please contact Eli Kahn at eliezer.kahn@jhuapl.edu if
you have any questions about this program.
