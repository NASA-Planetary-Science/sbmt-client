% Release Notes
%
%

# Release Notes

## May 9, 2019 (SBMT-0.7.5.1)
 
### Bug fixes
Custom Plate colorings and Custom images no longer write to the same configuration file (removes an exception seen by some users)
Save plate data/view statistics in polygons now works properly
Loading plate colorings then a structures file no longer crashes the tool
The scalebar on the 3D screen should now update properly as the user zooms in/out
Various updates to the behavior of the image table (NOTE: There is a known issue where sorting by Date requires 2 clicks of the column; we are investigating)
 
### OREx only
20190207b sumfile update
Delivery of 20190414 model

## April 25, 2019 (SBMT-0.7.5)

### New
Added ability to save structures when saving state
Added the ability to sort lists of images, data, and structures by column headers
Added a properties button to view FITS headers for plate colorings
Added the ability to sync contrast stretches between portions of images that are on the body and off the limb
Added ability to customize the color of offlimb image boundaries 
Improved lidar GUI and features
Enabled the ability to right click and view options for custom images
 
### Bug fixes
Fixed a bug that caused the tool to hang on startup when too many restricted models were encountered
Fixed bug that was not properly saving images in the save state
Reinstated ability to save plate data within multiple structures
Fixed a bug that would not allow flip edits to already loaded custom images
Fixed a bug that caused custom lighting to be removed when an image was unmapped
 
### OREx only
Added a NAVCAM tab
Fixed a bug that caused an error when performing an OCAMS search
Fixed a bug that prevented sunward arrow from appearing for OTES and OVIRS spectra
Added new shape models

## March 8, 2019 (SBMT-0.7.4.1)

### Bug Fixes
- Fixed problems with item selections in image table
- Fixed ENVI loading problem in custom image pane on windows

## March 1, 2019 (SBMT-0.7.4)

### New
- Added the ability to save the state of ellipse, circle, and point structures
- Added support to allow shape model manipulation while in structures edit mode
- Added ability to sort structures by attribute
- Added support to import/export ESRI-compatible structures files
- Added the ability to view off-limb portions of images
- Made UI improvements to lidar tabs
- Re-enabled the ability to drag Lidar tracks

### Bug Fixes
- Fixed bug that prevented custom images from being loaded
- Fixed bug that prevented users from saving structures files if offline and plate colorings were not cached
- Fixed a bug that prevented reset of image search parameters
- Fixed bug that prevented users with European machines from viewing some shape models
- Fixed bugs associated with loading and deleting custom DTMs
- Fixed bugs associated with image checkboxes
 
### OREx Only: 
- Added new shape models 
- Made enhancements to OLA search capability 
- Fixed bug that prevented image map from displaying on newest models

### January 17, 2019 (SBMT-0.7.3)
- Loading structures on higher resolution models now presents a progress dialog to show the user something is indeed happning
- Improvements to the custom images tab (NOTE: we are aware of continuing problems with the state of the checkboxes in this pane; this will be fixed in a future release)
- Improvements to LIDAR displays
- Improvements to the DTM tab
- Color images with perspective projection are now colored properly

OREX: 
- List of images now match the boundaries for MAPCAM and POLYCAM
- MAPCAM image gallery button on the last few models has been fixed
- Updates to the 1217 and 1227 models
- Bennu basemap can now be displayed from the main Bennu tab.    

### January 3, 2019 (SBMT-0.7.2)
- Model updates for OREX

### December 21, 2018 (SBMT-0.7.1)
- Improvements when loading saved image lists
- Improvements to the custom DTM pane
- Improvements to the custom images pane

OREX:
- OTES and OVIRS are now enabled
- Better UI behavior in the OTES and OVIRS panes

### November 28, 2018 (SBMT-0.7.0)
- Made UI improvements to the properties pane within the Regional DTMs tab 
- Made the saving of very high resolution plate colorings to a local machine more efficient
- Added version numbers to the downloaded application once unzipped
- Fixed bug that caused an error when loading custom plate colorings on Windows machines
- Fixed bug that caused an error when adding additional plate colorings to a custom fits shape model
- Fixed bug that prevented users from loading custom models via the command line

OREx:
- Fixed bug that caused an incorrect flip of MAPCAM and POLYCAM images
- Added ability to load in a global fits image cube
- Added new SPC shape models

### November 6, 2018 (SBMT-0.6.6)
- Added the ability to save the state of the camera view and image panel settings
- Added tooltips to and improved user-friendliness of toolbar icons
- Added ability to transfer custom DTM plate colorings when exporting to custom model
- Made bug fixes and UI improvements to the custom images panel
- Fixed bug that caused an error when loading a sumfile without deleting the default infofile name
- Fixed bug that with the "Show Spacecraft" option in the Observing Conditions tab

H2:
- Added new SPC shape models with the latest ONC, TIR, and LIDAR data
- Fixed bug that caused the LIDAR hide checkbox to become unsynced with the hide dropdown menu

### October 22, 2018 (SBMT-0.6.5.1)

- Configuration update for H2 lidar data

### October 18, 2018 (SBMT-0.6.5)

- Made bug fixes, feature enhancements, and UI improvements to the structures panel
- Made bug fixes, feature enhancements, and UI improvements to the custom images panel
- Made bug fixes and feature enhancements to custom plate coloring import and display
- Made bug fixes and feature enhancements to DTM tab
- Made performance enhancements when changing the resolution of shape models while also displaying plate colorings
 
OREx:
- Added search capabilities for OTES and OVIRS
 
H2:
- Added hypertree search for LIDAR data
- Added ability to import custom LIDAR data
 

### September 7, 2018 (SBMT-0.6.0)

- Added ability for users to use a selected regional DTM as the main shape on which to map data
- Made minor enhancements to SBMT GUI
- Resolved bug that interfered with saving plate data inside polygon structures
- Resolved bug with backplane generation
- Resolved bug with Viking images of Phobos and Deimos

H2: 
- Added LIDAR data; added new SPC and SFM shape models with the latest ONC and TIR data; added searchable database for TIR data

### August 16, 2018 (SBMT-0.5.5.1)

- Fixed a startup problem crash when the preferences file didn't exist

### August 15, 2018 (SBMT-0.5.5)


- Updated the way the Tool behaves if user is not connected to the internet when the SBMT starts.
- Fixed bug with structures not displaying and repositioning correctly on the body.
- Fixed bug that prevented users on Windows machine from passing shape models to the SBMT using the command line
- Fixed bugs with plate coloring visualization
- Fixed bugs with custom images functionality
 
 
H2:
- Several new SPC and SFM shape models with the latest ONC and TIR data
 

### July 13, 2018 (SBMT-0.5.1)

- Fixed a problem with EROS NIS Database searches

### July 10, 2018 (SBMT-0.5.0)

- Added ability for the user to return the SBMT to the same state it was in previously (i.e., to open the SBMT with same shape model and view as the user previously was using)
- Added ability to read in a csv file that contains ancillary plate coloring data
- Fixed bugs with plate coloring saving
- Fixed bugs with custom images functionality
- Fixed bugs in the camera menu.
 
 
 OREx:
- Fixed bugs with displaying and saving OTES and OVIRS spectra
- Fixed bugs with loading LIDAR tracks with similar times
- Fixed bug that led to SBMT returning search results on the wrong part of the body for OCAMS data

### March 20, 2018 (SBMT-2018.03.20)

- Release notes coming soon!

### March 19, 2018 (SBMT-2018.03.19)
- Release notes coming soon!



### July 31, 2017
-   Terms of use is now on the internal and external web page
-   Splashscreen now shows upon launch
-   Time history functionality fixes implemented

### March 13, 2017

-   Linux version working again
-   Hierarchical image search specification (Phobos Experimental only)
-   Fixed bugs with structure control point visibility, frustum visibility, image cube generation, and DEM properties window colorbar synchronization
-   Notifications appear whenever client fails attempt to access SQL or data server
-   Added "Gaskell Updated" pointing search option to Eros (Gaskell) MSI tab
-   User is given option to decide when colormaps update
-	Other minor GUI updates and bugfixes

### January 24, 2017 (Beta)

-   Fixed bugs in Bennu OLA search, browse, and track panels (leftover from the October 21, 2016 beta release)
-	Added colormap specification and control for scalar plate colorings, contours can be rendered as filled regions or lines
-	Fixed structure labels so they are hidden when their footpoint is occluded from view
-	Corrected band slider synchronization issues with multispectral images
-	Other minor bugfixes

### November 19, 2016 (Beta)

-   Improved accuracy of lidar point selection using mouse pointer
-   Profile plots show radius if no plate coloring is selected
-   All structures now support level of detail (LOD) switching
-   Multiple DEMs can be loaded from file simultaneously in DEMs tab
-   Plate color scaling can now be synchronized between DEM Properties View and the main window
-   Fixed synchronization bugs between DEM Properties View and the main window
-   Descriptive error message now shown if unable to draw structure due to an erroneously formed shape model
-   Added button to custom-generate image gallery HTML page based on image search results

### October 21, 2016 (Beta)

-   Moved to VTK (Visualization Toolkit) version 6.3 libraries (Linux version not working yet)
-   Display of spacecraft trajectories, animation of spacecraft, sub-solar and sub-earth surface points (Beta version)
-   Loading of ALTWG DEM FIT files as custom shape models
-   Off-limb image rendering (Beta)
-   Added Phobos MOLA tab 
-   Fixed problem of gallery image preview going off screen
-   OLA tracks under search tab now show the time range
-   Automatically set footprint stacking order of NIS spectral footprints using phase angle statistics
-   Fixed bugs in Bennu OLA search, browse, and track panels, added several new generated datasets
-   Added pick tolerance slider to View menu.
-   Added button to custom-generate image gallery HTML page based on image search results (Beta)
-   Now showing image pixel value in status bar when clicked in image property view
-   Implemented shadowing calculations for NIS images
-   Revised Names of asteroids and comets

### September 14, 2016

-   Re-enabled output of pixel coordinates when clicking on image in properties window

### August 29, 2016

-   Image properties dialog now displaying for all images
-   Viking Phobos images now being returned from database searches
-   Console once again prints out coordinates when clicking on the surface
-   "Hide Labels" and "Show Labels" buttons added to Structures tabs

### August 17, 2016

-   Recents menu is now updating properly
-   Added text labels to all structures
-   OLA tracks are no longer divided into 10 second chunks	
-   Bigmaps now makes all DistributedGravity results available

### August 5, 2016

-   DEMs with .fits, .FITS, .fit, and .FIT file extensions can now be loaded
-   Name of DEM file now displayed in title of DEM view

### August 4, 2016

-   Added "Show spacecraft position" checkbox to the lidar search panel.
-   Selecting a spacecraft point or its corresponding surface point highlights both simultaneously.
-   Fixed bug in lidar point selection due to LODs.
-   DEM tab is now able to save original FITs files
-   Bigmaps now works on shape models with spaces in name
-   Track name now displayed in OLA track list
-   Right click on instrument tab now brings up "set default instrument"
-   OLA track labels now match the color of their respective tracks
-   OLA tracklist in Tracks tab does now populates when .L2 files are loaded
-   OLA point selection now works when LODs are enabled
-   Right click on OLA track list now produces popup menu
-   "Recently viewed models" pullright added to view menu
-   Flip and Rotate controls in Import New Image dialog are now enabled for GENERIC_IMAGE type
-   Import New Shape Model dialog Polar Radius Z axis is no longer corrupted when editing
-   Can now load generic FITS DTM files into DEM view
-   Paths show interpolated profiles if no plate coloring is selected
-   Boundary of deleted custom image or DEM no longer stays on shape model after deleted
-   If the normal offset of an image is changed, the normal offset of the boundary now changes
-   Added a "Clear Cache" menu item
-   Fixed bugs in "Import Shape Models" dialog
-   Improved look of LidarSearchPanel GUI

### July 15, 2016

-   DEM tab, added to all bodies allows DEM files to be loaded locally
-   DEMs can now be displayed directly on the body surface in the 3D View
-   MapMaker and BigMaps generated DEMs can be selected from DEM tab and viewed in DEM Properties View
-   DEM plate coloring in DEM Properties View can now be optionally synchronized with the 3D View
-   2D Profiles can now be viewed on tracks in the 3D View, as well as the DEM Properties View
-   OLA panel in the Bennu model is now optimized for time queries as well as spatial queries
-   OLA search tab now allows user to select from multiple locally generated OLA data sources
-   OLA search results now show name of OLA data file
-   OLA search results list now has checkboxes to control visibility
-   New "Favorites" menu item allows user to add frequently used small body models to the View menu
-   Favorites menu also allows specification of default model to load on startup.
-   Right click on current viewing tab allows user to set favorite instrument for default model.
-   Image pull-right "Simulate Lighting" menu item can now be toggled on and off
-   Colors of image boundaries no longer change to red when model resolution is changed


### June 14, 2016

-   Fixed bugs in ImageCube registration and display of boundaries
-   Tutorial can now be downloaded directly from client help menu
-   Daphne, Hermione and Pallas models now load properly
-   Reference potential values have now been added to the references web page

### May 26, 2016

-   Changed Image Cubes to output image pixel values in scientific units
-   Added stereo viewing capabilities
-   Added Bennu shape model version 4

### May 5, 2016

-   MapMaker functionality has been upgraded to use BigMaps
-   Multi-layer images (Image Cubes) can now be created from multiple overlapping images
-   OLA LIDAR intensity data can now be displayed 
-   Fixed bugs in importing of FITS images for custom models
-   FITS images of type double can now be imported
-   New version of tutorial now available
-   Fixed another bug in "File->Export Six Views" command

### April 6, 2016

-   Added custom image import to user manual.
-   Fixed bug introduced in March 9, 2016 public release that prevented any images from being mapped

### April 4, 2016

-   Added "Image Galleries" link to sidebar menu.
-   Version 3 of 67p model, increased resolution of image search.
-   Fixed MapMaker and LEISA image search bug.
-   Added "Save Selected Image List" option.

### March 11, 2016

-   Improved performance of OLA search for Bennu using a tree search algorithm

### March 9, 2016

-   3D view now switches to lower level of detail for shape and lidar data when dragging or zooming to improve interactive performance
-   Custom image panel can now import FITS image files, including image cubes
-   Imported custom images can now be arbitrarily rotated or flipped, including generic FITS images
-   Displayed band of imported custom image cubes can now be selected with a slider
-   Lidar point intensity now being displayed by modulating intensity of color
-   Ancillary FITS files can now be imported to specify plate coloring
-   Sensor to target range info now being displayed in status bar when selecting lidar point
-   Removed old version of Bennu model data

### January 29, 2016

-   Version 2 of 67p model with documentation
-   Mapmaker profile plot now showing correct data for values besides gravity-derived elevation
-   Updated content of references webpage

### December 31, 2015

-   Full fix of clipping bug in imported cylindrical images

### December 7, 2015

-   Fixed bug with Ceres genericPhpQuery
-   Changed Ceres FS search default start/end dates to April 2015 - July 2015, 
    which covers the entire span of dates for available images
-   Users can now export a mapped image as an ENVI file

### November 24, 2015

-   Database queries now working for Bennu images
-   File->Camera... dialog box now has lat, lon and roll parameters
-   Fixed bug in "File->Export Six Views" command
-   Partial fix of bug in importing cylindrical images covering less than the full surface

### October 28, 2015

-   Added database query for new image files

### October 26, 2015

-   Added new model and image files

### October 20, 2015

-   Added test image data for Bennu POLYCAM imager

### September 28, 2015

-   Fixed Windows bug loading AMICA image files

### August 11, 2015

-   Fixed bug in "Export Six Views" command
-   Added "Corrected" and "Corrected Spice Derived" pointing sources for LORRI images.
    Currently, only Pluto contains any corrected pointing information, but this will be
    uploaded to the server when it becomes available.
-   "Reset Pointing" button in ImageInfo dialog now deletes any adjusted pointing file
-   Exported INFO files now contain the transformed pointing information without any
    adjusted keywords, so the adjustments have been folded in.
-   Fixed bug in zoom buttons of ImageInfo dialog that caused zooming to stop after a
    certain amount.
-   Fixed bug in ImageInfo dialog boxes where components were shrinking to minimum size.
-   Improved resizing behavior of components in ImageInfo dialog boxes.
-   Fixed bug in "Camera..." menu command causing camera position to freeze.
-   Added three-axis ellipsoid model generation to Import Shape Models dialog.
-   Updated radiuses of Charon, Hydra and Nix shape models. Currently, the Hydra and Nix
    models are spheres, but we will update them to three-axis ellipsoids when data
    becomes available.

### July 18, 2015

-   Pluto and its moons' models have all been modified to reflect lastest consensus radii
-   SUM pointing files can now be placed manually in the local .neartool directory, or
    submitted to an SBMT curator for upload to the server. These will override the INFO
    pointing files.
-   Bug in image pointing pan buttons, which made some closeup images impossible to 
    adjust, has been fixed.
-   Bug in x/X/y/Y/x/Z key commands, which caused the camera to zoom in close to the 
    body, has been fixed.
-   Bug in "Generate Backplanes..." image popup menu, which caused it to throw an
    exception, has been fixed.

### July 16, 2015

-   Upgraded to latest version of VTK
-   Fixed bug in loading of SUM pointing files in the custom "Image" tab
-   Fixed bug in loading of custom perspective images where images were flipped
-   Added LORRI, MVIC and LEISA tabs for HYDRA and NIX moons of PLUTO
-   Added HYDRA and NIX to the data pipeline

### July 13, 2015

-   Modified image pointing information is now being saved with an "INFO.adjusted" suffix
    in the local cache, so it is not overwritten when the backend pipeline is rerun.
    These files can be copied directly from the cache directory into other user's cache
    directories, and they can eventually be uploaded to the server.    
-   INFO file import and export file names in dialog boxes now default to the image file
    name with a ".INFO" suffix.    
-   Panning, zooming and rotation pointing direction buttons scalable by scale factor.

### July 10, 2015

-   Added interactive pointing adjust capability
-   [New Horizons team only] Added spectrum display for LEISA images
-   Added INFO file specification field for importing custom perspective images.
-   Pointing information can now by adjusted manually using the "Select Target" mode.
    or the pan, zoom and rotate buttons on the Image Info dialog.
-   LEISA and MVIC instrument tabs added to Pluto bodies.
-   Color image info dialog now has contrast and intensity sliders.

### February 26, 2015

-   When loading a shape model by specifying path to shape model on the
    command line, fixed issue in which full path of shape model was
    required rather than a relative path.
-   In Mapmaker tab, added ability to manually specify search region,
    rather than via region selection with mouse.
-   In image search tabs, added checkboxes to list of matching images
    for mapping and hiding images.
-   In image search tabs, added button (below search list) for saving
    out list of matched images to a text file.
-   In image search tabs, when searching for images, image boundaries
    are now shown in different colors which can be changed by the user
    by right-clicking.
-   When starting SBMT, if the password.txt file cannot be found, a
    warning is printed out to the console.
-   In Tracks tab added ability to load OLA Level 2 format.
-   [New Horizons team only] Added MVIC tab to Jupiter body and moons
    for viewing MVIC data.
-   [New Horizons team only] Added Imaging tab to Pluto and Charon
    models for viewing latest LORRI images.

### January 21, 2015

-   Added Stooke Eros image map (available at
    [http://sbn.psi.edu/pds/resource/stookemaps.html](http://sbn.psi.edu/pds/resource/stookemaps.html))
    to Eros shape models (Gaskell, Thomas, NLR, NAV). There is now a
    "Show Image Map" checkbox in the leftmost tab for each of these
    shape models.
-   Now applying flat field correction to Itokawa AMICA images.

### December 30, 2014

-   Fixed issue performing search queries.
-   At bottom is Statistics information on leftmost tab, added new
    button for showing additional statistics. These statistics take
    longer to compute so are not shown by default.
-   Added control in Eros Lineament tab for changing line width of
    lineaments.

### December 4, 2014

-   In NLR, LIDAR, and Tracks tabs, now displaying both UTC and ET of
    lidar point in status bar when user clicks on lidar point.
-   In Tracks tab, added support for loading track in binary
    format. Previously only text format was supported. See the [user
    guide](helpcontents.html) for description of the allowed formats.

### October 31, 2014

-   Changed error shown for lidar tracks to be RMS (root mean square) in NLR, LIDAR, and Tracks tabs.
-   Added 3 new shape models (Pallas, Daphne, and Hermione) from
    [http://benoit.carry.free.fr/database/shapemodels.php](http://benoit.carry.free.fr/database/shapemodels.php).
-   Added Mapmaker tab to Vesta.

### August 27, 2014

-   Fixed problem which sometimes occured in previous release when
    loading the same shape model from the command line multiple times
    in which SBMT would display a blank window.

### August 13, 2014

-   Added units to labels in shape model importer dialog.
-   In the Mapmaker tab, changed max half size to 512 rather then 513
    since 513 causes array out of bounds error in mapmaker fortran code.
-   When launching the SBMT on the command line, the program now takes
    one optional argument, namely a path to a shape model. If such an
    argument is specified, the shape model is loaded and displayed
    rather than the default Eros shape model. This is similar to
    importing a custom shape model except that when loading the shape
    model via the command line, the shape model is only available
    temporarily for that run of the tool and is not saved to
    disk for future runs of the tool as is the case when importing the
    shape model. The format of the shape model is determined based on
    the extension of the file as follows:
    -   OBJ format valid extensions: .obj, .wf
    -   Gaskell PLT format valid extensions: .pds, .plt, .tab
    -   VTK format valid extensions: .vtk

    Note that case is ignored. This feature is only supported on Mac and
    Linux platforms.
-   Created new independent tab called "Tracks" for showing custom lidar
    tracks. Therefore, removed the "Load Track" button for loading
    custom tracks from existing lidar search panel such as for
    NLR. This new tab is available for all shape models.


### May 27, 2014

-   In image properties window, adding spacecraft position, orientation,
    camera FOV, and sun vector to property list.
-   In image search panel, changed PDS to read SPICE in pointing drop
    down menu (since that better describes it).
-   In File menu, added option for exporting shape model to STL format.
-   Improved centering of structures (when using options when
    right-clicking on a structure).  There are now 2 options: 1)
    center in window and move up close to structure and 2) center in
    window but preserve original distance.
-   Fixed problem in which some circles would not draw.
-   Added 15 more Hudson shape models (YORP, WT24, SK, Ra-Shalom, Nereus,
    Mithra, KW4 Alpha, KW4 Beta, HW1, EV5, CE26 Alpha, CC Alpha,
    Betulia, DA Prograde, DA Retrograde). Available from
    [http://echo.jpl.nasa.gov/asteroids/shapes/shapes.html](http://echo.jpl.nasa.gov/asteroids/shapes/shapes.html).
-   Added N key press shortcut for spinning view along boresight so that Z axis is up.
-   Added link to data folder on SBMT home page.
-   Fixed wrong orientation of triangles in Mapmaker renderer view.
-   In Imaging search panels, changed label to read Search by Filename
    (instead of Search by ID) since we now do text search in
    filename. This means that when you enter a search term, it does
    not need to be an integer (as was the case previously). It can be
    any part of the filename. For example, if you are searching for
    MSI image M0131076086F4_2P_IOF_DBL.FIT, you can now enter the full
    name "M0131076086F4_2P_IOF_DBL.FIT" (without quotes) rather than
    "131076086". In addition you can enter any part of the name, and all
    images that contain the text entered in the filename, will be
    returned. For example, if you enter "M013", then all images
    beginning with M013 will be returned. If you leave the field
    empty, all images in the database with the specified pointing will
    be returned.
-   In Mapmaker rendering view, adding export to OBJ format in File menu
    (was missing previously).

### April 9, 2014

-   Added shape models of Jupiter, Callisto, Europa, Ganymede, and Io
    along with LORRI images from New Horizons flyby in 2007. These
    shape models are simple ellipsoid models and have been added for
    the benefit of the New Horizons team in preparation of the Pluto
    flyby in 2015.
-   Also added preliminary shape models of Pluto and its 5 moons in
    preparation of the Pluto flyby in 2015.

### March 25, 2014

-   When saving lidar tracks to file (by right-clicking on a track and
    selecting one of the Save Track options), a new column is written
    out listing the range of each lidar point.
-   When running the Mapmaker tool, the output maplet file is now
    written out in FITS format. There is no label file saved out
    anymore but some information about the maplet is saved in the
    header portion of the FITS file. Also when loading in a maplet
    file (by clicking the Load button in the Mapmaker tab), the file
    must now be in FITS format.

### February 14, 2014

-   When saving out polygons to file, area is now written out as
    well. Note though that if the area was not already computed by
    right-clicking on a polygon and clicking "Display Interior" then
    zero is written out. Thus you must for click "Display Interior"
    for a polygon for the correct area to be saved to the file. Also,
    when loading polygons from a file, the area saved in the file is
    displayed in the table (even though "Display Interior" has not been
    clicked).
-   Deimos images are now indexed in the database so that search options
    work now.

### January 30, 2014

-   Fixed performance issue when drawing large polygons which still
    remained in previous release. Drawing polygons should now be as
    fast as drawing paths, even with polygons with hundreds of
    vertices. Displaying the interior of polygons with many vertices
    is still very slow though.
-   Fixed bug in which popup menu would not appear when right-clicking
    on paths.

### January 28, 2014

-   Improved performance somewhat when drawing large polygon
    structures. By default interior of polygon is now not
    displayed. To display the interior, right-click on a polygon and
    select "Display Interior". At this point, the area of the polygon
    will be calculated and shown in the table. As soon as you start
    editing again, the interior of the polygon will no longer be shown
    and will only reappear if you click "Display Interior"
    again. Performance still degrades with large polygons but is not
    as bad as previously. We hope to improve this further in the near
    future.


### December 26, 2013

-   Fixed problem which prevented importing custom plate data for custom
    shape models.
-   Changed how plate data are saved out so that all plate data is
    saved out to a single file including information about plates
    such as as area and coordinates of center of plate.
-   Added plate data (slope, elevation, acceleration, potential) to most
    shape models.
-   Added new Steins, Hartley, Lutetia, Bennu, Dione, Tethys, Mimas and
    Phoebe shape models which were recently delivered to the
    PDS. Dione and Tethys are now no longer restricted to only certain
    accounts.
-   Added support for loading multiple custom lidar tracks in lidar
    search panel at once (when pressing "Load Tracks From Files...").
-   Changed "Source:" to "Pointing:" in image search tabs.
-   In lidar browse panels, show start and stop times of each file.
-   Changed title of main window to display name of shape model currently being viewed.
-   Added option in Help menu for opening web page listing sources of all data.
-   [OSIRIS-REx team only] Added OLA tab to Bennu shape model and
    added some simulated lidar data.


### September 23, 2013

-   Added options in the Preferences panel for changing the look of the
    orientation axes such as the color, size, font size and color,
    thickness, and cone (tip) size and radius.
-   Added option in Camera dialog (available in File menu) for using
    orthographic projection in renderer (previously only perspective
    projection was supported).


### August 30, 2013

-   In the mapmaker view window, you can now right click on a profile
    and an option called "Save Profile" will appear. (Previously there
    were several options but none of them worked). This will invoke the
    gravity program which will compute the acceleration, potential,
    elevation and slope at all points along the profile using the shape
    model at the resolution currently set for, say, the Eros shape
    model. The file will be in CSV format.
    Note that the save profile option is similar to the save profile
    option in the main renderer view (when you right click on a path with
    exactly 2 points) except that the latter does yet not currently use
    the gravity program but instead gets the gravity values from the plate
    data closest to the points.
-   Fixed bug when importing new shape models which required you to
    restart the tool to view the new shape model. This is no longer
    required.
-   Added mapmaker tab back to Bennu shape model (had accidentally
    removed it)
-   Added slope, elevation, acceleration, potential data for Mathilde
    and fixed a problem that occurred when generating backplanes for the
    MSI image currently available.
-   Fixed a bug which prevented importing custom plate data


### July 8, 2013

-   Added Imaging tab to Thomas Deimos shape model with SPICE derived
    Viking and HRSC (framing camera only) images. Unfortunately, the
    pointing is not very accurate so the images do not line up with
    the asteroid. We are working on improving this. Simply press the
    Search button to see a list of images as the search options do not
    work yet.
-   Added generation of backplanes label files for all
    cameras. Previously backplane label files were only being
    generated for MSI and AMICA images.
-   Added slope, elevation, gravitation acceleration and potential
    plate data for Ida and Gaspra.


### July 3, 2013

-   When plotting elevation, acceleration and potential of a lidar track
    (which you can do by right-clicking on a track and selecting "Plot
    Track..." in the menu), the shape model used when computing the
    plots values is at the same resolution as is currently viewed in
    the renderer. Previously, the lowest resolution shape model was
    always used regardless of the current resolution. Because of this
    change, plotting will take much longer when using high resolution
    shape models. Also changed algorithm to use Werner method rather
    than Cheng method since the former is more accurate, though it is
    slower.
-   Fixed problem in MSI backplanes label file where MSI backplanes
    could not be loaded into ISIS.

### June 19, 2013

-   Fixed problem on Mac and Linux platforms where tool would not start
    if placed in folder with a space in its path.
-   When saving out profile of a path (i.e. when right-clicking on a
    path with only 2 control points and clicking "Save Profile..." in
    the popup menu), now cartesian coordinates,
    latitude/longitude/radius, and all plate data (as listed in the
    leftmost tab in the Plate Coloring section) along the profile are
    saved out to separate columns in a CSV file.
-   Changed scale bar in renderer to have black text on white background
    (partially transparent) with text centered justified.
-   When right-clicking on an image and clicking Properties in the popup
    menu, the surface area of the image footprint is shown in the list of
    properties.

### June 6, 2013

-   Added MEX HRSC images (framing camera only) for Phobos using SPICE
    pointing data. In the Imaging tab for Phobos, in the source drop
    down menu, select "PDS derived" and just click search and you'll
    see a list of 1655 images (these images are not indexed in the
    database so the other search options do not work). Unfortunately,
    these images are not registered very well with the asteroid,
    probably due to imprecisions in the SPICE kernels, and do not line
    up well with the surface features. We are working on improving
    this.

### May 30, 2013

-   Added 3 new shape models of Eros, available at
    [http://sbn.psi.edu/pds/resource/nearbrowse.html](http://sbn.psi.edu/pds/resource/nearbrowse.html). They
    are: 1. Peter Thomas' model based on MSI images (6 resolution
    levels), 2. NAV Plate Model, and 3. NLR Shape Data.
-   Added option in preferences for changing background color of renderer view.

### May 8, 2013

-   Reorganized list of shape models in View menu.

### April 30, 2013

-   Added slope, elevation, gravitation acceleration and potential
    plate data for Phoebe and Mimas.
-   Added Robert Gaskell's shape models of Dione, Rhea, Tethys,
    Hyperion, and Tempel 1. These are experimental and only available
    to several accounts.
-   Added Imaging Data for Phoebe and Mimas. These images have not yet
    been indexed in the database, so simply press the search button and
    all the available images will be listed. The images listed are
    those Robert Gaskell used to create the shape model.

### April 17, 2013

-   Phobos images have been indexed in the database so
    search options are now functional.
-   Added slope, elevation, gravitation acceleration and potential
    plate data for Phobos.
-   Added option in File menu for exporting currently shown shape
    model to Wavefront OBJ format.

### March 5, 2013

-   Added new tab to Phobos shape model for mapping Viking and Phobos
    2 images. The various search options are not functional
    yet. Simply click the Search button to show a list of available
    images.
-   Fixed problem where checkbox to show image map would appear for some
    shape models for which there was none.

### Feb 15, 2013

-   Added Robert Gaskell's Lutetia shape model. This shape model,
    however, has not yet been released to the public so most users
    will not be able to access it yet (only a few accounts have access
    to it).

### January 24, 2013

-   There are now options in the main leftmost tab of each view for
    customization of the plate data by the user. To customize plate
    data, click the Customize button. This will allow users to load
    their own plate data for both built-in and imported shape models
    (previously it was only possible to customize plate data for
    imported models. When importing new shape models, you will no
    longer see the options for loading custom plate data since this
    functionality is now available in the main tab of each view.)
    Also made all views support RGB false coloring.
-   Added buttons in Images tab for moving items up and down. Also
    made the Delete button delete all selected items.
-   Added ability to change the name of an image in Images panel and show
    name in image list.
-   Implemented multi-selection for structures. You can now select
    multiple structures in the structures list and apply certain
    operations to all the selected structures (such as Delete or
    change color). To select multiple structures, you need to click on
    the items with the mouse while holding down the Shift or Control
    (Command on Mac) key (with the shift key, a single contiguous
    block of items will be selected and with the Control or Command
    key, multiple contiguous blocks can be selected). Note also that
    multiple structures can be selected in the renderer itself as
    well by holding down the shift or control (command on Mac) key.
-   Added option for hiding subset of structures by right-clicking on
    structures and clicking Hide. Hidden structures are now grayed out.
-   Added option for centering a structure in the window (similar to
    the way images can be centered) by right-clicking on a
    structure and clicking Center in Window. After centering a
    structure the shape model will be oriented such that the top of
    the window points in the direction of the north pole.
-   Added ability to change selection color in preferences.

### October 30, 2012

-   Added support for multi-selection in image lists popup menu. This
    works with the list of images that's returned when doing a search
    (MSI, AMICA, etc) as well as the list of images in the Images
    tab. To select multiple images, you need to click on the items
    with the mouse while holding down the Shift or Control (Command on
    Mac) key (with the shift key, a single contiguous block of items
    will be selected and with the Control or Command key, multiple
    contiguous blocks can be selected).
-   Changed how drawing of ellipses is done so that now it is similar
    to circles in that you click 3 point and an ellipse is drawn to
    fit them. Also fixed earlier problem where interactive changing of
    the flattening did not always follow the current mouse position
    exactly. Also added warning info about new way to create ellipses

### October 9, 2012

-   Fixed problem where NIS custom formulas did not allow dividing by a
    band (e.g. B01/B02 was flagged as invalid)

### September 2012

-   Added the ability to save out the plate data. For the built-in shape
    models, there is a button in the leftmost tab called Save Plate
    Data. For the mapmaker view, the option is in the File menu. This saves out
    the currently shown plate data set to a text file, where each line has the
    value for the corresponding plate.
-   Updated the tool so that now Windows users can run the
    mapmaker program (tested only on Windows 7--may not work on
    previous versions of Windows).
-   Added the ability to save out plate data for the interiors of
    polygons, circles, and ellipses.  If you right-click on one of these
    structures, there is now an option called "Save plate data inside..."
    If you click it, it will save out a text file with the following
    columns:
        1. plate id the integer id of the plate of the shape model.
        2. latitude at center of plate.
        3. longitude at center of plate.
        4. slope at center of plate.
        5. elevation at center of plate.
        6. gravitational acceleration at center of plate.
        7. gravitational potential slope at center of plate.
    Note that if a plate from the shape model is only partially inside the
    polygon, ellipse or circle, then it still gets saved out to the
    file. Thus there may be some plates saved out that are not exactly
    inside the structure.
-   added ability to delete individual structures by pressing delete or
    backspace key.
-   added Hide All and Show All buttons to structure mapping panels to
    temporarily show and hide all structures, without deleting them. Still
    working on supporting hiding/showing of individual structures.
-   when loading structures from file and there are already structures
    drawn, user is now prompted if they want to append to or overwrite
    existing structures. This allows loading more than 1 file at a time
    without needing to combine them into a single file.
-   added ability to change line thickness and color of maplet boundary.
-   In lidar panels (for Eros and Itokawa), added options for saving
    both modified and unmodified tracks.
-   In lidar panels (for Eros and Itokawa), added options for saving
    all visible tracks at once.
-   In the NIS panel for Eros, you can now use RGB color to color NIS footprints, unlike
    previously where only grayscale was supported.
-   In the NIS panel for Eros, you can now save out an individual NIS spectrum to a text file (by right
    clicking on a spectrum in the list or on the footprint in the
    renderer).
-   In the NIS panel for Eros, 3 requested formulas were added to the very end of the list of
    available bands in drop down menus. You should see "B36 - B05",
    "B01 - B05",  and "B52 - B36" at the end of the list. You can now
    select these when coloring NIS spectra.
-   In the NIS panel for Eros, you can now create your own custom formulas. There is a new
    button named "Custom Formulas..." in the NIS panel. Click on it
    and a dialog will open showing your list of formulas. Click 'Add'
    and another dialog will appear allowing you to input your
    formula. You can use the variables B01 through B64 to refer to
    individual bands. Standard math and trigonometric functions should
    work. E.g. (B21-B01)*sin(B33)/sqrt(2.5). When you close the first
    dialog, the custom formulas are added to the dropdown list from
    which to choose from. The list of formulas is saved from session
    to session so you won't lose your work when you close the tool.

### August 2012

-   A new polygons tab has been created for drawing polygon structures.
    This works similar to the Paths shape and works by placing and
    dragging control points. The surface area and perimeter length are
    shown in the table on the left for each polygon. Both convex and
    concave polygons are supported. However, polygons with crossovers
    are not drawn correctly and should be avoided (though the tool will
    not prevent it). Out of all the shapes, polygons are the most
    expensive to render. Also, as more control points are added, the
    longer it takes to render.
-   In the structures tab, a new button is provided on the bottom for
    changing line thickness of structures. This is enabled for all shape
    types with the exception of Points.
-   In the lidar search tab, there is now a button called "Translate
    Tracks...". When pressed, a dialog window will open allowing you to
    explicitly specify the translation (3 cartesian coordinates in
    kilometers). All tracks will then get shifted by this amount.
-   In the lidar search tab, there is now a "Drag Tracks" toggle button.
    When pressed, a new "drag mode" will be activated which disables
    standard navigation around the asteroid and allows you to drag the
    tracks around. As you drag around, the tracks get shifted such that
    the selected point is moved to the location on the asteroid the
    mouse is currently positioned over. If there is no selected point,
    nothing happens. (Note the "selected point" is the point shown in
    blue after clicking and for which some information about it is shown
    in the status bar)
-   In the lidar search tabs, there is a new checkbox called "Show Track
    Error". When pressed, the error of the displaced tracks is shown to
    the right of the checkbox. This error is computed as the mean
    distance between each lidar point and its closest point on the
    asteroid (expressed in km). This error is computed using only the
    visible tracks, not tracks that are currently hidden. This error is
    updated every time you make a change to the tracks, e.g. change
    translation or offset or hide/show a track. There may be a
    performance penalty for calculating the error which is why this
    option is provided to disable it. (Note that if you change the
    resolution level of the asteroid, the error does not get updated.
    You will then need to uncheck and recheck it to show the correct
    error).
-   In the lidar search tabs, there is now an option for loading a track
    you previously saved to disk (using the "Save Track..." option).
    Click this button, choose the track file and the track will load and
    replace any existing tracks in the list. Note that it is currently
    only possible to have a single track loaded from file at a time. You
    cannot load more than one track at a time from files.
-   Fixed a bug which prevented running the tool using Java version 7 on
    some Linux systems.
-   When saving lidar tracks (by right clicking on a track and clicking
    Save track), you can now choose between saving the original track or
    the track with the translation and radial offset applied.
-   When plotting a track (by right clicking on a track and clicking
    Save track), 3 sets of plots are now displayed. Previously only
    potential vs distance and time was shown, but now, in addition,
    elevation and gravitation acceleration vs distance and time are
    shown. Each is show in a separate window. In addition it is possible
    to save out the data of these plots to a text file using the "Export
    Data..." option in the File menu of each plot window.

### July 2012

-   For paths that consists of exactly 2 control points, you can
    right-click on it and select an option for saving a profile. A
    profile is a file which contains elevation data vs distance along
    the path. The file is formatted as a CSV file with the first column
    being distance and the second elevation. If the current shape model
    contains elevation data for each plate (which you would normally use
    to color the shape model in the leftmost tab), then that is used. If
    no elevation is available, then the user is prompted asking if the
    tool should evaluate elevation as the distance between a point on
    the path and the center of the asteroid. While not as accurate as
    true elevation data, this may be acceptable for bodies that have a
    circular shape.
-   Added options when right-clicking on a coordinate grid for changing
    its color and line thickness.
-   In the Camera dialog (available from the File menu) it is now
    possible to set the distance of the virtual camera to the center of
    the asteroid. The direction of the camera is not affected when
    changing this value.
-   In the Preferences dialog it is now possible to set the "Motion
    Factor" of the mouse wheel. This factor controls the speed of
    zooming in and out with the mouse wheel. Larger values result in
    faster speeds and smaller values result in slower speeds. The
    default value is 1. Negative values reverse the direction of the
    zoom (i.e. zoom out becomes zoom in and zoom in becomes zoom out).
-   Help contents now load in a web browser rather than a separate
    window.
-   Added "Recent Changes" option to Help menu which opens this page in
    a web browser.

### June 2012

-   When right-clicking on an image, there is now an option to change
    its opacity
-   It is now possible to delete an individual vertex from a Path
    (previously, it was necessary to delete the entire path and start
    again). To delete a vertex, press the Delete or Backspace key while
    in Edit mode and the vertex that is blue will be deleted. There's
    only one vertex at a time that's blue, all others are red. The blue
    vertex represents the "current" vertex. To change the current
    vertex, simply click on a new vertex while hovering over it (so that
    the cursor changes to a hand shape).
-   Added option to export mapmaker maplet to a plate file.
-   In the File menu, there is now an option to automatically save 6
    images of views along both directions of all 3 axes. This option
    will automatically reorient the view to successively point the
    virtual camera in the positive x, negative x, positive y, negative
    y, positive z, and negative z directions and save an image for each
    view. When prompted to choose a file name in the dialog, the name
    you choose will be modified to indicate each of the six views. For
    example, if you choose the name "image.png", then the following six
    files will get saved out: "image+x.png", "image-x.png",
    "image+y.png", "image-y.png", "image+z.png", and "image-z.png".
-   There is now a new tab called "Images" for each View to the right of
    the Structures tab, for both built-in and custom models, so instead
    of specifying images when you create a new custom shape model (which
    previously was the only to import custom images), you would now do
    it in the Images tab, which shows a list of the images you've added.
    To add a new image, press "New" and a dialog will appear in which
    you can specify the image and projection parameters. To edit an
    existing image, click the "Edit" button. And to remove an image from
    the list press "Delete from List". Adding an image doesn't
    immediately make it visible on the renderer. To view it, right-click
    on the image in the list, and press Map Image. To remove it,
    right-click and press Map Image again (clicking Hide also removes
    it--the difference is that Map does all the computation needed to
    map the image to view unlike toggling the Hide option, so using the
    Hide option is faster). To remove all images from the renderer (not
    from the list), press the "Remove All from View".

    The menu that appears when you right-click is the same as the one
    for the MSI and AMICA images, though some options are not enabled.
    The "Properties" option allows you to view a 2D view of the image as
    well as change the contrast (however, contrast stretching is
    currently only supported for grayscale images).

    In addition to the cylindrical projection, I've also added the
    ability to use a perspective projection, which is the same
    projection used to map the original spacecraft images (i.e. MSI or
    AMICA). To do this you need to provide a sumfile in the format that
    Bob Gaskell uses which contains the position and orientation of the
    spacecraft. You also need to provide the angular field of view of
    the camera in degrees for both dimensions (e.g. for AMICA images
    both field of views would be 5.839083 degrees)

    The "Show Image Map" checkbox which you previously would check to
    show the images is no longer present in the custom shape models. It
    is present, however, in several of the built-in models and is used
    to simply show a single image of the entire asteroid.

    Existing lists of images (from custom imported shape models) should
    show up in the list of the Images tab. If this is not the case,
    please let me know.

    Some current limitations to be aware of which I hope to address:

    -   When doing a perspective projection, the image must be FITS
        format, unlike a cylindrical projection where the image can be
        png, jpg, tiff, etc. (the typical formats), though not FITS.
    -   As mentioned already, the adjusting contrast stretching is only
        available for single channel images, not color images. If the
        image is really grayscale but stored as a color image (i.e. the
        3 channels are the same), you will need to use some image
        processing software to convert it to a true grayscale format. I
        hope to add stretching for color images as well.

-   When rescaling data range of plate data (e.g. Elevation or slope),
    it is now possible to set the min and max values to be less than min
    value of the data or greater than the max value of the data,
    respectively.
-   In the File menu, there is now a new Camera option which opens a
    dialog in which you can specify vertical field of view of the
    virtual camera. The default value is 30 degrees. Note that the field
    of view changes if you right click on an MSI or AMICA image and
    press "Center in Window". When you do that, the field of view
    changes to the field of view of the MSI or AMICA camera.
-   The Statistics label (at the bottom of the leftmost tab for each
    view) is now selectable, so it is now possible to copy and paste the
    values to another program (with Ctrl-C).
-   added options to preferences for setting the pick tolerance
-   There is now and option in the Preferences dialog to switch between
    a "Trackball Interactor Style" and a "Joystick Interactor Style".
    Until now, only the "Trackball" style has been used by the tool. The
    Joystick style provides a different way to navigate around the
    asteroid. One of things you can do with Joystick style is to
    continuously spin the asteroid around, while holding down the left
    mouse button.
-   Added SSI tab for Ida and Gaspra, and MSI tab for Mathilde. Only a
    few images are currently available for these asteroids (Simply click
    Search to show a list of images--all the search options are
    currently ignored). Work on adding the remaining images is currently
    in progress.
-   Some of the AMICA images of Itokawa are not registered well,
    especially the ones acquired on Nov 12 and Nov 19. We have begun
    work on correcting some of these misregistered images and you can
    view the currently available corrected images by selecting
    "Corrected" from the Source dropdown menu at the top of the AMICA
    tab and then pressing Search (all the search options are ignored in
    this case). Work on adding additional corrected images is currently
    in progress.

### April 2012

-   added requested feature to show a scale bar on the view to indicate
    size of asteroid at current zoom
-   added option for exporting shape model to plt format
-   Fixed crash which sometimes occurred when changing orientation axes
    properties in Preferences dialog.
-   Added new lighting options in a new Preferences dialog.

### March 2012

-   Added support for loading in custom plate data when importing a
    custom shape model.
-   Added support for arbitrary positioning of custom images on custom
    non-ellipsoidal shape models. Previously images on custom
    non-ellipsoidal shape models were required to cover the entire
    model. Also removed all restrictions on the values for longitudes.

### February 2012

-   Added many new shape models to tool such as models by Thomas and
    Stooke as well as radar based shape models. All of these were
    obtained from the PDS.

### January 2012

-   Added support for explicitly changing the normal offset (shift) of
    an image or structure away from the shape model with a new dialog.
-   Improved lidar search capability for both Eros and Itokawa. Has more
    features which should hopefully make it easier to find, view, and
    export lidar tracks.
-   Lidar browse functionality now includes options for showing
    spacecraft.
-   Added unfiltered and optimized versions of Itokawa Lidar data for
    both searching and browsing.
-   Added extent of small body in statistics label.
-   Added option when searching for "PDS derived" images to exclude
    Gaskell images.

### October 2011

-   Added Itokawa LIDAR data (new tab).

### September 2011

-   Fixed problem where wrong FOV size was being used for Itokawa images
    causing incorrect shifts in images.
-   When saving ellipse structures, added column for orientation angle
    of ellipse relative to gravity vector (for shape models for which
    such data is available).
-   Improved documentation of structure file formats.
-   Changed cursor to crosshairs when drawing structures.
-   When drawing structures, increased sensitivity when hovering over
    existing structures.
-   When drawing a circle (by clicking 3 points on perimeter),
    interacting with other circles is disabled after the first point has
    been clicked until the completion of the circle.
-   Fixed inability to save structures when no backplane data is
    available.
-   Use superscripts for exponents in Statistics label.
-   Fixed problem where values on color bar would not get updated when
    changing resolution level.
-   Added backplanes for very high resolution Itokawa shape model
    (previously, backplanes were a subsampled version of the high
    resolution model).
-   Added ability to map multiple images to a custom shape model.
-   Added ability to edit and duplicate a custom shape model.

### August 2011

-   Added more Itokawa images to AMICA search panels. Pointing info of
    these images are derived from SPICE kernel files.
-   Added new Hide Image option to image context menu for temporarily
    hiding an image
-   Added ability to create circle simply by clicking on any 3 points on
    its perimeter.
-   Fixed problem with distortions appearing around edges of images
    mapped to custom shape models.
-   Removed "seam" that appeared on images mapped to shape model along
    zero-longitude line.
-   Added new option to set focal point of camera by pressing the c key.
    Focal point is set to point in 3D view the cursor is currently over.

### July 2011

-   Added ability to import custom shape models into tool and map an
    image.
-   Fixed problem where 8-bit Itokawa AMICA images were not being
    displayed correctly.
-   Fixed problem where export to image was not working after switching
    to a different asteroid.
-   Added slope/elevation/gravity data for medium and high resolution
    shape models of Itokawa.
-   Speed improvement in loading circles/ellipses/points on Itokawa.
-   New Delete All button in structures tab for deleting all structures
    at once.
-   Fixed problem with computation of incidence and emission backplanes
    for MSI and AMICA images.
-   Fixed problem where old circle structure format could not be loaded
    into new ellipse tab.
