cmake_minimum_required(VERSION 2.6)

PROJECT(ola)

set(SPICE_DIR /project/nearsdc/software/spice/alpha_dsk_c CACHE PATH "Path to Spice")

add_definitions(-std=c99 -pedantic -Wall -Wextra)

include_directories(
${SPICE_DIR}/include
${CMAKE_SOURCE_DIR}
${CMAKE_SOURCE_DIR}/src)
link_directories(${SPICE_DIR}/lib)

#add_library(lidar
#)
#target_link_libraries(lidar cspice)

add_executable( ola-level0-to-level1 src/ola-level0-to-level1.c src/ola-common.h )
target_link_libraries( ola-level0-to-level1 cspice )

add_executable( ola-level1-to-ck src/ola-level1-to-ck.c src/ola-common.h )
target_link_libraries( ola-level1-to-ck cspice )

add_executable( ola-level1-to-level2 src/ola-level1-to-level2.c src/ola-common.h )
target_link_libraries( ola-level1-to-level2 cspice )

#add_executable( ola-test-level2 src/ola-test-level2.c src/ola-common.h )
#target_link_libraries( ola-test-level2 cspice )

#add_executable( ola-generate-nadir-ck src/ola-generate-nadir-ck.c src/ola-common.h )
#target_link_libraries( ola-generate-nadir-ck cspice )
