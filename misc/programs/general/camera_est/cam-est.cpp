#include <iostream>
#include <fstream>
#include <vector>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "lbfgs.h"
#include "optimize.h"
#include "optimize-gsl.h"
#include "adolc/adolc.h"
#include "util.h"

using namespace std;

struct Pixel
{
    double x;
    double y;

    Pixel(double a, double b):
        x(a), y(b)
    {
    }
};

const unsigned int N = 7; // number of independent variables
double focal_length = 10066.666666666666;
unsigned int imageWidth = 1024;
unsigned int imageHeight = 1024;
vector<Point> objectPoints;
vector<Pixel> imagePoints;


void getMatrixFromQuaternion(
        const double q0,
        const double q1,
        const double q2,
        const double q3,
        double m[3][3]
        )
{
    //  Copied from Rotation.java in Apache Commons Math

    // products
    double q0q0  = q0 * q0;
    double q0q1  = q0 * q1;
    double q0q2  = q0 * q2;
    double q0q3  = q0 * q3;
    double q1q1  = q1 * q1;
    double q1q2  = q1 * q2;
    double q1q3  = q1 * q3;
    double q2q2  = q2 * q2;
    double q2q3  = q2 * q3;
    double q3q3  = q3 * q3;

    m [0][0] = 2.0 * (q0q0 + q1q1) - 1.0;
    m [1][0] = 2.0 * (q1q2 - q0q3);
    m [2][0] = 2.0 * (q1q3 + q0q2);

    m [0][1] = 2.0 * (q1q2 + q0q3);
    m [1][1] = 2.0 * (q0q0 + q2q2) - 1.0;
    m [2][1] = 2.0 * (q2q3 - q0q1);

    m [0][2] = 2.0 * (q1q3 - q0q2);
    m [1][2] = 2.0 * (q2q3 + q0q1);
    m [2][2] = 2.0 * (q0q0 + q3q3) - 1.0;
}


template <class Double>
void applyRotationToVector(
        const Double vec[3],
        const Double& q0,
        const Double& q1,
        const Double& q2,
        const Double& q3,
        Double rotatedVector[3]
        )
{
    //  Copied from Rotation.java in Apache Commons Math
    const Double& x = vec[0];
    const Double& y = vec[1];
    const Double& z = vec[2];

    Double s = q1 * x + q2 * y + q3 * z;
    rotatedVector[0] = 2 * (q0 * (x * q0 - (q2 * z - q3 * y)) + s * q1) - x;
    rotatedVector[1] = 2 * (q0 * (y * q0 - (q3 * x - q1 * z)) + s * q2) - y;
    rotatedVector[2] = 2 * (q0 * (z * q0 - (q1 * y - q2 * x)) + s * q3) - z;
}

template <class Double>
void getPixelFromPoint(
        const Point& point,
        const Double scpos[3],
        const Double& q0,
        const Double& q1,
        const Double& q2,
        const Double& q3,
        Double& pixelx,
        Double& pixely
        )
{
    Double vec[3] = {
        point.p[0] - scpos[0],
        point.p[1] - scpos[1],
        point.p[2] - scpos[2]
    };

    Double rotatedVector[3];

    applyRotationToVector(vec,q0,q1,q2,q3,rotatedVector);

    pixelx = focal_length * rotatedVector[0] / rotatedVector[2];
    pixely = focal_length * rotatedVector[1] / rotatedVector[2];

    pixelx += ((imageWidth-1.0)/2.0);
    pixely += ((imageHeight-1.0)/2.0);
}

template <class Double>
void normalizeQuaternion(
        Double& q0,
        Double& q1,
        Double& q2,
        Double& q3)
{
    //  Copied from Rotation.java in Apache Commons Math
    Double inv = 1.0 / sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
    q0 *= inv;
    q1 *= inv;
    q2 *= inv;
    q3 *= inv;
}

double func(const double* u, void* params)
{
    double funcValue = 0.0;

    adouble funcValueA = 0.0;
    adouble q0;
    adouble q1;
    adouble q2;
    adouble q3;
    adouble scpos[3];
    adouble projectedPixelX;
    adouble projectedPixelY;
    adouble distx;
    adouble disty;
    adouble* uA = new adouble[N];

    trace_on(1);

    for (unsigned int i=0; i<N; ++i)
        uA[i] <<= u[i];

    scpos[0] = uA[0];
    scpos[1] = uA[1];
    scpos[2] = uA[2];
    q0 = uA[3];
    q1 = uA[4];
    q2 = uA[5];
    q3 = uA[6];

    // normalize the quaternion
    normalizeQuaternion(q0, q1, q2, q3);

    for (unsigned int i=0; i<objectPoints.size(); ++i)
    {
        const Point& point = objectPoints[i];
        const Pixel& pixel = imagePoints[i];

        getPixelFromPoint(point, scpos, q0, q1, q2, q3, projectedPixelX, projectedPixelY);

        //cout<< i << " " << projectedPixelX << " " << projectedPixelY << endl;

        distx = pixel.x - projectedPixelX;
        disty = pixel.y - projectedPixelY;

        funcValueA += distx*distx + disty*disty;
    }

    funcValueA = sqrt(funcValueA / objectPoints.size());

    funcValueA >>= funcValue;

    trace_off();

    //printTapestats();

    delete [] uA;

    return funcValue;
}

void grad(const double* u, double* df, void* params)
{
    func(u, params); // return value not used
    gradient(1,N,u,df);
}

void loadPointsAndInitialOrientation(const char* inputfile, double initialOrientation[N])
{
    ifstream fin(inputfile);
    if (fin.is_open())
    {
        string line;
        int count = 0;
        while (getline(fin, line))
        {
            trim(line);
            vector<string> tokens = split(line);

            if (count == 0)
            {
                if (tokens.size() != N)
                {
                    cerr << "Error: First line of input file must contain initial orientation of camera, " << endl;
                    cerr << "but it is incorrectly formatted." << endl;
                    exit(1);
                }

                for (unsigned int i=0; i<N; ++i)
                    initialOrientation[i] = atof(tokens[i].c_str());
            }
            else
            {
                if (tokens.size() == 2)
                {
                    imagePoints.push_back(Pixel(atof(tokens[0].c_str()),
                                                atof(tokens[1].c_str())));
                }
                else if (tokens.size() == 3)
                {
                    objectPoints.push_back(Point(atof(tokens[0].c_str()),
                                                 atof(tokens[1].c_str()),
                                                 atof(tokens[2].c_str())));
                }
                else
                {
                    cerr << "Error: Input file incorrectly formatted." << endl;
                    exit(1);
                }
            }
            ++count;
        }
    }
    else
    {
        cerr << "Error: Unable to open file '" << inputfile << "'" << endl;
        exit(1);
    }

    if (imagePoints.size() != objectPoints.size())
    {
        cerr << "Error: Number of image points not equal to number of object points." << endl;
        exit(1);
    }

    if (imagePoints.size() == 0)
    {
        cerr << "Error: No image or object points provided." << endl;
        exit(1);
    }

    if (imagePoints.size() < 4)
    {
        cerr << "Warning: Too few points provided. Algorithm may not converge." << endl;
    }
}

void saveSumfile(const char* sumfilename,
                 const double scpos[3],
                 const double q0,
                 const double q1,
                 const double q2,
                 const double q3)
{
    ofstream fout(sumfilename);
    if (!fout.is_open())
    {
        cerr << "Error: Unable to open file for writing" << endl;
        exit(1);
    }
    fout.precision(16);

    fout << "IMAGE-NAME\n";
    fout << "2000 JAN 01 00:00:00.000\n";
    fout << imageWidth << " " << imageHeight << " 0 0 NPX, NLN, THRSH\n";
    fout << "0 0 0 MMFL, CTR\n";
    fout << -scpos[0] << " " << -scpos[1] << " " << -scpos[2] << " SCOBJ\n";

    double m[3][3];
    getMatrixFromQuaternion(q0, q1, q2, q3, m);

    fout << m[0][0] << " " << m[0][1] << " " << m[0][2] << " CX\n";
    fout << m[1][0] << " " << m[1][1] << " " << m[1][2] << " CY\n";
    fout << m[2][0] << " " << m[2][1] << " " << m[2][2] << " CZ\n";
    fout << "0 0 1 SZ\n";

    fout << flush;

    fout.close();
}

void printResults(
        const double scpos[3],
        const double q0,
        const double q1,
        const double q2,
        const double q3)
{
    for (unsigned int i=0; i<objectPoints.size(); ++i)
    {
        const Point& point = objectPoints[i];
        const Pixel& pixel = imagePoints[i];

        double projectedPixelX;
        double projectedPixelY;
        getPixelFromPoint(point, scpos, q0, q1, q2, q3, projectedPixelX, projectedPixelY);

        double distx = pixel.x - projectedPixelX;
        double disty = pixel.y - projectedPixelY;

        double error = sqrt(distx*distx + disty*disty);

        //printf("Point %d: %10.4g,%10.4g,%10.4g projected to %10.4g,%10.4g true value is %10.4g,%10.4g error is %10.4g pixels\n",
        //       i, point.p[0], point.p[1], point.p[2], projectedPixelX, projectedPixelY,
        //       pixel.x, pixel.y, error);
        cout << "Point " << (i+1) <<  ": " << point.p[0] << "," << point.p[1] << "," << point.p[2]
             << " projected to " << projectedPixelX << "," << projectedPixelY
             << " true value is " << pixel.x << "," << pixel.y
             << " error is " << error << " pixels" << endl;
    }
}

static void usage()
{
    exit(0);
}

int main(int argc, char** argv)
{
    const int numberRequiredArgs = 2;
    if (argc-1 < numberRequiredArgs)
        usage();

    int i = 1;
    for(; i<argc; ++i)
    {
        if (!strcmp(argv[i], "-f"))
        {
            focal_length = atof(argv[++i]);
        }
        else if (!strcmp(argv[i], "-w"))
        {
            imageWidth = atoi(argv[++i]);
        }
        else if (!strcmp(argv[i], "-h"))
        {
            imageHeight = atoi(argv[++i]);
        }
        else
        {
            break;
        }
    }


    // There must be numRequiredArgs arguments remaining after the options. Otherwise abort.
    if (argc - i != numberRequiredArgs)
        usage();

    const char* inputfile = argv[i];
    const char* sumfile = argv[i+1];

    double u[N];
    loadPointsAndInitialOrientation(inputfile, u);

    double prevError = func(u, 0);
    while (true)
    {
        //optimizeGsl(func, grad, &u[0], N, 0);
        optimizeLbfgs(func, grad, &u[0], N, 0);

        double currentError = func(u, 0);

        if (currentError >= prevError)
            break;

        prevError = currentError;
    }

    double scpos[3] = {u[0], u[1], u[2]};
    double q0 = u[3];
    double q1 = u[4];
    double q2 = u[5];
    double q3 = u[6];

    normalizeQuaternion(q0, q1, q2, q3);

    cout << "final values: "
         << scpos[0] << " "
         << scpos[1] << " "
         << scpos[2] << " "
         << q0 << " "
         << q1 << " "
         << q2 << " "
         << q3 << "\n" << endl;

    saveSumfile(sumfile, scpos, q0, q1, q2, q3);

    printResults(scpos, q0, q1, q2, q3);

    return 0;
}
