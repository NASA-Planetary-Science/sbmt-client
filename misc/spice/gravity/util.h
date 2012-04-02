#ifndef MATHUTIL_H
#define MATHUTIL_H

#include <math.h>

// The following functions are copied directly from VTK

inline void Subtract(const double a[3], const double b[3], double c[3]) {
  for (int i = 0; i < 3; ++i)
    c[i] = a[i] - b[i];
}

inline double Norm(const double x[3]) {
  return sqrt( x[0] * x[0] + x[1] * x[1] + x[2] * x[2] );}

inline double Normalize(double x[3])
{
  double den;
  if ( ( den = Norm( x ) ) != 0.0 )
    {
    for (int i=0; i < 3; i++)
      {
      x[i] /= den;
      }
    }
  return den;
}

inline void Cross(const double x[3], const double y[3], double z[3])
{
  double Zx = x[1] * y[2] - x[2] * y[1];
  double Zy = x[2] * y[0] - x[0] * y[2];
  double Zz = x[0] * y[1] - x[1] * y[0];
  z[0] = Zx; z[1] = Zy; z[2] = Zz;
}

inline double Dot(const double x[3], const double y[3]) {
  return ( x[0] * y[0] + x[1] * y[1] + x[2] * y[2] );}

inline void Outer(const double x[3], const double y[3], double A[3][3]) {
  for (int i=0; i < 3; i++)
    for (int j=0; j < 3; j++)
      A[i][j] = x[i] * y[j];
}

inline void MultiplyScalar(double a[3], double s) {
  for (int i = 0; i < 3; ++i)
    a[i] *= s;
}

inline void ComputeNormalDirection(double v1[3], double v2[3],
                                   double v3[3], double n[3])
{
  double ax, ay, az, bx, by, bz;

  // order is important!!! maintain consistency with triangle vertex order
  ax = v3[0] - v2[0]; ay = v3[1] - v2[1]; az = v3[2] - v2[2];
  bx = v1[0] - v2[0]; by = v1[1] - v2[1]; bz = v1[2] - v2[2];

  n[0] = (ay * bz - az * by);
  n[1] = (az * bx - ax * bz);
  n[2] = (ax * by - ay * bx);
}

inline void ComputeNormal(double v1[3], double v2[3],
                          double v3[3], double n[3])
{
  double length;

  ComputeNormalDirection(v1, v2, v3, n);

  if ( (length = sqrt((n[0]*n[0] + n[1]*n[1] + n[2]*n[2]))) != 0.0 )
    {
    n[0] /= length;
    n[1] /= length;
    n[2] /= length;
    }
}

inline void TriangleCenter(double p1[3], double p2[3],
                           double p3[3], double center[3])
{
  center[0] = (p1[0]+p2[0]+p3[0]) / 3.0;
  center[1] = (p1[1]+p2[1]+p3[1]) / 3.0;
  center[2] = (p1[2]+p2[2]+p3[2]) / 3.0;
}

inline double Distance2BetweenPoints(const double x[3],
                                     const double y[3])
{
  return ( ( x[0] - y[0] ) * ( x[0] - y[0] )
           + ( x[1] - y[1] ) * ( x[1] - y[1] )
           + ( x[2] - y[2] ) * ( x[2] - y[2] ) );
}

inline double TriangleArea(double p1[3], double p2[3], double p3[3])
{
  double a,b,c;
  a = Distance2BetweenPoints(p1,p2);
  b = Distance2BetweenPoints(p2,p3);
  c = Distance2BetweenPoints(p3,p1);
  return (0.25* sqrt(fabs(4.0*a*c - (a-b+c)*(a-b+c))));
}

#endif // MATHUTIL_H
