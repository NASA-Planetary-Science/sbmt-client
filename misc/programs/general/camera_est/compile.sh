# To compile cam-est, you will need these 2 libraries: GSL available
# from http://www.gnu.org/software/gsl/ and ADOLC available from
# http://projects.coin-or.org/ADOL-C. Download, build, and install
# these 2 libraries. Then the following command can be used to compile
# cam-est. You will need to change the paths in this command to the
# locations where you installed GSL and ADOLC.

g++ -o cam-est -O2 cam-est.cpp optimize-gsl.cpp \
	-I. \
	-I/project/nearsdc/software/gsl/install/include \
	-I/project/nearsdc/software/adolc/install/include \
	/project/nearsdc/software/gsl/install/lib/libgsl.a \
	/project/nearsdc/software/gsl/install/lib/libgslcblas.a \
	/project/nearsdc/software/adolc/install/lib64/libadolc.a
