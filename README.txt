Small Body Mapping Tool

The purpose of this file is to document all the steps needed to build, and deploy this tool.
This includes the C++ vtk libraries, the SQL database, php scripts, 
the data files needed on the server, and of course
the java code run by the client. There's a lot to do to get the tool
up and running so let's go through each step one-by-one.

1. Compile VTK C++ libraries

2. Compile our own C++ extensions
   Certain 

3. Setup the RPATH on the binary files (Mac and Linux only)

4. Setup directory structure on server
	It is necessary to use a php-enabled web server such as Apache. The following
	is the directory layout on the server. We will assume the top level folder is
	located in /www
	
	Then the following folders are needed
	
	/www/sbmt/
	/www/query/
	/www/data/
	/www/data/EROS/
	/www/data/MSI/
	/www/data/NIS/
	/www/data/NLR/
	
	The contents of each of these folders will be discussed in the next sections.
	
5. Php scripts setup

6. Java web start setup

7. /www/data/EROS

8. /www/data/MSI

9. /www/data/NIS

10. /www/data/NLR

11. SQL database generation