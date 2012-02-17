#include <stdio.h>
#include <vtkPolyDataReader.h>
#include <vtkPolyData.h>
#include <vtkCellLocator.h>
#include <vtkGenericCell.h>


static vtkCellLocator* cellLocator = 0;
static vtkGenericCell* genericCell = 0;


extern "C" void initializeVtk(const char* dskfile)
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


extern "C" void findClosestPointVtk(const double* origin, double* closestPoint, int* found)
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
