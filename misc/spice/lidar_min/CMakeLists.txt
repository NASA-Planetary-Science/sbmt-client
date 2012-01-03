cmake_minimum_required(VERSION 2.6)
 
PROJECT(lidar)

set(VTK_DIR /project/nearsdc/software/vtk/build) 
find_package(VTK REQUIRED)
include(${VTK_USE_FILE})

set(SPICE_DIR /project/nearsdc/software/spice/alpha_dsk_c CACHE PATH "Path to Spice")
set(LBFGS_DIR /project/nearsdc/software/liblbfgs/install CACHE PATH "Path to Lbfgs")
set(ADOLC_DIR /project/nearsdc/software/adolc/install CACHE PATH "Path to ADOLC")

add_definitions(-ansi -Wall)

include_directories(${SPICE_DIR}/include ${SPICE_DIR}/src/dsklib_c ${LBFGS_DIR}/include /opt/local/include ${ADOLC_DIR}/include)
link_directories(${SPICE_DIR}/lib ${LBFGS_DIR}/lib /opt/local/lib ${ADOLC_DIR}/lib64)

add_library(lidar
optimize.c
closest-point-dsk.c
icp.c
icp-vtk.cpp
closest-point-vtk.cpp
optimize-gsl.c
)
target_link_libraries(lidar vtkHybrid dsklib_c cspice csupport lbfgs gsl gslcblas adolc)

add_executable(lidar-min-icp lidar-min-icp.cpp)
target_link_libraries(lidar-min-icp lidar)

add_executable(lidar-min-fit lidar-min-fit.cpp)
target_link_libraries(lidar-min-fit lidar)

add_executable(lidar-compute-rms lidar-compute-rms.cpp)
target_link_libraries(lidar-compute-rms lidar)

add_executable(lidar-save-min-icp lidar-save-min-icp.c)
target_link_libraries(lidar-save-min-icp lidar)

add_executable(lidar-remove-duplicates lidar-remove-duplicates.cpp)

add_executable(lidar-convert-edr lidar-convert-edr.c)
target_link_libraries(lidar-convert-edr cspice csupport m)

add_executable(lidar-compute-gaskell-error lidar-compute-gaskell-error.cpp)
target_link_libraries(lidar-compute-gaskell-error cspice csupport)
