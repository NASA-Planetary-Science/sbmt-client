CMAKE_MINIMUM_REQUIRED(VERSION 2.4)
IF(COMMAND CMAKE_POLICY)
  CMAKE_POLICY(SET CMP0003 NEW)
ENDIF(COMMAND CMAKE_POLICY)

#
# Set the project name.
#

PROJECT (VTKSBMT)

#
# Load CMake commands that you probably should not modify.
#

INCLUDE (${VTKSBMT_SOURCE_DIR}/CMakeOptions.cmake)

#
# Here is where you can list the sub-directories holding your local
# classes. Sorting classes by 'package' type like VTK does (Common,
# Rendering, Filtering, Imaging, IO, etc.) is a good thing and prevents
# numerous dependencies problems.
#

SUBDIRS (
  Unsorted
  Utilities
)

#
# You can put your include path(s) here
#

INCLUDE_DIRECTORIES(${VTKSBMT_SOURCE_DIR}/Common)
INCLUDE_DIRECTORIES (${VTKSBMT_SOURCE_DIR}/Imaging)
INCLUDE_DIRECTORIES (${VTKSBMT_SOURCE_DIR}/Unsorted)

#
# Build examples too ?
#

#OPTION(BUILD_EXAMPLES "Build examples." ON)
#IF (BUILD_EXAMPLES)
#  SUBDIRS(Examples)
#ENDIF (BUILD_EXAMPLES)

#
# Common configuration settings
#
# Do not worry about this one.
#

CONFIGURE_FILE(
  ${VTKSBMT_SOURCE_DIR}/vtksbmtConfigure.h.in
  ${VTKSBMT_BINARY_DIR}/vtksbmtConfigure.h
)

INCLUDE_DIRECTORIES(${VTKSBMT_BINARY_DIR})
