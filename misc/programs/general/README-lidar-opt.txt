This program determines the optimal translation that minimizes the
error of a lidar track to the asteroid. Currently lidar data from the
NEAR or Hayabusa missions only are supported. To use this program,
first save out to disk the tracks you are interested in optimizing in
the SBMT (by right-clicking on the track(s) and selecting the "Save
Track" or "Save All Visible Tracks" options). In this folder, you'll
see a python program called lidar-opt.py. This python program sets
various things up and then calls a C++ program (called lidar-min-icp
in the same directory) which does the actual optimization. The
lidar-opt.py program takes 2 or more arguments. The first is either
-eros or -itokawa depending on the source of the lidar data. The
remaining arguments are the track files. Examples:

./lidar-opt.py -itokawa /path/to/track0.tab
./lidar-opt.py -eros /path/to/track0.tab /path/to/track1.tab /path/to/track2.tab

This program uses the highest resolution shape model file of Eros or
Itokawa available in the SBMT cache. You need to run the SBMT at least
once and display the highest resolution shape model of Eros or Itokawa
(so that the files get downloaded to the cache) before using this
program.

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
