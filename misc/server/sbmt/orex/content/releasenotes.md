% Release Notes
%
%

# Release Notes

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

### October 18, 2018 (SBMT-0.6.5)

- Made bug fixes, feature enhancements, and UI improvements to the structures panel
- Made bug fixes, feature enhancements, and UI improvements to the custom images panel
- Made bug fixes and feature enhancements to custom plate coloring import and display
- Made bug fixes and feature enhancements to DTM tab
- Made performance enhancements when changing the resolution of shape models while also displaying plate colorings
 
OREx:
- Added search capabilities for OTES and OVIRS

 

### September 7, 2018 (SBMT-0.6.0)

- Added ability for users to use a selected regional DTM as the main shape on which to map data
- Made minor enhancements to SBMT GUI
- Resolved bug that interfered with saving plate data inside polygon structures
- Resolved bug with backplane generation
- Resolved bug with Viking images of Phobos and Deimos

### August 16, 2018 (SBMT-0.5.5.1)

- Fixed a startup problem crash when the preferences file didn't exist

### August 15, 2018 (SBMT-0.5.5)

- Updated the way the Tool behaves if user is not connected to the internet when the SBMT starts.
- Fixed bug with structures not displaying and repositioning correctly on the body.
- Fixed bug that prevented users on Windows machine from passing shape models to the SBMT using the command line
- Fixed bugs with plate coloring visualization
- Fixed bugs with custom images functionality 

### July 13, 2018 (SBMT-0.5.1)

- Fixed a problem with EROS NIS Database searches

### July 10, 2018 (SBMT-0.5.0)

#### General
- Added ability for the user to return the SBMT to the same state it was in previously (i.e., to open the SBMT with same shape model and view as the user previously was using)
- Added ability to read in a csv file that contains ancillary plate coloring data
- Fixed bugs with plate coloring saving
- Fixed bugs with custom images functionality
- Fixed bugs in the camera menu.
 
#### OREX:
- Fixed bugs with displaying and saving OTES and OVIRS spectra
- Fixed bugs with loading LIDAR tracks with similar times
- Fixed bug that led to SBMT returning search results on the wrong part of the body for OCAMS data

### June 15, 2018
-   Deployment to SPOC


