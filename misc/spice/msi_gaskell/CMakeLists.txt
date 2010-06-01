cmake_minimum_required (VERSION 2.6)
project (msi_gaskell)

set(SPICE_DIR /home/kahneg1/programs/spice/c/cspice CACHE PATH "Path to
    Spice")

include_directories(${SPICE_DIR}/include)
link_directories(${SPICE_DIR}/lib)

add_executable(process_sumfiles process_sumfiles.cpp)
target_link_libraries(process_sumfiles cspice)
