# SPICE
SPICE_DIR			= /project/sbmtpipeline/software/spice/cspice
SPICE_LIB			= $(SPICE_DIR)/cspice.a
#
# Compiler
CXX				= g++
COPT				= -O2 # change to -g for debugging
CFLAGS				= -Wall --pedantic
CINCLUDES			= -I$(SPICE_DIR)
CLIBS				= $(SPICE_LIB)
#
# Tool
TOOL				= createInfoFiles
SRC				= createInfoFiles.cpp getSpacecraftState.cpp getTargetState.cpp getFov.cpp

$(TOOL): $(SRC)
	$(CXX) -o $@ $(COPT) $(CFLAGS) $(CINCLUDES) $(SRC) $(CLIBS)

.SUFFIXES: