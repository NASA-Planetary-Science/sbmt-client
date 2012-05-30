#ifndef SPICEFORTRAN_H_
#define SPICEFORTRAN_H_

/*
  This program links to the FORTRAN version of spice, NOT the C
  version. Therefore this file contains function prototypes of the
  Fortran functions used throughout this program.
 */

void furnsh_(char*,
			 int size);
void rdplat_(char*,
			 int size);
void spkpos_(char* targ,
			 double* et,
			 char* ref,
			 char* abcorr,
			 char* obs,
			 double ptarg[3],
			 double* lt,
			 int size1,
			 int size2,
			 int size3,
			 int size4);
void spkezr_(char* targ,
			 double* et,
			 char* ref,
			 char* abcorr,
			 char* obs,
			 double starg[3],
			 double* lt,
			 int size1,
			 int size2,
			 int size3,
			 int size4);
void pxform_(char* from,
			 char* to,
			 double* et,
			 double rotate[3][3],
			 int size1,
			 int size2);
void vpack_ (double* x,
			 double* y,
			 double* z,
			 double v[3]);
void mxv_   (double m1[3][3],
			 double vin[3],
			 double vout[3]);
void plbore_(double posobs[3],
			 double bsight[3],
			 int* plid,
			 int* body,
			 double xyzhit[3],
			 int* found);
void reclat_(double rectan[3],
			 double* radius,
			 double* longitude,
			 double* latitude);
void utc2et_(char* utcstr,
			 double* et,
			 int size);
void et2utc_(double* et,
			 char* format,
			 int* prec,
			 char* utcstr,
			 int size1,
			 int size2);
void pltang_(int* plid,
			 int* body,
			 int* target,
			 double* et,
			 char* corr,
			 double* angle,
			 int size);
void plsang_(int* plid,
			 int* body,
			 int* obs,
			 double* et,
			 char* corr,
			 double* angle,
			 double* visamt,
			 double* litamt,
			 int size);
double vdist_(double v1[3],
			  double v2[3]);
void ktotal_ (char* kind,
			  int* count,
			  int size);
void kdata_  (int* which,
			  char* kind,
			  char* file,
			  char* filtyp,
			  char* source,
			  int* handle,
			  int* found,
			  int size1,
			  int size2,
			  int size3,
			  int size4);
double dpr_();
void vproj_(double a[3],
			double b[3],
			double p[3]);
void unorm_(double v1[3],
			double vout[3],
			double* vmag);
void scs2e_(int* sc,
			char* sclkch,
			double* et,
			int size);
	

#endif
