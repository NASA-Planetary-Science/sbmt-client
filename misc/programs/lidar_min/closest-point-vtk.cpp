#include <stdio.h>
#include <vtkPolyDataReader.h>
#include <vtkPolyData.h>
#include <vtkCellLocator.h>
#include <vtkGenericCell.h>
#include <vtkMath.h>


static vtkCellLocator* cellLocator = 0;
static vtkGenericCell* genericCell = 0;


void initializeVtk(const char* dskfile)
{
    vtkPolyDataReader* smallBodyReader = vtkPolyDataReader::New();
    smallBodyReader->SetFileName(dskfile);
    smallBodyReader->Update();

    vtkPolyData* polyData = vtkPolyData::New();
    polyData->ShallowCopy(smallBodyReader->GetOutput());

    // Initialize the cell locator
    cellLocator = vtkCellLocator::New();
    cellLocator->FreeSearchStructure();
    cellLocator->SetDataSet(polyData);
    cellLocator->CacheCellBoundsOn();
    cellLocator->AutomaticOn();
    //cellLocator->SetMaxLevel(10);
    //cellLocator->SetNumberOfCellsPerNode(5);
    cellLocator->BuildLocator();

    genericCell = vtkGenericCell::New();
}


void findClosestPointVtk(const double* origin, double* closestPoint, int* found)
{
    double point[3] = {origin[0], origin[1], origin[2]};
    vtkIdType cellId;
    int subId;
    double dist2;
    cellLocator->FindClosestPoint(point, closestPoint, genericCell, cellId, subId, dist2);

    if (cellId >= 0)
        *found = 1;
    else
        *found = 0;
}

void intersectWithLineVtk(const double* origin, const double* direction, double* closestPoint, int* found)
{
    double start[3] = {origin[0], origin[1], origin[2]};
    double distance = vtkMath::Norm(start);
    double lookPt[3];
    lookPt[0] = start[0] + 2.0*distance*direction[0];
    lookPt[1] = start[1] + 2.0*distance*direction[1];
    lookPt[2] = start[2] + 2.0*distance*direction[2];

    double tol = 1e-6;
    double t;
    double x[3];
    double pcoords[3];
    int subId;

    int result = cellLocator->IntersectWithLine(start, lookPt, tol, t, x, pcoords, subId);

    closestPoint[0] = x[0];
    closestPoint[1] = x[1];
    closestPoint[2] = x[2];

    if (result > 0)
        *found = 1;
    else
        *found = 0;
}
