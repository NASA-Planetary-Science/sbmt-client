# SPICE
SPICE_DIR			= /project/sbmtpipeline/software/spice/cspice
SPICE_LIB			= $(SPICE_DIR)/lib/cspice.a
#
# Compiler
CXX				= g++
# Change -O2 to -g for debugging
COPT				= -O2
CFLAGS				= -Wall --pedantic
CINCLUDES			= -I$(SPICE_DIR)/include
CLIBS				= $(SPICE_LIB)
#
# Tool
TOOL				= createInfoFiles
SRC				= createInfoFiles.cpp getSpacecraftState.cpp getTargetState.cpp getFov.cpp

$(TOOL): $(SRC)
	$(CXX) -o $@ $(COPT) $(CFLAGS) $(CINCLUDES) $(SRC) $(CLIBS)
	
clean:
	rm $(TOOL)

.SUFFIXES: