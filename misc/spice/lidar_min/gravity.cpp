#include <vtkPolyDataReader.h>
#include <vtkPolyData.h>
#include <vtkCellData.h>
#include <vtkMath.h>
#include <vtkIdList.h>
#include <vtkPolyDataNormals.h>
#include <vtkTriangle.h>
#include <vector>

using namespace std;

struct EdgeData
{
    double r[3];
    double E[3][3];
    double Er[3];
    double rEr;
    int p1;
    int p2;
    double edgeLength;
};

struct FaceData
{
    double r[3];
    double F[3][3];
    double Fr[3];
    double rFr;
};

static vector<EdgeData> edgeData;
static vector<FaceData> faceData;
static vtkPolyData* polyData = 0;
static vtkIdList* idList = 0;

/*
  These functions compute gravitation potential and acceleration
  of a closed triangular plate model using the method of Werner as
  described in Werner R. A. and D. J. Scheeres (1997) CeMDA, 65, 313-344.
  */
vtkPolyData* initializeGravity(const char* vtkfile)
{
    vtkPolyDataReader* smallBodyReader = vtkPolyDataReader::New();
    smallBodyReader->SetFileName(vtkfile);
    smallBodyReader->Update();

    if (polyData != 0)
        polyData->Delete();

    edgeData.clear();
    faceData.clear();


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

    // Compute the edge data
    int numFaces = polyData->GetNumberOfCells();
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        polyData->GetCellPoints(i, idList);

        double cellNormal[3];
        normals->GetTuple(i, cellNormal);

        for (vtkIdType j=0; j<3; ++j)
        {
            vtkIdType p1;
            vtkIdType p2;
            if (j < 2)
            {
                p1 = idList->GetId(j);
                p2 = idList->GetId(j+1);
            }
            else
            {
                p1 = idList->GetId(2);
                p2 = idList->GetId(0);
            }

            EdgeData ed;

            // Compute unit vector from p1 to p2
            double edgeUnitVector[3];
            double pt1[3];
            double pt2[3];
            points->GetPoint(p1, pt1);
            points->GetPoint(p2, pt2);
            vtkMath::Subtract(pt1, pt2, edgeUnitVector);
            ed.edgeLength = vtkMath::Normalize(edgeUnitVector);
            // Compute half of the E dyad
            double edgeNormal[3];
            vtkMath::Cross(edgeUnitVector, cellNormal, edgeNormal);

            vtkMath::Outer(cellNormal, edgeNormal, ed.E);

            // Put the point with the lowest id into ed so that
            // the 2 identical edges always have the same point
            if (p1 < p2)
            {
                points->GetPoint(p1, ed.r);
                ed.p1 = p1;
                ed.p2 = p2;
            }
            else
            {
                points->GetPoint(p2, ed.r);
                ed.p1 = p2;
                ed.p2 = p1;
            }

            vtkMath::Multiply3x3(ed.E, ed.r, ed.Er);
            ed.rEr = vtkMath::Dot(ed.r, ed.Er);

            edgeData.push_back(ed);
        }
    }


    // Compute the face data
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        FaceData fd;
        polyData->GetCellPoints(i, idList);

        // Any vertex of the cell will do, so just choose the first one.
        points->GetPoint(idList->GetId(0), fd.r);

        // Compute the F dyad
        double normal[3];
        normals->GetTuple(i, normal);
        vtkMath::Outer(normal, normal, fd.F);

        vtkMath::Multiply3x3(fd.F, fd.r, fd.Fr);
        fd.rFr = vtkMath::Dot(fd.r, fd.Fr);

        faceData.push_back(fd);
    }

    return polyData;
}

static double compute_wf(const double fieldPoint[3], vtkIdType cellId)
{
    polyData->GetCellPoints(cellId, idList);
    vtkPoints* points = polyData->GetPoints();

    double pt1[3];
    double pt2[3];
    double pt3[3];
    points->GetPoint(idList->GetId(0), pt1);
    points->GetPoint(idList->GetId(1), pt2);
    points->GetPoint(idList->GetId(2), pt3);

    double r1v[3];
    vtkMath::Subtract(pt1, fieldPoint, r1v);
    double r1 = vtkMath::Norm(r1v);

    double r2v[3];
    vtkMath::Subtract(pt2, fieldPoint, r2v);
    double r2 = vtkMath::Norm(r2v);

    double r3v[3];
    vtkMath::Subtract(pt3, fieldPoint, r3v);
    double r3 = vtkMath::Norm(r3v);

    double cross[3];
    vtkMath::Cross(r2v, r3v, cross);

    double numerator = vtkMath::Dot(r1v, cross);
    double denominator = r1*r2*r3 + r1*vtkMath::Dot(r2v,r3v) + r2*vtkMath::Dot(r3v,r1v) + r3*vtkMath::Dot(r1v,r2v);

    if (fabs(numerator) < 1e-9)
        numerator = -0.0;

    return 2.0 * atan2(numerator, denominator);
}

static double compute_Le(const double fieldPoint[3], vtkIdType edgeId)
{
    const EdgeData& ed = edgeData[edgeId];

    vtkPoints* points = polyData->GetPoints();

    double pt1[3];
    double pt2[3];
    points->GetPoint(ed.p1, pt1);
    points->GetPoint(ed.p2, pt2);

    double r1v[3];
    vtkMath::Subtract(pt1, fieldPoint, r1v);
    double r1 = vtkMath::Norm(r1v);

    double r2v[3];
    vtkMath::Subtract(pt2, fieldPoint, r2v);
    double r2 = vtkMath::Norm(r2v);

    if ( fabs(r1 + r2 - ed.edgeLength) < 1e-9)
    {
        cout << "skipping " << edgeId << endl;
        return 0.0;
    }

    return log ( (r1 + r2 + ed.edgeLength) / (r1 + r2 - ed.edgeLength) );
}

double getPotential(const double fieldPoint[3], double* acc)
{
    double potential = 0.0;
    if (acc)
    {
        acc[0] = 0.0;
        acc[1] = 0.0;
        acc[2] = 0.0;
    }

    int twiceNumEdges = edgeData.size();
    for (vtkIdType i=0; i<twiceNumEdges; ++i)
    {
        const EdgeData& ed = edgeData[i];
        double Le = compute_Le(fieldPoint, i);

        potential += (ed.rEr * Le);

        if (acc)
        {
            acc[0] -= ed.Er[0]*Le;
            acc[1] -= ed.Er[1]*Le;
            acc[2] -= ed.Er[2]*Le;
        }
    }

    int numFaces = polyData->GetNumberOfCells();
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        const FaceData& fd = faceData[i];

        double wf = compute_wf(fieldPoint, i);

        potential -= (fd.rFr * wf);

        if (acc)
        {
            acc[0] += fd.Fr[0]*wf;
            acc[1] += fd.Fr[1]*wf;
            acc[2] += fd.Fr[2]*wf;
        }
    }

    return 0.5 * potential;
}
