#include <vtkPolyDataReader.h>
#include <vtkPolyData.h>
#include <vtkCellData.h>
#include <vtkMath.h>
#include <vtkIdList.h>
#include <vtkPolyDataNormals.h>
#include <vtkTriangle.h>
#include <vector>

using namespace std;

/*
  These function compute potential in an essentially equivalent way to that
  of A. Cheng (in file gravity2.cpp) but with slightly different formulas.
  */
struct FaceCenters
{
    double center[3];
    double normal[3];
    double area;
};

static vector<FaceCenters> faceCenters;
static vtkPolyData* polyData = 0;
static vtkIdList* idList = 0;

vtkPolyData* initializeGravity3(const char* vtkfile)
{
    vtkPolyDataReader* smallBodyReader = vtkPolyDataReader::New();
    smallBodyReader->SetFileName(vtkfile);
    smallBodyReader->Update();

    if (polyData != 0)
        polyData->Delete();

    vtkPolyDataNormals* normalsFilter = vtkPolyDataNormals::New();
    normalsFilter->SetInput(smallBodyReader->GetOutput());
    normalsFilter->SetComputeCellNormals(1);
    normalsFilter->SetComputePointNormals(0);
    normalsFilter->SplittingOff();
    normalsFilter->ConsistencyOn();
    normalsFilter->AutoOrientNormalsOn();
    normalsFilter->Update();

    polyData = vtkPolyData::New();
    polyData->ShallowCopy(normalsFilter->GetOutput());

    polyData->BuildCells();
    polyData->BuildLinks();


    vtkPoints* points = polyData->GetPoints();
    vtkDataArray* normals = polyData->GetCellData()->GetNormals();

    idList = vtkIdList::New();


    // Compute the face data
    int numFaces = polyData->GetNumberOfCells();
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        FaceCenters fc;

        // Get center of cell
        polyData->GetCellPoints(i, idList);

        double pt1[3];
        double pt2[3];
        double pt3[3];
        points->GetPoint(idList->GetId(0), pt1);
        points->GetPoint(idList->GetId(1), pt2);
        points->GetPoint(idList->GetId(2), pt3);

        vtkTriangle::TriangleCenter(pt1, pt2, pt3, fc.center);

        fc.area = vtkTriangle::TriangleArea(pt1, pt2, pt3);

        normals->GetTuple(i, fc.normal);

        faceCenters.push_back(fc);
    }

    return polyData;
}

double getGravity3(const double fieldPoint[3], double acc[3])
{
    double potential = 0.0;
    acc[0] = 0.0;
    acc[1] = 0.0;
    acc[2] = 0.0;

    // Compute the edge data
    int numFaces = polyData->GetNumberOfCells();
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        const FaceCenters& fc = faceCenters[i];

        double rhat[3];
        vtkMath::Subtract(fc.center, fieldPoint, rhat);
        vtkMath::Normalize(rhat);

        double rhat_dot_N = vtkMath::Dot(rhat, fc.normal);

        potential += rhat_dot_N * fc.area;
    }

    potential *= 0.5;
    acc[0] *= 0.5;
    acc[1] *= 0.5;
    acc[2] *= 0.5;

    return potential;
}
