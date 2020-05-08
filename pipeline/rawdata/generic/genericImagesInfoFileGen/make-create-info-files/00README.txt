The combined directory was created by Alex Welsh in order to combine the 2 different ways the create_info_files.cpp generated info files.

The issue is that ONC fits files from HAYABUSA2 had time data in the fits header, whereas TIR data had the date in the file name.
The compile.sh script will compile the contents of this directory and give a create_info_files program that handles both issues.
If the scripts need to be edited in the future to handle different dates, etc. please edit the create_info_files.cpp files, 
recompile, and place the new create_info_files into the scripts directory above it.

Also, the get*.c files are RYUGU specific so other missions will need to use modified body prefixes.
broken.cpp is my first attempt and is not working

the _old files were the previous versions that didn't handle the date being in the file name and not the header.

The current code will compile and create a file that handles the ONC/TIR problem stated above.
