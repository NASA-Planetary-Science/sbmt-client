#
# Source files
#
# Here is where you can add the name of your local unsorted classes.
#

SET (Unsorted_SRCS
  vtksbmtCellLocator.cxx
)

# --------------------------------------------------------------------------
# You probably do not need to modify anything below this line

# Create the vtksbmtUnsorted C++ library.
ADD_LIBRARY (vtksbmtUnsorted ${Unsorted_SRCS})
TARGET_LINK_LIBRARIES(vtksbmtUnsorted vtkGraphics)

#IF (VTK_USE_PARALLEL)
#  TARGET_LINK_LIBRARIES (vtksbmtUnsorted vtkParallel)
#ENDIF (VTK_USE_PARALLEL)

#IF (VTK_USE_RENDERING)
#  TARGET_LINK_LIBRARIES (vtksbmtUnsorted vtkRendering)
#  TARGET_LINK_LIBRARIES (vtksbmtUnsorted vtkVolumeRendering)
#  TARGET_LINK_LIBRARIES (vtksbmtUnsorted vtkHybrid)
#  TARGET_LINK_LIBRARIES (vtksbmtUnsorted vtkWidgets)
#ENDIF (VTK_USE_RENDERING)

# Create the vtksbmtUnsorted Tcl library.
IF (VTK_WRAP_TCL AND VTKSBMT_WRAP_TCL)
  VTK_WRAP_TCL3 (vtksbmtUnsortedTCL UnsortedTCL_SRCS "${Unsorted_SRCS}" "")
  ADD_LIBRARY (vtksbmtUnsortedTCL ${UnsortedTCL_SRCS})
  TARGET_LINK_LIBRARIES (vtksbmtUnsortedTCL vtksbmtCommonTCL vtksbmtUnsorted)
ENDIF (VTK_WRAP_TCL AND VTKSBMT_WRAP_TCL)

# Create the vtksbmtUnsorted Python library.
IF (VTK_WRAP_PYTHON AND VTKSBMT_WRAP_PYTHON)
  VTK_WRAP_PYTHON3 (vtksbmtUnsortedPython UnsortedPython_SRCS "${Unsorted_SRCS}")
  ADD_LIBRARY (vtksbmtUnsortedPythonD ${UnsortedPython_SRCS})
  ADD_LIBRARY (vtksbmtUnsortedPython MODULE vtksbmtUnsortedPythonInit.cxx)
  TARGET_LINK_LIBRARIES(vtksbmtUnsortedPythonD vtksbmtUnsorted vtksbmtCommonPythonD)
  TARGET_LINK_LIBRARIES(vtksbmtUnsortedPython vtksbmtUnsortedPythonD)
ENDIF (VTK_WRAP_PYTHON AND VTKSBMT_WRAP_PYTHON)

# Create the vtksbmtUnsorted Java library.
IF (VTK_WRAP_JAVA AND VTKSBMT_WRAP_JAVA)
  VTK_WRAP_JAVA3 (vtksbmtUnsortedJava UnsortedJava_SRCS "${Unsorted_SRCS}")
  ADD_LIBRARY (vtksbmtUnsortedJava SHARED ${UnsortedJava_SRCS})
  TARGET_LINK_LIBRARIES(vtksbmtUnsortedJava vtksbmtUnsorted)
ENDIF (VTK_WRAP_JAVA AND VTKSBMT_WRAP_JAVA)
