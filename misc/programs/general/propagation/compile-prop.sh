# To compile prop, you will need these 2 libraries: GSL available
# from http://www.gnu.org/software/gsl/ and SPICE available from
# http://naif.jpl.nasa.gov. Download, build, and install
# these 2 libraries. Then the following command can be used to compile
# prop. You will need to change the paths in this command to the
# locations where you installed GSL and SPICE.

g++ -o prop -O3 *.cpp \
	-I. \
	-I/project/nearsdc/software/gsl/install/include \
	-I/project/nearsdc/software/spice/cspice/include \
	/project/nearsdc/software/gsl/install/lib/libgsl.a \
	/project/nearsdc/software/gsl/install/lib/libgslcblas.a \
	/project/nearsdc/software/spice/cspice/lib/cspice.a
