c ..................................................................
c         SELECTED SPICE ROUTINES     
c ..................................................................

C$Procedure      VNORM ( Vector norm, 3 dimensions )
 
      DOUBLE PRECISION FUNCTION VNORM ( V1 )
 
C$ Abstract
C
C      Compute the magnitude of a double precision, 3-dimensional
C      vector.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION  V1 ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       V1        I     Vector whose magnitude is to be found.
C
C$ Detailed_Input
C
C      V1      This may be any 3-dimensional, double precision vector.
C
C$ Detailed_Output
C
C      VNORM is the magnitude of V1 calculated in a numerically stable
C      way.
C
C$ Parameters
C
C     None.
C
C$ Particulars
C
C      VNORM finds the component of V1 whose magnitude is the largest.
C      If the absolute magnitude of that component indicates that a
C      numeric overflow would occur when it is squared, or if it
C      indicates that an underflow would occur when square (giving a
C      magnitude of zero) then the following expression is used:
C
C      VNORM = V1MAX * MAGNITUDE OF [ (1/V1MAX)*V1 ]
C
C      Otherwise a simpler expression is used:
C
C      VNORM = MAGNITUDE OF [ V1 ]
C
C      Beyond the logic described above, no further checking of the
C      validity of the input is performed.
C
C$ Examples
C
C      The following table show the correlation between various input
C      vectors V1 and VNORM:
C
C      V1                                    VNORM
C      -----------------------------------------------------------------
C      (1.D0, 2.D0, 2.D0)                     3.D0
C      (5.D0, 12.D0, 0.D0)                   13.D0
C      (-5.D-17, 0.0D0, 12.D-17)             13.D-17
C
C$ Restrictions
C
C      None.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     norm of 3-dimensional vector
C
C-&
 
      DOUBLE PRECISION V1MAX
C
C  Determine the maximum component of the vector.
C
      V1MAX = MAX (DABS(V1(1)), DABS(V1(2)), DABS(V1(3)))
C
C  If the vector is zero, return zero; otherwise normalize first.
C  Normalizing helps in the cases where squaring would cause overflow
C  or underflow.  In the cases where such is not a problem it not worth
C  it to optimize further.
C
      IF (V1MAX.EQ.0.D0) THEN
         VNORM = 0.D0
      ELSE
         VNORM = V1MAX * DSQRT (  (V1(1)/V1MAX)**2
     .                          + (V1(2)/V1MAX)**2
     .                          + (V1(3)/V1MAX)**2)
      END IF
C
      RETURN
      END

C$Procedure      VDOT  ( Vector dot product, 3 dimensions )
 
      DOUBLE PRECISION FUNCTION VDOT ( V1, V2 )
 
C$ Abstract
C
C      Compute the dot product of two double precision, 3-dimensional
C      vectors.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C      None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION   V1 ( 3 )
      DOUBLE PRECISION   V2 ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       V1        I     First vector in the dot product.
C       V2        I     Second vector in the dot product.
C
C       The function returns the value of the dot product of V1 and V2.
C
C$ Detailed_Input
C
C      V1      This may be any 3-dimensional, double precision vector.
C
C      V2      This may be any 3-dimensional, double precision vector.
C
C$ Detailed_Output
C
C      The function returns the value of the dot product of V1 and V2.
C
C$ Parameters
C
C      None.
C
C$ Particulars
C
C      VDOT calculates the dot product of V1 and V2 by a simple
C      application of the definition.  No error checking is
C      performed to prevent numeric overflow.
C
C$ Examples
C
C      Suppose that given two position vectors, we want to change
C      one of the positions until the two vectors are perpendicular.
C      The following code fragment demonstrates the use of VDOT to do
C      so.
C
C      DOT = VDOT ( V1, V2 )
C
C      DO WHILE ( DOT .NE. 0.0D0 )
C         change one of the position vectors
C         DOT = VDOT ( V1, V2 )
C      END DO
C
C$ Restrictions
C
C      The user is responsible for determining that the vectors V1 and
C      V2 are not so large as to cause numeric overflow.  In most cases
C      this won't present a problem.
C
C$ Exceptions
C
C     Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-    SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C        Comment section for permuted index source lines was added
C        following the header.
C
C-    SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     dot product 3-dimensional vectors
C
C-&
 
 
      VDOT = V1(1)*V2(1) + V1(2)*V2(2) + V1(3)*V2(3)
C
      RETURN
      END

C$Procedure                     RPD ( Radians per degree )
 
      DOUBLE PRECISION FUNCTION RPD ( )
 
C$ Abstract
C
C     Return the number of radians per degree.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C     CONSTANTS
C
C$ Declarations
C
C     None.
C
C$ Brief_I/O
C
C     The function returns the number of radians per degree.
C
C$ Detailed_Input
C
C     None.
C
C$ Detailed_Output
C
C     The function returns the number of radians per degree: pi/180.
C     The value of pi is determined by the ACOS function. That is,
C
C           RPD = ACOS ( -1.D0 ) / 180.D0
C
C$ Parameters
C
C     None.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C     None.
C
C$ Particulars
C
C     The first time the function is referenced, the value is computed
C     as shown above. The value is saved, and returned directly upon
C     subsequent reference.
C
C$ Examples
C
C     The code fragment below illustrates the use of RPD.
C
C        C
C        C     Convert all input angles to radians.
C        C
C              CLOCK = CLOCK * RPD()
C              CONE  = CONE  * RPD()
C              TWIST = TWIST * RPD()
C
C     or equivalently,
C
C        C
C        C     Convert all input angles to radians.
C        C
C              CALL VPACK  ( CLOCK, CONE, CCTWIST, ALBTGAM )
C              CALL VSCL   ( RPD(), ALBTGAM, ALBTGAM )
C              CALL VUPACK ( ALBTGAM, CLOCK, CONE, CCTWIST )
C
C$ Restrictions
C
C     None.
C
C$ Literature_References
C
C     None.
C
C$ Author_and_Institution
C
C     W.L. Taber      (JPL)
C     I.M. Underwood  (JPL)
C
C$ Version
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WLT) (IMU)
C
C-&
 
C$ Index_Entries
C
C     radians per degree
C
C-&
 
 
 
 
C
C     Local variables
C
      DOUBLE PRECISION      VALUE
      SAVE                  VALUE
 
C
C     Initial values
C
      DATA                  VALUE      / 0.D0 /
 
 
C
C     What is there to say?
C
      IF ( VALUE .EQ. 0.D0 ) THEN
         VALUE = ACOS ( -1.D0 ) / 180.D0
      END IF
 
      RPD = VALUE
 
      RETURN
      END
 
C$Procedure                     SPD ( Seconds per day )
 
      DOUBLE PRECISION FUNCTION SPD ( )
 
C$ Abstract
C
C     Return the number of seconds in a day.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C     CONSTANTS
C
C$ Declarations
C
C     None.
C
C$ Brief_I/O
C
C     The function returns the number of seconds in a day.
C
C$ Detailed_Input
C
C     None.
C
C$ Detailed_Output
C
C     The function returns the number of seconds in a day: 86400.
C
C$ Parameters
C
C     None.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C     None.
C
C$ Particulars
C
C     The function always returns the constant value shown above.
C
C$ Examples
C
C     The following code fragment illustrates the use of SPD.
C
C        C
C        C     Convert Julian Date to UTC seconds past the reference
C        C     epoch (J2000).
C        C
C              SPREF = ( JD - J2000() ) * SPD()
C
C$ Restrictions
C
C     None.
C
C$ Literature_References
C
C     None.
C
C$ Author_and_Institution
C
C     W.L. Taber      (JPL)
C     I.M. Underwood  (JPL)
C
C$ Version
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WLT) (IMU)
C
C-&
 
C$ Index_Entries
C
C     seconds per day
C
C-&
 
 
C
C     Just like it says.
C
      SPD = 86400.D0
 
      RETURN
      END

C$Procedure      UCRSS ( Unitized cross product, 3x3 )
 
      SUBROUTINE UCRSS ( V1, V2, VOUT )
 
C$ Abstract
C
C      Compute the normalized cross product of two 3-vectors.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION   V1   ( 3 )
      DOUBLE PRECISION   V2   ( 3 )
      DOUBLE PRECISION   VOUT ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       V1        I     Left vector for cross product.
C       V2        I     Right vector for cross product.
C       VOUT      O     Normalized cross product (V1xV2) / |V1xV2|.
C
C$ Detailed_Input
C
C      V1   A 3-vector.
C
C      V2   A 3-vector.
C
C$ Detailed_Output
C
C      VOUT is the result of the computation (V1xV2)/|V1xV2|
C
C$ Parameters
C
C      None.
C
C$ Particulars
C
C      None.
C
C$ Examples
C
C      To get a unit normal to the plane spanned by two vectors
C      V1 and V2. Simply call
C
C         CALL UCRSS ( V1, V2, NORMAL )
C
C$ Restrictions
C
C      None.
C
C$ Exceptions
C
C     Error free.
C
C     1) If the cross product of V1 and V2 yields the zero-vector, then
C        the zero-vector is returned instead of a vector of unit length.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C      W.L. Taber      (JPL)
C
C$ Literature_References
C
C      None
C
C$ Version
C
C-    SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C        Comment section for permuted index source lines was added
C        following the header.
C
C-    SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     unitized cross product
C
C-&
 
C$ Revisions
C
C-    Beta Version 1.1.0, 10-JAN-1989 (WLT)
C
C     Error free specification added. In addition the algorithm was made
C     more robust in the sense that floating point overflows cannot
C     occur.
C
C-&
      DOUBLE PRECISION      VNORM
      DOUBLE PRECISION      VCROSS(3)
      DOUBLE PRECISION      VMAG
 
      DOUBLE PRECISION      MAXV1
      DOUBLE PRECISION      MAXV2
 
      DOUBLE PRECISION      TV1 ( 3 )
      DOUBLE PRECISION      TV2 ( 3 )
 
C
C     Get the biggest component of each of the two vectors.
C
      MAXV1 = MAX ( DABS(V1(1)), DABS(V1(2)), DABS(V1(3)) )
      MAXV2 = MAX ( DABS(V2(1)), DABS(V2(2)), DABS(V2(3)) )
 
C
C     Scale V1 and V2 by 1/MAXV1 and 1/MAXV2 respectively
C
      IF ( MAXV1 .NE. 0 ) THEN
         TV1(1) = V1(1)/MAXV1
         TV1(2) = V1(2)/MAXV1
         TV1(3) = V1(3)/MAXV1
      ELSE
         TV1(1) = 0.0D0
         TV1(2) = 0.0D0
         TV1(3) = 0.0D0
      END IF
 
      IF ( MAXV2 .NE. 0 ) THEN
         TV2(1) = V2(1)/MAXV2
         TV2(2) = V2(2)/MAXV2
         TV2(3) = V2(3)/MAXV2
      ELSE
         TV2(1) = 0.0D0
         TV2(2) = 0.0D0
         TV2(3) = 0.0D0
      END IF
 
C
C  Calculate the cross product of V1 and V2
C
      VCROSS(1) = TV1(2)*TV2(3) - TV1(3)*TV2(2)
      VCROSS(2) = TV1(3)*TV2(1) - TV1(1)*TV2(3)
      VCROSS(3) = TV1(1)*TV2(2) - TV1(2)*TV2(1)
C
C  Get the magnitude of VCROSS and normalize it
C
      VMAG = VNORM(VCROSS)
 
      IF (VMAG.GT.0.D0) THEN
         VOUT(1) = VCROSS(1) / VMAG
         VOUT(2) = VCROSS(2) / VMAG
         VOUT(3) = VCROSS(3) / VMAG
      ELSE
         VOUT(1) = 0.D0
         VOUT(2) = 0.D0
         VOUT(3) = 0.D0
      END IF
 
      RETURN
      END

C$Procedure      VHAT ( "V-Hat", unit vector along V, 3 dimensions )
 
      SUBROUTINE VHAT ( V1, VOUT )
 
C$ Abstract
C
C      Find the unit vector along a double precision 3-dimensional
C      vector.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION   V1   ( 3 )
      DOUBLE PRECISION   VOUT ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       V1        I     Vector to be normalized.
C       VOUT      O     Unit vector V1 / |V1|.
C                       If V1 = 0, VOUT will also be zero.
C                       VOUT can overwrite V1.
C
C$ Detailed_Input
C
C      V1      This is any double precision, 3-dimensional vector.  If
C              this vector is the zero vector, this routine will detect
C              it, and will not attempt to divide by zero.
C
C$ Detailed_Output
C
C      VOUT    VOUT contains the unit vector in the direction of V1. If
C              V1 represents the zero vector, then VOUT will also be the
C              zero vector.  VOUT may overwrite V1.
C
C$ Parameters
C
C     None.
C
C$ Particulars
C
C      VHAT determines the magnitude of V1 and then divides each
C      component of V1 by the magnitude.  This process is highly stable
C      over the whole range of 3-dimensional vectors.
C
C$ Examples
C
C      The following table shows how selected V1 implies VOUT.
C
C      V1                    VOUT
C      ------------------    ------------------
C      (5, 12, 0)            (5/13, 12/13, 0)
C      (1D-7, 2D-7, 2D-7)    (1/3, 2/3, 2/3)
C
C
C$ Restrictions
C
C      There is no known case whereby floating point overflow may occur.
C      Thus, no error recovery or reporting scheme is incorporated
C      into this subroutine.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      N.J. Bachman    (JPL)
C      H.A. Neilan     (JPL)
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     unitize a 3-dimensional vector
C
C-&
 
 
C$ Revisions
C
C-     Beta Version 1.1.0, 10-FEB-1989 (HAN) (NJB)
C
C         Contents of the Exceptions section was changed
C         to "error free" to reflect the decision that the
C         module will never participate in error handling.
C         Also, the declaration of the unused variable I was
C         removed.
C-&
 
      DOUBLE PRECISION VNORM
      DOUBLE PRECISION VMAG
C
C  Obtain the magnitude of V1
C
      VMAG = VNORM(V1)
C
C   If VMAG is nonzero, then normalize.  Note that this process is
C   numerically stable: overflow could only happen if VMAG were small,
C   but this could only happen if each component of V1 were small.
C   In fact, the magnitude of any vector is never less than the
C   magnitude of any component.
C
      IF (VMAG.GT.0.D0) THEN
         VOUT(1) = V1(1) / VMAG
         VOUT(2) = V1(2) / VMAG
         VOUT(3) = V1(3) / VMAG
      ELSE
         VOUT(1) = 0.D0
         VOUT(2) = 0.D0
         VOUT(3) = 0.D0
      END IF
 
      RETURN
      END

C$Procedure      LATREC ( Latitudinal to rectangular coordinates )
 
      SUBROUTINE LATREC ( RADIUS, LONG, LAT, RECTAN )
 
C$ Abstract
C
C     Convert from latitudinal coordinates to rectangular coordinates.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C     CONVERSION,  COORDINATES
C
C$ Declarations
 
      DOUBLE PRECISION   RADIUS
      DOUBLE PRECISION   LONG
      DOUBLE PRECISION   LAT
      DOUBLE PRECISION   RECTAN ( 3 )
 
C$ Brief_I/O
C
C     VARIABLE  I/O  DESCRIPTION
C     --------  ---  --------------------------------------------------
C     RADIUS     I   Distance of a point from the origin.
C     LONG       I   Longitude of point in radians. 
C     LAT        I   Latitude of point in radians. 
C     RECTAN     O   Rectangular coordinates of the point.
C
C$ Detailed_Input
C
C     RADIUS     Distance of a point from the origin.
C
C     LONG       Longitude of the input point.  This is the angle 
C                between the prime meridian and the meridian
C                containing the point.  The direction of increasing
C                longitude is from the +X axis towards the +Y axis.
C 
C                Longitude is measured in radians.  On input, the 
C                range of longitude is unrestricted.
C
C     LAT        Latitude of the input point.  This is the angle from
C                the XY plane of the ray from the origin through the
C                point.
C
C                Latitude is measured in radians. On input, the range
C                of latitude is unrestricted.
C
C$ Detailed_Output
C
C     RECTAN     The rectangular coordinates of the input point.
C                RECTAN is a 3-vector.
C
C                The units associated with RECTAN are those
C                associated with the input RADIUS.
C
C$ Parameters
C
C     None.
C
C$ Exceptions
C
C     Error free.
C
C$ Files
C
C     None.
C
C$ Particulars
C
C     This routine returns the rectangular coordinates of a point
C     whose position is input in latitudinal coordinates.
C
C     Latitudinal coordinates are defined by a distance from a central
C     reference point, an angle from a reference meridian, and an angle
C     above the equator of a sphere centered at the central reference
C     point.
C
C$ Examples
C
C     Below are two tables.
C
C     Listed in the first table (under R, LONG and LAT) are
C     latitudinal coordinate triples that approximately represent
C     points whose rectangular coordinates are taken from the set
C     {-1, 0, 1}.  (Angular quantities are given in degrees.)
C
C     The results of the code fragment
C
C          C
C          C     Use the SPICELIB routine CONVRT to convert the angular
C          C     quantities to radians
C          C
C                CALL CONVRT ( LAT,  'DEGREES', 'RADIANS', LAT  )
C                CALL CONVRT ( LONG, 'DEGREES', 'RADIANS', LONG )
C
C                CALL LATREC ( R, LONG, LAT, X )
C
C
C     are listed in the second parallel table under X(1), X(2) and X(3).
C
C
C       R         LONG       LAT           X(1)       X(2)     X(3)
C       --------------------------         --------------------------
C       0.0000    0.0000    0.0000         0.0000     0.0000   0.0000
C       1.0000    0.0000    0.0000         1.0000     0.0000   0.0000
C       1.0000   90.0000    0.0000         0.0000     1.0000   0.0000
C       1.0000    0.0000   90.0000         0.0000     0.0000   1.0000
C       1.0000  180.0000    0.0000        -1.0000     0.0000   0.0000
C       1.0000  -90.0000    0.0000         0.0000    -1.0000   0.0000
C       1.0000    0.0000  -90.0000         0.0000     0.0000  -1.0000
C       1.4142   45.0000    0.0000         1.0000     1.0000   0.0000
C       1.4142    0.0000   45.0000         1.0000     0.0000   1.0000
C       1.4142   90.0000   45.0000         0.0000     1.0000   1.0000
C       1.7320   45.0000   35.2643         1.0000     1.0000   1.0000
C
C$ Restrictions
C
C     None.
C
C$ Author_and_Institution
C
C     C.H. Acton      (JPL)
C     N.J. Bachman    (JPL)
C     W.L. Taber      (JPL)
C
C$ Literature_References
C
C     None.
C
C$ Version
C
C-    SPICELIB Version 1.0.2, 29-JUL-2003 (NJB) (CHA)
C
C        Various header changes were made to improve clarity.  Some
C        minor header corrections were made.
C
C-    SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C        Comment section for permuted index source lines was added
C        following the header.
C
C-    SPICELIB Version 1.0.0, 31-JAN-1990 (WLT)
C
C-&
 
C$ Index_Entries
C
C     latitudinal to rectangular coordinates
C
C-&
 
C$ Revisions
C
C-     Beta Version 1.0.1, 1-Feb-1989 (WLT)
C
C      Example section of header upgraded.
C
C-&
 
      DOUBLE PRECISION X
      DOUBLE PRECISION Y
      DOUBLE PRECISION Z
 
C
C     Convert to rectangular coordinates, storing the results in
C     temporary variables.
C
      X = RADIUS * DCOS(LONG) * DCOS(LAT)
      Y = RADIUS * DSIN(LONG) * DCOS(LAT)
      Z = RADIUS * DSIN(LAT)
 
C
C  Move the results to the output variables.
C
      RECTAN(1) = X
      RECTAN(2) = Y
      RECTAN(3) = Z

      RETURN
      END

C$Procedure      VEQU ( Vector equality, 3 dimensions )
 
      SUBROUTINE VEQU ( VIN, VOUT )
 
C$ Abstract
C
C      Make one double precision 3-dimensional vector equal to
C      another.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C      None.
C
C$ Keywords
C
C      ASSIGNMENT,  VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION  VIN   ( 3 )
      DOUBLE PRECISION  VOUT  ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       VIN       I   3-dimensional double precision vector.
C       VOUT      O   3-dimensional double precision vector set equal
C                     to VIN.
C
C$ Detailed_Input
C
C      VIN      This may be ANY 3-dimensional double precision vector.
C
C$ Detailed_Output
C
C      VOUT    This 3-dimensional double precision vector is set equal
C              to VIN.
C
C$ Parameters
C
C      None.
C
C$ Particulars
C
C      VEQU simply sets each component of VOUT in turn equal to VIN.  No
C      error checking is performed because none is needed.
C
C$ Examples
C
C     Let  STATE be a state vector. The angular momentum vector is
C     determined by the cross product of the position vector and the
C     velocity vector.
C
C      CALL VEQU ( STATE(1), R )
C      CALL VEQU ( STATE(4), V )
C
C      CALL VCRSS ( R, V, H )
C
C
C$ Restrictions
C
C      None.
C
C$ Exceptions
C
C     Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-    SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C        Comment section for permuted index source lines was added
C        following the header.
C
C-    SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     assign a 3-dimensional vector to another
C
C-&
 
      VOUT(1) = VIN(1)
      VOUT(2) = VIN(2)
      VOUT(3) = VIN(3)
C
      RETURN
      END

C$Procedure      VSCL ( Vector scaling, 3 dimensions )
 
      SUBROUTINE VSCL ( S, V1, VOUT )
 
C$ Abstract
C
C     Multiply a scalar and a 3-dimensional double precision vector.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION   S
      DOUBLE PRECISION   V1   ( 3 )
      DOUBLE PRECISION   VOUT ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       S         I     Scalar to multiply a vector.
C       V1        I     Vector to be multiplied.
C       VOUT      O     Product vector, S*V1. VOUT can overwrite V1.
C
C$ Detailed_Input
C
C      S    This is a double precision scalar used to multiply the
C            vector V1.
C
C      V1   This is a 3-dimensional, double precision vector which is
C           to be scaled by S.
C
C$ Detailed_Output
C
C      VOUT   This is a 3-dimensional, double precision vector which
C             is the scalar multiple of V1.  VOUT = S*V1.
C
C$ Parameters
C
C     None.
C
C$ Particulars
C
C      VSCL multiplies each component of V1 by S to form the respective
C      components of VOUT.  No error checking is performed.
C
C$ Examples
C
C      The following table shows the output VOUT as a function of the
C      the inputs V1, and S from the subroutine VSCL.
C
C      V1                   S         VOUT
C      -------------------------------------------------------
C      (1D0, -2D0, 0D0)   -1D0       (-1D0, 2D0, 0D0)
C      (0D0, 0D0, 0D0)     5D0       (0D0, 0D0, 0D0)
C
C$ Restrictions
C
C      The user is responsible for insuring that no floating point
C      overflow occurs from multiplying S by any component of V1.
C      No error recovery or reporting scheme is incorporated in this
C      subroutine.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     3-dimensional vector scaling
C
C-&
 
      VOUT(1) = S * V1(1)
      VOUT(2) = S * V1(2)
      VOUT(3) = S * V1(3)
 
      RETURN
      END

C$Procedure      VSUB ( Vector subtraction, 3 dimensions )
 
      SUBROUTINE VSUB ( V1, V2, VOUT )
 
C$ Abstract
C
C      Compute the difference between two 3-dimensional, double
C      precision vectors.
C
C$ Copyright
C
C     Copyright (1995), California Institute of Technology.
C     U.S. Government sponsorship acknowledged.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION  V1   ( 3 )
      DOUBLE PRECISION  V2   ( 3 )
      DOUBLE PRECISION  VOUT ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       V1        I     First vector (minuend).
C       V2        I     Second vector (subtrahend).
C       VOUT      O     Difference vector, V1 - V2. VOUT can overwrite
C                        either V1 or V2.
C
C$ Detailed_Input
C
C      V1    This can be any 3-dimensional, double precision vector.
C
C      V2    Ditto.
C
C$ Detailed_Output
C
C      VOUT   This is a 3-dimensional, double precision vector which
C             represents the vector difference, V1 - V2.
C
C$ Parameters
C
C     None.
C
C$ Particulars
C
C      This routine simply performs subtraction between components of V1
C      and V2.  No checking is performed to determine whether floating
C      point overflow has occurred.
C
C$ Examples
C
C      The following table shows the output VOUT as a function of the
C      the input V1 and V2 from the subroutine VSUB.
C
C      V1                  V2              ---> VOUT
C      --------------      --------------       --------------
C      ( 1.0, 2.0, 3.0)    ( 4.0,  5.0, 6.0)    (-3.0, -3.0, -3.0)
C      (1D-7, 1D23,0.0)    (1D24, 1D23, 0.0)    (-1D24, 0.0,  0.0)
C
C$ Restrictions
C
C      The user is required to determine that the magnitude each
C      component of the vectors is within the appropriate range so as
C      not to cause floating point overflow.  No error recovery or
C      reporting scheme is incorporated in this subroutine.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-     SPICELIB Version 1.0.2, 07-NOV-2003 (EDW)
C
C         Corrected a mistake in the second example's value
C         for VOUT, i.e. replaced (1D24, 2D23, 0.0) with
C         (-1D24, 0.0, 0.0).
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     3-dimensional vector subtraction
C
C-&
 
      VOUT(1) = V1(1) - V2(1)
      VOUT(2) = V1(2) - V2(2)
      VOUT(3) = V1(3) - V2(3)
 
      RETURN
      END


C$Procedure      VADD ( Vector addition, 3 dimensional )
 
      SUBROUTINE VADD ( V1, V2, VOUT )
 
C$ Abstract
C
C      Add two 3 dimensional vectors.
C
C$ Disclaimer
C
C     THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE
C     CALIFORNIA INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S.
C     GOVERNMENT CONTRACT WITH THE NATIONAL AERONAUTICS AND SPACE
C     ADMINISTRATION (NASA). THE SOFTWARE IS TECHNOLOGY AND SOFTWARE
C     PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS AND IS PROVIDED "AS-IS"
C     TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND, INCLUDING ANY
C     WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR A
C     PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC
C     SECTIONS 2312-2313) OR FOR ANY PURPOSE WHATSOEVER, FOR THE
C     SOFTWARE AND RELATED MATERIALS, HOWEVER USED.
C
C     IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA
C     BE LIABLE FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT
C     LIMITED TO, INCIDENTAL OR CONSEQUENTIAL DAMAGES OF ANY KIND,
C     INCLUDING ECONOMIC DAMAGE OR INJURY TO PROPERTY AND LOST PROFITS,
C     REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE ADVISED, HAVE
C     REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY.
C
C     RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF
C     THE SOFTWARE AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY
C     CALTECH AND NASA FOR ALL THIRD-PARTY CLAIMS RESULTING FROM THE
C     ACTIONS OF RECIPIENT IN THE USE OF THE SOFTWARE.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION   V1   ( 3 )
      DOUBLE PRECISION   V2   ( 3 )
      DOUBLE PRECISION   VOUT ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       V1        I     First vector to be added.
C       V2        I     Second vector to be added.
C       VOUT      O     Sum vector, V1 + V2.
C                       VOUT can overwrite either V1 or V2.
C
C$ Detailed_Input
C
C      V1      This may be any 3-element vector.
C
C      V2      Likewise.
C
C$ Detailed_Output
C
C      VOUT   This is vector sum of V1 and V2. VOUT may overwrite either
C             V1 or V2.
C
C$ Parameters
C
C     None.
C
C$ Particulars
C
C      This routine simply performs addition between components of V1
C      and V2.  No checking is performed to determine whether floating
C      point overflow has occurred.
C
C$ Examples
C
C      The following table shows the output VOUT as a function of the
C      the input V1 and V2 from the subroutine VADD.
C
C      V1                  V2              ---> VOUT
C      --------------      --------------       --------------
C      (1.0, 2.0, 3.0)     (4.0, 5.0, 6.0)      (5.0, 7.0, 9.0)
C      (1D-7,1D23,0)       (1D24, 1D23, 0.0)    (1D24,2D23,0.0)
C
C$ Restrictions
C
C      The user is required to determine that the magnitude each
C      component of the vectors is within the appropriate range so as
C      not to cause floating point overflow.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     3-dimensional vector addition
C
C-&
 
      VOUT(1) = V1(1) + V2(1)
      VOUT(2) = V1(2) + V2(2)
      VOUT(3) = V1(3) + V2(3)
C
      RETURN
      END

C$Procedure      VCRSS ( Vector cross product, 3 dimensions )
 
      SUBROUTINE VCRSS ( V1, V2, VOUT )
 
C$ Abstract
C
C      Compute the cross product of two 3-dimensional vectors.
C
C$ Disclaimer
C
C     THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE
C     CALIFORNIA INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S.
C     GOVERNMENT CONTRACT WITH THE NATIONAL AERONAUTICS AND SPACE
C     ADMINISTRATION (NASA). THE SOFTWARE IS TECHNOLOGY AND SOFTWARE
C     PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS AND IS PROVIDED "AS-IS"
C     TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND, INCLUDING ANY
C     WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR A
C     PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC
C     SECTIONS 2312-2313) OR FOR ANY PURPOSE WHATSOEVER, FOR THE
C     SOFTWARE AND RELATED MATERIALS, HOWEVER USED.
C
C     IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA
C     BE LIABLE FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT
C     LIMITED TO, INCIDENTAL OR CONSEQUENTIAL DAMAGES OF ANY KIND,
C     INCLUDING ECONOMIC DAMAGE OR INJURY TO PROPERTY AND LOST PROFITS,
C     REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE ADVISED, HAVE
C     REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY.
C
C     RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF
C     THE SOFTWARE AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY
C     CALTECH AND NASA FOR ALL THIRD-PARTY CLAIMS RESULTING FROM THE
C     ACTIONS OF RECIPIENT IN THE USE OF THE SOFTWARE.
C
C$ Required_Reading
C
C     None.
C
C$ Keywords
C
C      VECTOR
C
C$ Declarations
 
      DOUBLE PRECISION    V1   ( 3 )
      DOUBLE PRECISION    V2   ( 3 )
      DOUBLE PRECISION    VOUT ( 3 )
 
C$ Brief_I/O
C
C      VARIABLE  I/O  DESCRIPTION
C      --------  ---  --------------------------------------------------
C       V1        I     Left hand vector for cross product.
C       V2        I     Right hand vector for cross product.
C       VOUT      O     Cross product V1xV2.
C                       VOUT can overwrite either V1 or V2.
C
C$ Detailed_Input
C
C      V1      This may be any 3-dimensional vector.  Typically, this
C              might represent the (possibly unit) vector to a planet,
C              sun, or a star which defines the orientation of axes of
C              some coordinate system.
C
C      V2      Ditto.
C
C$ Detailed_Output
C
C      VOUT    This variable represents the cross product of V1 and V2.
C              VOUT may overwrite V1 or V2.
C
C$ Parameters
C
C     None.
C
C$ Particulars
C
C      VCRSS calculates the three dimensional cross product of two
C      vectors according to the definition.  The cross product is stored
C      in a buffer vector until the calculation is complete.  Thus VOUT
C      may overwrite V1 or V2 without interfering with intermediate
C      computations.
C
C      If V1 and V2 are large in magnitude (taken together, their
C      magnitude surpasses the limit allow by the computer) then it may
C      be possible to generate a floating point overflow from an
C      intermediate computation even though the actual cross product
C      may be well within the range of double precision numbers.
C      VCRSS does NOT check the magnitude of V1 or V2 to insure that
C      overflow will not occur.
C
C$ Examples
C
C      V1                  V2                  VOUT (=V1XV2)
C      -----------------------------------------------------------------
C      (0, 1, 0)           (1, 0, 0)           (0, 0, -1)
C      (5, 5, 5)           (-1, -1, -1)        (0, 0, 0)
C
C$ Restrictions
C
C      No checking of V1 or V2 is done to prevent floating point
C      overflow. The user is required to determine that the magnitude
C      of each component of the vectors is within an appropriate range
C      so as not to cause floating point overflow. In almost every case
C      there will be no problem and no checking actually needs to be
C      done.
C
C$ Exceptions
C
C      Error free.
C
C$ Files
C
C      None.
C
C$ Author_and_Institution
C
C      W.M. Owen       (JPL)
C
C$ Literature_References
C
C      None.
C
C$ Version
C
C-     SPICELIB Version 1.0.1, 10-MAR-1992 (WLT)
C
C         Comment section for permuted index source lines was added
C         following the header.
C
C-     SPICELIB Version 1.0.0, 31-JAN-1990 (WMO)
C
C-&
 
C$ Index_Entries
C
C     vector cross product
C
C-&
 
      DOUBLE PRECISION VTEMP(3)
C
C  Calculate the cross product of V1 and V2, store in VTEMP
C
      VTEMP(1) = V1(2)*V2(3) - V1(3)*V2(2)
      VTEMP(2) = V1(3)*V2(1) - V1(1)*V2(3)
      VTEMP(3) = V1(1)*V2(2) - V1(2)*V2(1)
C
C  Now move the result into VOUT
C
      VOUT(1) = VTEMP(1)
      VOUT(2) = VTEMP(2)
      VOUT(3) = VTEMP(3)
C
      RETURN
      END
