---
title: Small Body Mapping Tool
---

## Overview

It is currently difficult for small body scientists and researchers to
quickly analyze and interpret data returned from small body missions
due to the large amounts of data returned and the difficult to
understand formats in which the data is often stored in. In addition,
the highly irregular shapes of small bodies such as 433 Eros or 25143
Itokawa add further difficulties and 2D-based tools are not well
suited. The Small Body Mapping Tool (SBMT) was designed to address
these issues by providing a 3D tool that makes it easy to quickly
search and visualize small body data. Please continue reading below
for more information on how to download and run the SBMT.

## How to run it

### Prerequisites

The Small Body Mapping Tool is supported on the following platforms:

-   Windows 7 or higher (64-bit)
-   Linux, recent distributions (64-bit)
-   Mac OS X version 10.5 or higher (64-bit Intel-based Macs only)

In addition, the 64-bit version of Java 6 or 7 must be installed on
your system for Windows or Linux platforms (unless running the Windows
version of the tool below which includes Java). For Mac platforms,
Java 6 must be installed--Java 7 is not supported at this time.

For Windows and Linux platforms, Java can be downloaded from
[http://www.java.com](http://www.java.com), if not already installed
on your system. Make sure to download the 64-bit version of Java, not
the 32-bit version.

On Macs, Java 6 comes either preinstalled (Leopard or Snow Leopard),
or is available as an add-on directly from Apple (Lion or later). The
version of Java available at
[http://www.java.com](http://www.java.com) for Macs, which is Java 7,
will not work with the SBMT. However, even if you've already installed
Java 7, it is not necessary to uninstall it, and the SBMT should be
able to find Java 6 if it present on your system.

It is also recommended that your machine be equipped with a good
graphics card as the tool makes heavy use of 3D graphics.

### Download the tool

The Small Body Mapping Tool is distributed as a self contained zip
file. Download the version appropriate for your platform and follow
the steps below. For the benefit of Windows users, we provide 2
versions of the tool, one with Java and one without.

   -  Mac: [sbmt-2013.03.05-macosx-x64.zip](releases/sbmt-2013.03.05-macosx-x64.zip)
   -  Linux: [sbmt-2013.03.05-linux-x64.zip](releases/sbmt-2013.03.05-linux-x64.zip)
   -  Windows: [sbmt-2013.03.05-windows-x64.zip](releases/sbmt-2013.03.05-windows-x64.zip) (does not include Java)
   -  Windows: [sbmt-2013.03.05-windows-x64-with-java.zip](releases/sbmt-2013.03.05-windows-x64-with-java.zip) (includes Java)

For those who have previously used the tool, please note that we no
longer support launching it with Java Web Start. The only way to launch
the tool now is by downloading one of the above files.

### Steps to launch the tool:

1. Make sure Java is installed on your system and can be found on the system's path (unless using the Windows version above which includes Java).
2. Download the appropriate file above for your platform.
3. Unzip the file to any folder
4. Navigate to the 'sbmt' folder
5. Open the 'password.txt' file located in the 'sbmt' folder in a text
   editor and enter your username on the first line and password on the
   second line. (This step may be omitted if located on the APL
   campus.)
6. On Mac and Linux platforms, run the 'runsbmt' shell script. On Windows, run the 'runsbmt.exe' program.

## Previous Releases

Previous releases of the tool can be found [here](releases). Previous
releases are *not* supported. They are provided only as a fallback in
case of a problem with the most recent version.
