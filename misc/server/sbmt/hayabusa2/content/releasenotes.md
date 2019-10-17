% Release Notes
%
%

# Release Notes

## October 17, 2019 (SBMT-0.7.7.1)

### Bug Fixes:
- Fixed bug related to the View Search Results as Image Gallery not being enabled for certain bodies

## October 11, 2019 (SBMT-0.7.7)

### New:
- Added support for MacOS Catalina 
- The install process for the software for all Mac users has changed to accommodate this support: 1) Open the downloaded .pkg and follow instructions in the install wizard. 2) SBMT will now appear as an application in the /Applications/SBMT folder. The functionality of the tool should be unchanged. Please contact sbmt@jhuapl.edu if you encounter installation issues.  
- Added ability for client to add new models to SBMT without requiring a new release.
- Added offlimb image visualization capability for custom images.
- Added ability to define default color scheme for visualizing plate colorings.
- Added functionality for colorizing lidar tracks.
- Added visual indicators to show which column(s) is being used for sorting.
- Added scalebar value to DTM properties panel.
- Improved performance of lidar track processing.
- Improved indication on body of which lidar tracks are selected.
- Improved tables implemented for lidar tab.
- Improved logic for selecting multiple structures.
- Improved performance when hiding/showing many circles/ellipse/points structures.

### Bug fixes:
- Fixed bug that allowed duplicate custom image names to be created.
- Fixed a bug causing strange behavior when user went on and offline repeatedly.
- Fixed a bug that prevented structures from being deleted.
- Fixed a bug that caused a model error when a custom perspective image was imported after a custom cylindrical image (or vice versa).


## July 10, 2019 (SBMT-0.7.6)

### New 
- Major improvements to the layout of the LIDAR pane, for both browse and search
- Support for multiple basemaps on the main Body page, with server side support, has been added
- The startup process of the tool has been streamlined, and it should start much quicker, pushing some tasks to work in the background
- As part of the above streamlining, SBMT can now detect whether you go on or offline, and update menu items accordingly
- Some models now have image footprints and offlimb images pre-rendered on the server; we will be adding more of these over time
 
### Bug fixes
- A fix in 0.7.5.1 caused the CPU to run high - this has been resolved
- A difference in reported pixel location and value when certain pointing types were used has been resolved - they should now be consistent across pointing types (e.g. SPICE vs SPC)
- Several fixes related to reporting the proper pixel, value and lat/lon have been made
- Improvements to the structures rendering have been made, especially with large numbers of structures

## May 9, 2019 (SBMT-0.7.5.1)
 
### Bug fixes
- Custom Plate colorings and Custom images no longer write to the same configuration file (removes an exception seen by some users)
- Save plate data/view statistics in polygons now works properly
- Loading plate colorings then a structures file no longer crashes the tool
- The scalebar on the 3D screen should now update properly as the user zooms in/out
- Various updates to the behavior of the image table (NOTE: There is a known issue where sorting by Date requires 2 clicks of the column; we are investigating)

## April 25, 2019 (SBMT-0.7.5)

### New
- Added ability to save structures when saving state
- Added the ability to sort lists of images, data, and structures by column headers
- Added a properties button to view FITS headers for plate colorings
- Added the ability to sync contrast stretches between portions of images that are on the body and off the limb
- Added ability to customize the color of offlimb image boundaries 
- Improved lidar GUI and features
- Enabled the ability to right click and view options for custom images
 
### Bug fixes
- Fixed a bug that caused the tool to hang on startup when too many restricted models were encountered
- Fixed bug that was not properly saving images in the save state
- Reinstated ability to save plate data within multiple structures
- Fixed a bug that would not allow flip edits to already loaded custom images
- Fixed a bug that caused custom lighting to be removed when an image was unmapped
 
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
 

### January 17, 2019 (SBMT-0.7.3)
- Loading structures on higher resolution models now presents a progress dialog to show the user something is indeed happning
- Improvements to the custom images tab (NOTE: we are aware of continuing problems with the state of the checkboxes in this pane; this will be fixed in a future release)
- Improvements to LIDAR displays
- Improvements to the DTM tab
- Color images with perspective projection are now colored properly

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

#### General
- Updated the way the Tool behaves if user is not connected to the internet when the SBMT starts.
- Fixed bug with structures not displaying and repositioning correctly on the body.
- Fixed bug that prevented users on Windows machine from passing shape models to the SBMT using the command line
- Fixed bugs with plate coloring visualization
- Fixed bugs with custom images functionality
 
#### H2
- Several new SPC and SFM shape models with the latest ONC and TIR data
 

### July 13, 2018 (SBMT-0.5.1)

- Fixed a problem with EROS NIS Database searches

### July 10, 2018 (SBMT-0.5.0)

- Added ability for the user to return the SBMT to the same state it was in previously (i.e., to open the SBMT with same shape model and view as the user previously was using)
- Added ability to read in a csv file that contains ancillary plate coloring data
- Fixed bugs with plate coloring saving
- Fixed bugs with custom images functionality
- Fixed bugs in the camera menu.


### May 11, 2018
-   Added image database search functionality for Ryugu body
-   Moved the LIDAR data tab to its correct location on the Ryugu body


