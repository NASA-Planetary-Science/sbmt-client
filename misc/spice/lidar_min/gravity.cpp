#include <vtkPolyDataReader.h>
#include <vtkPolyData.h>
#include <vtkCellData.h>
#include <vtkMath.h>
#include <vtkPolyDataNormals.h>
#include <vtkTriangle.h>
#include <vector>
#include <tr1/unordered_map>

using namespace std;
using namespace std::tr1;

struct EdgeKey
{
    int p1;
    int p2;
};

struct EdgeHash
{
    size_t
    operator()(const EdgeKey& key) const
    {
        // This hash seems to produce efficient look ups. Not sure what
        // the best hash is though.
        return key.p1;
    }
};

static bool operator==(const EdgeKey& key1, const EdgeKey& key2)
{
    return key1.p1 == key2.p1 && key1.p2 == key2.p2;
}

struct EdgeData
{
    double E[3][3];
    double edgeLength;
    int p1;
    int p2;
};

struct FaceData
{
    double F[3][3];
    int p1;
    int p2;
    int p3;
};

struct PointData
{
    double r[3];
    double r_mag;
};

typedef unordered_map<EdgeKey, EdgeData, EdgeHash> EdgeDataMap;

static vector<EdgeData> edgeData;
static vector<FaceData> faceData;
static vtkPolyData* polyData = 0;
static vector<PointData> pointData;


static void addMatrices(double a[3][3], double b[3][3], double c[3][3])
{
    for (int i=0; i<3; ++i)
        for (int j=0; j<3; ++j)
            c[i][j] = a[i][j] + b[i][j];
}

static void Multiply3x3(const double A[3][3], const double v[3], double u[3])
{
    u[0] = A[0][0]*v[0] + A[0][1]*v[1] + A[0][2]*v[2];
    u[1] = A[1][0]*v[0] + A[1][1]*v[1] + A[1][2]*v[2];
    u[2] = A[2][0]*v[0] + A[2][1]*v[1] + A[2][2]*v[2];
}

static double Abs(double a)
{
    return (a <= 0.0) ? 0.0 - a : a;
}

/*
  // For debugging
static void printmatrix(double m[3][3])
{
    for (int i=0; i<3; ++i)
    {
        for (int j=0; j<3; ++j)
            cout << m[i][j] << " ";
        cout << endl;
    }
}

static void printvec(const char* str, double m[3])
{
    cout << str << " : ";
    for (int j=0; j<3; ++j)
        cout << m[j] << " ";
    cout << endl;
}
*/

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

    EdgeDataMap edgeDataMap;

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


    vtkPoints* points = polyData->GetPoints();
    vtkDataArray* normals = polyData->GetCellData()->GetNormals();

    vtkIdType *pts, npts;
    int pointIds[3];

    int numFaces = polyData->GetNumberOfCells();
    // Compute the edge data
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        polyData->GetCellPoints(i, npts, pts);
        // Copy point ids to new array immediately since not sure how long pts is valid
        // (probably not necessary to do this).
        pointIds[0] = pts[0];
        pointIds[1] = pts[1];
        pointIds[2] = pts[2];

        double cellNormal[3];
        normals->GetTuple(i, cellNormal);

        for (vtkIdType j=0; j<3; ++j)
        {
            vtkIdType p1;
            vtkIdType p2;
            if (j < 2)
            {
                p1 = pointIds[j];
                p2 = pointIds[j+1];
            }
            else
            {
                p1 = pointIds[2];
                p2 = pointIds[0];
            }

            // Put the point with the lowest id into ed so that
            // the 2 identical edges always have the same point
            EdgeKey key;
            if (p1 < p2)
            {
                key.p1 = p1;
                key.p2 = p2;
            }
            else
            {
                key.p1 = p2;
                key.p2 = p1;
            }

            // If key not found
            EdgeDataMap::iterator it = edgeDataMap.find(key);
            if (it == edgeDataMap.end())
            {
                EdgeData ed;
                ed.E[0][0] = ed.E[0][1] = ed.E[0][2] = 0.0;
                ed.E[1][0] = ed.E[1][1] = ed.E[1][2] = 0.0;
                ed.E[2][0] = ed.E[2][1] = ed.E[2][2] = 0.0;
                ed.edgeLength = 0.0;
                ed.p1 = key.p1;
                ed.p2 = key.p2;
                it = edgeDataMap.insert(pair<EdgeKey,EdgeData>(key,ed)).first;
            }

            EdgeData& ed = it->second;

            // Compute unit vector from p1 to p2
            double edgeUnitVector[3];
            double pt1[3];
            double pt2[3];
            points->GetPoint(p1, pt1);
            points->GetPoint(p2, pt2);
            vtkMath::Subtract(pt2, pt1, edgeUnitVector);
            ed.edgeLength = vtkMath::Normalize(edgeUnitVector);
            // Compute half of the E dyad
            double edgeNormal[3];
            vtkMath::Cross(edgeUnitVector, cellNormal, edgeNormal);

            double E[3][3];
            vtkMath::Outer(cellNormal, edgeNormal, E);

            addMatrices(ed.E, E, ed.E);
        }
    }

    // Now convert the edgeDataMap to a vector
    edgeData.resize(numFaces*3/2);
    int i = 0;
    EdgeDataMap::const_iterator it;
    EdgeDataMap::const_iterator end = edgeDataMap.end();
    for (it = edgeDataMap.begin(); it != end; ++it)
    {
        edgeData[i] = it->second;
        ++i;
    }


    // Compute the face data
    faceData.resize(numFaces);
    for (vtkIdType i=0; i<numFaces; ++i)
    {
        FaceData fd;
        polyData->GetCellPoints(i, npts, pts);

        fd.p1 = pts[0];
        fd.p2 = pts[1];
        fd.p3 = pts[2];

        // Compute the F dyad
        double normal[3];
        normals->GetTuple(i, normal);
        vtkMath::Outer(normal, normal, fd.F);

        faceData[i] = fd;
    }

    return polyData;
}

static double compute_wf(const FaceData& fd)
{
    const PointData& pd1 = pointData[fd.p1];
    const PointData& pd2 = pointData[fd.p2];
    const PointData& pd3 = pointData[fd.p3];

    double cross[3];
    vtkMath::Cross(pd2.r, pd3.r, cross);

    double numerator = vtkMath::Dot(pd1.r, cross);
    double denominator = pd1.r_mag*pd2.r_mag*pd3.r_mag +
            pd1.r_mag*vtkMath::Dot(pd2.r,pd3.r) +
            pd2.r_mag*vtkMath::Dot(pd3.r,pd1.r) +
            pd3.r_mag*vtkMath::Dot(pd1.r,pd2.r);

    if (Abs(numerator) < 1e-9)
        numerator = -0.0;

    return 2.0 * atan2(numerator, denominator);
}

static double compute_Le(const EdgeData& ed)
{
    const PointData& pd1 = pointData[ed.p1];
    const PointData& pd2 = pointData[ed.p2];

    if ( Abs(pd1.r_mag + pd2.r_mag - ed.edgeLength) < 1e-9)
    {
        return 0.0;
    }

    return log ( (pd1.r_mag + pd2.r_mag + ed.edgeLength) / (pd1.r_mag + pd2.r_mag - ed.edgeLength) );
}

double getGravity(const double fieldPoint[3], double* acc)
{
    double potential = 0.0;
    if (acc)
    {
        acc[0] = 0.0;
        acc[1] = 0.0;
        acc[2] = 0.0;
    }

    // Cache all the vectors from field point to vertices and their magnitudes
    vtkPoints* points = polyData->GetPoints();
    int numPoints = points->GetNumberOfPoints();
    pointData.resize(numPoints);
    for (int i=0; i<numPoints; ++i)
    {
        PointData& pd = pointData[i];
        points->GetPoint(i, pd.r);
        vtkMath::Subtract(pd.r, fieldPoint, pd.r);
        pd.r_mag = vtkMath::Norm(pd.r);
    }


    double Er[3];
    double rEr;
    double Fr[3];
    double rFr;

    int numEdges = edgeData.size();
    for (int i=0; i<numEdges; ++i)
    {
        const EdgeData& ed = edgeData[i];

        // Any vertex of the cell will do, so just choose the first one.
        const PointData& pd = pointData[ed.p1];

        double Le = compute_Le(ed);

        Multiply3x3(ed.E, pd.r, Er);
        rEr = vtkMath::Dot(pd.r, Er);
        potential -= (rEr * Le);

        if (acc)
        {
            acc[0] -= Er[0]*Le;
            acc[1] -= Er[1]*Le;
            acc[2] -= Er[2]*Le;
        }
    }

    int numFaces = polyData->GetNumberOfCells();
    for (int i=0; i<numFaces; ++i)
    {
        const FaceData& fd = faceData[i];

        // Any vertex of the cell will do, so just choose the first one.
        const PointData& pd = pointData[fd.p1];

        double wf = compute_wf(fd);

        Multiply3x3(fd.F, pd.r, Fr);
        rFr = vtkMath::Dot(pd.r, Fr);

        potential += (rFr * wf);

        if (acc)
        {
            acc[0] += Fr[0]*wf;
            acc[1] += Fr[1]*wf;
            acc[2] += Fr[2]*wf;
        }
    }

    return 0.5 * potential;
}
