/*=========================================================================

  Program:   Visualization Toolkit
  Module:    vtksbmtCellLocator.h

  Copyright (c) 2010 The Johns Hopkins University Applied Physics Laboratory
  All rights reserved.

=========================================================================*/
// .NAME vtksbmtCellLocator - CellLocator class for vtk
// .SECTION Description
// The purpose of this class is to allow the IntersectWithLine
// function to be called from Java. The problem is that the default
// IntersectWithLine function takes references which the Java Wrapper
// can't handle. All references have therefore been replaced with 1 element
// arrays in the method signature.

#ifndef __vtksbmtCellLocator_h
#define __vtksbmtCellLocator_h

#include "vtkCellLocator.h"
#include "vtksbmtUnsortedWin32Header.h"

class VTK_SBMT_UNSORTED_EXPORT vtksbmtCellLocator : public vtkCellLocator
{
public:
  static vtksbmtCellLocator *New();
  vtkTypeMacro(vtksbmtCellLocator,vtkCellLocator);

  // Description:
  // This simply calls the corresponding method in the base class but
  // allows this function to be called from Java since all references
  // have been replaced with 1 element arrays.
  int IntersectWithLine(double a0[3], double a1[3], double tol,
                        double t[1], double x[3], double pcoords[3],
                        int subId[1], vtkIdType cellId[1],
                        vtkGenericCell *cell)
  {
      return Superclass::IntersectWithLine(a0, a1, tol, t[0], x, pcoords, subId[0], cellId[0], cell);
  }

protected:
  vtksbmtCellLocator() {};
  ~vtksbmtCellLocator() {};
private:
  vtksbmtCellLocator(const vtksbmtCellLocator&);  // Not implemented.
  void operator=(const vtksbmtCellLocator&);  // Not implemented.
};

#endif 
