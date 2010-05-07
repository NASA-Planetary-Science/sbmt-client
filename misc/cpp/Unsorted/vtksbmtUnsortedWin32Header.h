/*=========================================================================

  Program:   Visualization Toolkit
  Module:    vtksbmtUnsortedWin32Header.h

  Copyright (c) Ken Martin, Will Schroeder, Bill Lorensen
  All rights reserved.
  See Copyright.txt or http://www.kitware.com/Copyright.htm for details.

     This software is distributed WITHOUT ANY WARRANTY; without even
     the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
     PURPOSE.  See the above copyright notice for more information.

=========================================================================*/
// .NAME vtksbmtUnsortedWin32Header - manage Windows system differences
// .SECTION Description
// The vtksbmtUnsortedWin32Header captures some system differences between Unix
// and Windows operating systems. 

#ifndef __vtksbmtUnsortedWin32Header_h
#define __vtksbmtUnsortedWin32Header_h

#include <vtksbmtConfigure.h>

#if defined(WIN32) && !defined(VTKSBMT_STATIC)
#if defined(vtksbmtUnsorted_EXPORTS)
#define VTK_SBMT_UNSORTED_EXPORT __declspec( dllexport ) 
#else
#define VTK_SBMT_UNSORTED_EXPORT __declspec( dllimport ) 
#endif
#else
#define VTK_SBMT_UNSORTED_EXPORT
#endif

#endif
