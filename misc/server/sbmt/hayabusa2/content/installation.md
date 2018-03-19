---
title: Small Body Mapping Tool - Hayabusa2 Mission
---

## Installation Instructions

### Prerequisites

The Small Body Mapping Tool is supported on the following platforms:

-   Windows 7 or higher (64-bit)
-   Linux, recent distributions (64-bit)
-   Mac OS X version 10.7 or higher (64-bit Intel-based Macs only)

In previous versions of the SBMT, Java was required to be installed
before running the tool. The latest version now includes Java itself
within the release, so it is no longer required to install it.

### Download the tool

The Small Body Mapping Tool is distributed as a self-contained zip
file. Download the version appropriate for your platform and follow
the steps below.

   -  Mac: [sbmt-VERSIONXXXXXX-macosx-x64.zip](releases/sbmt-VERSIONXXXXXX-macosx-x64.zip)
   -  Linux: [sbmt-VERSIONXXXXXX-linux-x64.zip](releases/sbmt-VERSIONXXXXXX-linux-x64.zip)
   -  Windows: [sbmt-VERSIONXXXXXX-windows-x64.zip](releases/sbmt-VERSIONXXXXXX-windows-x64.zip)

For those who have previously used the tool, please note that we no
longer support launching it with Java Web Start. The only way to launch
the tool now is by downloading one of the above files.

### Steps to launch the tool:

1. Download the appropriate file above for your platform.
2. Unzip the file to any folder
3. Navigate to the 'sbmt' folder
4. (This step may be omitted if located on the APL campus.)
   You will need to create a password.txt file that contains your
   username and password so that the tool can connect to the APL
   server. There are 2 ways to do this:
       * Open the 'password.txt' file located in the 'sbmt' folder in a text
         editor and enter your username on the first line and password on the
         second line.
       * Alternatively, create a password.txt file that contains your
         username on the first line and password on the second line
         and place this file in the folder '\$HOME/.neartool'. '\$HOME' is
         the path to your home folder and '.neartool' is the folder the
         SBMT uses internally to store preferences and cached
         data. For example, if your home folder is located at
         '/Users/foo', then you would create the file at
         '/Users/foo/.neartool/password.txt'. Note that you would
         normally need to create the file using a terminal since
         hidden files are normally not visible with Finder. If the
         folder '.neartool' does not yet exist, create it. Placing the
         password.txt file in this location has the advantage of not
         requiring you to edit the password.txt file each time you
         download a new version of the tool.

    The tool first looks in the '.neartool' folder for the password.txt file
    and, if not present, then looks in the 'sbmt' folder.
5. On Mac and Linux platforms, run the 'runsbmt' shell script. On Windows, run the 'runsbmt.exe' program.


## Previous Releases

Previous releases of the tool can be found [here](releases).
