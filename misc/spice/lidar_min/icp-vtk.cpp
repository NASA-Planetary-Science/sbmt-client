#include <vtkSmartPointer.h>
#include <vtkVertexGlyphFilter.h>
#include <vtkPoints.h>
#include <vtkPolyData.h>
#include <vtkIterativeClosestPointTransform.h>
#include <vtkTransformPolyDataFilter.h>
#include <vtkLandmarkTransform.h>
#include <vtkMatrix4x4.h>

#include "icp.h"

// This file is adapted from the example at
// http://www.vtk.org/Wiki/VTK/Examples/Cxx/Filters/IterativeClosestPointsTransform

static void CreatePolyData(struct Point pts[], int n, vtkSmartPointer<vtkPolyData> polydata)
{
    vtkSmartPointer<vtkPoints> points =
        vtkSmartPointer<vtkPoints>::New();

    for (int i=0; i<n; ++i)
    {
        points->InsertNextPoint(pts[i].p);
    }
    
    vtkSmartPointer<vtkPolyData> temp =
        vtkSmartPointer<vtkPolyData>::New();
    temp->SetPoints(points);

    vtkSmartPointer<vtkVertexGlyphFilter> vertexFilter =
        vtkSmartPointer<vtkVertexGlyphFilter>::New();
    vertexFilter->SetInputConnection(temp->GetProducerPort());
    vertexFilter->Update();

    polydata->ShallowCopy(vertexFilter->GetOutput());
}


/**
   Perform ICP algorithm on source and target points, both of size
   n. The optimal transformation that maps the source points into target
   points is calculated and applied to the source points.

   * Arguments        I/O     Description
   * -----------      ---     ----------------------------
   * source           I/O     input source points and, on output, transformed source points
   * target           I       input target points (not modified on output)
   * n                I       number of source and target points (both must be same size)
   * additionalPoints I/O     if not null, additional points to transform using the computed
                              transformation. Assumed to be size n.
 */
extern "C" void icpVtk(struct Point source[], struct Point target[], int n, struct Point* additionalPoints)
{
    // Create source and target polydata

    vtkSmartPointer<vtkPolyData> sourcePolydata =
        vtkSmartPointer<vtkPolyData>::New();
    vtkSmartPointer<vtkPolyData> targetPolydata =
        vtkSmartPointer<vtkPolyData>::New();

    CreatePolyData(source, n, sourcePolydata);
    CreatePolyData(target, n, targetPolydata);
    

    // Setup ICP transform
    vtkSmartPointer<vtkIterativeClosestPointTransform> icp =
        vtkSmartPointer<vtkIterativeClosestPointTransform>::New();
    icp->SetSource(sourcePolydata);
    icp->SetTarget(targetPolydata);
    icp->GetLandmarkTransform()->SetModeToRigidBody();
    icp->SetMaximumNumberOfIterations(20);
    //icp->StartByMatchingCentroidsOn();
    icp->Modified();
    icp->Update();

    // Get the resulting transformation matrix (this matrix takes the source points to the target points)
    vtkSmartPointer<vtkMatrix4x4> m = icp->GetMatrix();
    std::cout << "The resulting matrix is: " << *m << std::endl;

    // Transform the source points by the ICP solution
    vtkSmartPointer<vtkTransformPolyDataFilter> icpTransformFilter =
        vtkSmartPointer<vtkTransformPolyDataFilter>::New();
    icpTransformFilter->SetInput(sourcePolydata);
    icpTransformFilter->SetTransform(icp);
    icpTransformFilter->Update();

    vtkSmartPointer<vtkPolyData> outPolydata = icpTransformFilter->GetOutput();
    for (int i=0; i<n; ++i)
    {
        double pt[3];
        outPolydata->GetPoint(i, pt);
        source[i].p[0] = pt[0];
        source[i].p[1] = pt[1];
        source[i].p[2] = pt[2];
    }
    
    if (additionalPoints != 0)
    {
        vtkSmartPointer<vtkPolyData> additionalPolydata =
            vtkSmartPointer<vtkPolyData>::New();
        CreatePolyData(additionalPoints, n, additionalPolydata);

        // Transform the additional points by the ICP solution
        icpTransformFilter = vtkSmartPointer<vtkTransformPolyDataFilter>::New();
        icpTransformFilter->SetInput(additionalPolydata);
        icpTransformFilter->SetTransform(icp);
        icpTransformFilter->Update();

        outPolydata = icpTransformFilter->GetOutput();
        for (int i=0; i<n; ++i)
        {
            double pt[3];
            outPolydata->GetPoint(i, pt);
            additionalPoints[i].p[0] = pt[0];
            additionalPoints[i].p[1] = pt[1];
            additionalPoints[i].p[2] = pt[2];
        }
    }
}
