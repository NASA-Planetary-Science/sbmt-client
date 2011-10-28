cmake_minimum_required(VERSION 2.6)
 
PROJECT(lidar)

set(VTK_DIR /project/nearsdc/software/vtk/build) 
find_package(VTK REQUIRED)
include(${VTK_USE_FILE})

set(SPICE_DIR /project/nearsdc/software/spice/alpha_dsk_c CACHE PATH "Path to Spice")
set(LBFGS_DIR /project/nearsdc/software/liblbfgs/install CACHE PATH "Path to Lbfgs")

add_definitions(-ansi -Wall)

include_directories(${SPICE_DIR}/include ${SPICE_DIR}/src/dsklib_c ${LBFGS_DIR}/include)
link_directories(${SPICE_DIR}/lib ${LBFGS_DIR}/lib)

add_library(lidar optimize.c closest_point_dsk.c icp.c closest_point_vtk.cpp)
if(VTK_LIBRARIES)
  target_link_libraries(lidar ${VTK_LIBRARIES} dsklib_c cspice csupport lbfgs)
else()
  target_link_libraries(lidar vtkGraphics vtkIO dsklib_c cspice csupport lbfgs)
endif()

add_executable(lidar_min lidar-min-icp.c)
target_link_libraries(lidar_min lidar)

add_executable(lidar_compute_rms lidar-compute-rms.c)
target_link_libraries(lidar_compute_rms lidar)

add_executable(lidar_save_min lidar-save-min-icp.c)
target_link_libraries(lidar_save_min lidar)

add_executable(lidar_remove_duplicates lidar-remove-duplicates.cpp)
