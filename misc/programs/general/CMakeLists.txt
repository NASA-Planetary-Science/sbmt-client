cmake_minimum_required(VERSION 2.6)

PROJECT(lidar)

set(VTK_DIR /project/nearsdc/software/vtk/build)
#set(VTK_DIR /Users/kahneg1/software/vtk/vtk-5.8.0/build)
find_package(VTK REQUIRED)
include(${VTK_USE_FILE})

set(SPICE_DIR /project/nearsdc/software/spice/alpha_dsk_c CACHE PATH "Path to Spice")
set(LBFGS_DIR /project/nearsdc/software/liblbfgs/install CACHE PATH "Path to Lbfgs")
set(ADOLC_DIR /project/nearsdc/software/adolc/install CACHE PATH "Path to ADOLC")
set(GSL_DIR /project/nearsdc/software/gsl/install CACHE PATH "Path to GSL")

add_definitions(-ansi -Wall)

include_directories(${SPICE_DIR}/include ${SPICE_DIR}/src/dsklib_c ${LBFGS_DIR}/include ${GSL_DIR}/include ${ADOLC_DIR}/include)
link_directories(${SPICE_DIR}/lib ${LBFGS_DIR}/lib ${GSL_DIR}/lib ${ADOLC_DIR}/lib64)

add_library(lidar
optimize.cpp
closest-point-dsk.cpp
icp.cpp
icp-vtk.cpp
icp-intersection.cpp
closest-point-vtk.cpp
optimize-gsl.cpp
gravity-werner.cpp
gravity-cheng.cpp
lidardata.cpp
propagator.cpp
propagatorfit.cpp
platemodel.cpp

constants.h
gravity-point.h
util.h
mathutil.h
)
target_link_libraries(lidar vtkHybrid dsklib_c cspice csupport lbfgs gsl gslcblas adolc)

add_executable(lidar-min-icp lidar-min-icp.cpp)
target_link_libraries(lidar-min-icp lidar)

add_executable(lidar-min-fit lidar-min-fit.cpp)
target_link_libraries(lidar-min-fit lidar)

add_executable(lidar-compute-rms lidar-compute-rms.cpp)
target_link_libraries(lidar-compute-rms lidar)

add_executable(lidar-save-min-icp lidar-save-min-icp.cpp)
target_link_libraries(lidar-save-min-icp lidar)

add_executable(lidar-remove-duplicates lidar-remove-duplicates.cpp)

add_executable(lidar-convert-edr lidar-convert-edr.cpp)
target_link_libraries(lidar-convert-edr cspice csupport m)

add_executable(lidar-compute-gaskell-error lidar-compute-gaskell-error.cpp)
target_link_libraries(lidar-compute-gaskell-error cspice csupport)

add_executable(propagator prop-fit.cpp)
target_link_libraries(propagator lidar)

add_executable(gravity gravity.cpp)
target_link_libraries(gravity lidar)
