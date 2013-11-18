This program determines the optimal translation that minimizes the
error of a lidar track to the asteroid. To use this program, first
save out to disk the tracks you are interested in optimizing in the
SBMT (by right-clicking on the track(s) and selecting the "Save Track"
or "Save All Visible Tracks" options). In this folder, you'll see a
python program called lidar-opt.py. This python program sets various
things up and then calls a C++ program (called lidar-min-icp in the
same directory) which does the actual optimization. The lidar-opt.py
program takes 2 or more arguments. The first is either -eros,
-itokawa, or the path to a shape model in VTK format. If -eros or
-itokawa are specified, then the program uses the highest resolution
shape model file of Eros or Itokawa available in the SBMT cache. You
need to run the SBMT at least once and display the highest resolution
shape model of Eros or Itokawa (so that the files get downloaded to
the cache) before using this program with the -eros or -itokawa
options. The remaining arguments are the track files. Examples:

./lidar-opt.py -itokawa /path/to/track0.tab
./lidar-opt.py -eros /path/to/track0.tab /path/to/track1.tab /path/to/track2.tab
./lidar-opt.py /path/to/shapemode.vtk /path/to/track0.tab /path/to/track1.tab /path/to/track2.tab

On output, for each of the inputs tracks, a new file of the same name
but with '-optimized.txt' appended to the name will be created in the
same folder as the track files. These files contain the original
tracks translated to their optimal positions. For instance, in the
first example above, a file called /path/to/track0.tab-optimized.txt
will be created. In the second example above, files
/path/to/track0.tab-optimized.txt /path/to/track1.tab-optimized.txt
/path/to/track2.tab-optimized.txt will be created.

In addition a file called 'all-tracks-optimized.txt' will be created
(in the same folder as the first track file listed when running the
program) which contains all the optimized tracks concatenated
together. For instance, in the second example above,
all-tracks-optimized.txt will contain
/path/to/track0.tab-optimized.txt, /path/to/track1.tab-optimized.txt,
and /path/to/track2.tab-optimized.txt concatenated together.

In addition a file called 'track-errors.csv' will be created
(in the same folder as the first track file listed when running the
program) which contains a table with the following columns:

Column 1 -> track : name of track file
Column 2 -> min distance before : the distance prior to
            optimization between of the lidar point closest to the shape model
Column 3 -> max distance before : the distance prior to
            optimization between of the lidar point farthest from the shape model
Column 4 -> RMS before : Root mean squared distance of lidar points to
                         shape model prior to optimization
Column 5 -> min distance before : the distance after
            optimization between of the lidar point closest to the shape model
Column 6 -> max distance before : the distance after
            optimization between of the lidar point farthest from the shape model
Column 7 -> RMS before : Root mean squared distance of lidar points to
                         shape model after optimization

Note the final row in the CSV file (with "all" in the first column)
contains these values for all tracks combined together, whereas the
previous row contains these values only for that specific track.
