cmake_minimum_required(VERSION 2.6)

PROJECT(lidar)

set(VTK_DIR /project/nearsdc/software/vtk/vtk-5.6.0/build-static)
find_package(VTK REQUIRED)
include(${VTK_USE_FILE})

set(SPICE_DIR /project/nearsdc/software/spice/alpha_dsk_c CACHE PATH "Path to Spice")
set(LBFGS_DIR /project/nearsdc/software/liblbfgs/install CACHE PATH "Path to Lbfgs")
set(ADOLC_DIR /project/nearsdc/software/adolc/install CACHE PATH "Path to ADOLC")
set(GSL_DIR /project/nearsdc/software/gsl/install CACHE PATH "Path to GSL")
set(GOOGLE_TEST_DIR /project/nearsdc/software/googletest/gtest-1.7.0 CACHE PATH "Path to googletest")

add_definitions(-ansi -Wall)

include_directories(
${SPICE_DIR}/include
${SPICE_DIR}/src/dsklib_c
${LBFGS_DIR}/include
${GSL_DIR}/include
${ADOLC_DIR}/include
${GOOGLE_TEST_DIR}/include
${CMAKE_SOURCE_DIR}
${CMAKE_SOURCE_DIR}/gravity
${CMAKE_SOURCE_DIR}/lidar
${CMAKE_SOURCE_DIR}/icp
${CMAKE_SOURCE_DIR}/common
${CMAKE_SOURCE_DIR}/propagation
${CMAKE_SOURCE_DIR}/optimization)

link_directories(${SPICE_DIR}/lib ${LBFGS_DIR}/lib ${GSL_DIR}/lib ${ADOLC_DIR}/lib64 ${GOOGLE_TEST_DIR})

add_library(lidar
icp/icp.cpp
icp/icp-vtk.cpp
icp/icp-intersection.cpp

optimization/optimize.cpp
optimization/optimize-gsl.cpp

gravity/gravity-werner.cpp
gravity/gravity-cheng.cpp
gravity/gravity-point.h
gravity/platemodel.cpp

lidar/lidardata.cpp
lidar/track-optimizer.cpp

propagation/propagator.cpp
propagation/propagatorfit.cpp

common/closest-point-dsk.cpp
common/closest-point-vtk.cpp
common/track.cpp
common/util.h
common/mathutil.h
common/point.h
)
target_link_libraries(lidar vtkHybrid dsklib_c cspice csupport lbfgs gsl gslcblas adolc)

add_executable(lidar-min-icp lidar/lidar-min-icp.cpp)
target_link_libraries(lidar-min-icp lidar)

add_executable(lidar-optimize-track lidar/lidar-optimize-track.cpp)
target_link_libraries(lidar-optimize-track lidar)

add_executable(lidar-min-fit lidar/lidar-min-fit.cpp)
target_link_libraries(lidar-min-fit lidar)

add_executable(lidar-compute-rms lidar/lidar-compute-rms.cpp)
target_link_libraries(lidar-compute-rms lidar)

add_executable(lidar-compute-track-stats lidar/lidar-compute-track-stats.cpp)
target_link_libraries(lidar-compute-track-stats lidar)

add_executable(lidar-save-min-icp lidar/lidar-save-min-icp.cpp)
target_link_libraries(lidar-save-min-icp lidar)

add_executable(lidar-remove-duplicates lidar/lidar-remove-duplicates.cpp)

add_executable(lidar-convert-edr lidar/lidar-convert-edr.cpp)
target_link_libraries(lidar-convert-edr cspice csupport m)

add_executable(lidar-compute-gaskell-error lidar/lidar-compute-gaskell-error.cpp)
target_link_libraries(lidar-compute-gaskell-error cspice csupport)

add_executable(prop-fit propagation/prop-fit.cpp)
target_link_libraries(prop-fit lidar)

add_executable(prop propagation/prop.cpp)
target_link_libraries(prop lidar)

add_executable(gravity gravity/gravity.cpp)
target_link_libraries(gravity lidar)

add_executable(elevation-slope gravity/elevation-slope.cpp)
target_link_libraries(elevation-slope lidar)

add_executable(normals-centers gravity/test/normals-centers.cpp)
target_link_libraries(normals-centers lidar)

add_executable(cam-est camera_est/cam-est.cpp)
target_link_libraries(cam-est lidar)

add_executable(runtests test/util-test.cpp)
target_link_libraries(runtests lidar gtest_main gtest )
