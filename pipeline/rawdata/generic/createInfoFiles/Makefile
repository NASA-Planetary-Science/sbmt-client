# SPICE
SPICE_DIR			= /project/sbmtpipeline/software/spice/cspice-20200519/cspice
SPICE_LIB			= $(SPICE_DIR)/lib/cspice.a
#
# Compiler
CXX				= g++
# Change -O2 to -O0 -g for debugging
COPT				= -O2
CFLAGS				= -Wall --pedantic -Wno-long-long
CINCLUDES			= -I$(SPICE_DIR)/include -I.
CLIBS				= $(SPICE_LIB)
CFITSIO_LIBS			= -L./cfitsio -lcfitsio
#
# Main tool (createInfoFiles).
TOOL				= createInfoFiles
SRC				= createInfoFiles.cpp getSpacecraftState.cpp getTargetState.cpp getFov.cpp

# SPICE pointing tool (sbmtSpicePointing).
SPICE_TOOL		= sbmtSpicePointing
SPICE_SRC		= sbmtSpicePointing.cpp getSpacecraftState.cpp getTargetState.cpp getFov.cpp

$(TOOL): $(SRC)
	$(CXX) -o $@ $(COPT) $(CFLAGS) $(CINCLUDES) $(SRC) $(CLIBS)

$(SPICE_TOOL): cfitsio $(SPICE_SRC)
	$(CXX) -o $@ $(COPT) $(CFLAGS) $(CINCLUDES) $(SPICE_SRC) $(CLIBS) $(CFITSIO_LIBS)

all: cfitsio $(TOOL) $(SPICE_TOOL)

cfitsio: cfitsio/libcfitsio.a

cfitsio/libcfitsio.a:
	@echo "Configuring cfitsio prior to building... \c"
	@(cd cfitsio; ./configure > configure.txt 2>&1; \
	  if test $$? -ne 0; then \
	    echo "Errors occurred; see file cfitsio/configure.txt for more info." >&2; \
	    exit 1; \
	  else \
	    echo "done"; \
	  fi)
	@echo "Building cfitsio... \c"
	@(cd cfitsio; make libcfitsio.a >& make.txt 2>&1 ; \
	  if test $$? -ne 0; then \
	    echo "Errors occurred; see file cfitsio/make.txt for more info." >&2; \
	    exit 1; \
	  else \
	    echo "done"; \
	  fi)

clean:
	rm -f $(TOOL) $(SPICE_TOOL)
	(cd cfitsio; make clean > /dev/null 2>&1)

distclean:
	rm -f $(TOOL) $(SPICE_TOOL)
	rm -rf *.dSYM/
	(cd cfitsio; make distclean > /dev/null 2>&1)

.SUFFIXES:
