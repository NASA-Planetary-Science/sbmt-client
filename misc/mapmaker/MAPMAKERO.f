C     g77  SOURCE/MAPMAKERO.f  SOURCE/SPICELITE.f -o EXECUTABLES/MAPMAKERO.e
C     ftn  SOURCE/MAPMAKER.f  SOURCE/SPICELITE.f -o EXECUTABLES/MAPMAKER.e
C     Objective of this program is to make maps on the surface of Eros 

      IMPLICIT NONE
      
      INTEGER               NTMP
      PARAMETER            (NTMP=513)

      DOUBLE PRECISION      VDOT
      DOUBLE PRECISION      VNORM
      DOUBLE PRECISION      RPD
      DOUBLE PRECISION      SPD
      DOUBLE PRECISION      S0, S1
      DOUBLE PRECISION      V(3)
      DOUBLE PRECISION      UX(3)
      DOUBLE PRECISION      UY(3)
      DOUBLE PRECISION      UZ(3)
      DOUBLE PRECISION      V0(3)
      DOUBLE PRECISION      CX(3)
      DOUBLE PRECISION      CY(3)
      DOUBLE PRECISION      CZ(3)
      DOUBLE PRECISION      V1(3)
      DOUBLE PRECISION      W(3)
      DOUBLE PRECISION      W1(3)
      DOUBLE PRECISION      W2(3)
      DOUBLE PRECISION      WX(3)
      DOUBLE PRECISION      WY(3)
      DOUBLE PRECISION      WZ(3)
      DOUBLE PRECISION      AX(0:3)
      DOUBLE PRECISION      AY(0:3)
      DOUBLE PRECISION      AZ(0:3)
      DOUBLE PRECISION      D1(3)
      DOUBLE PRECISION      D2(3)
      DOUBLE PRECISION      DC(3)
      DOUBLE PRECISION      NORM(3)
      DOUBLE PRECISION      Z1
      DOUBLE PRECISION      Z2
      DOUBLE PRECISION      Z3
      DOUBLE PRECISION      Z4
      DOUBLE PRECISION      WT
      DOUBLE PRECISION      RANN
      DOUBLE PRECISION      HSCALE      
      DOUBLE PRECISION      ETA      
      DOUBLE PRECISION      MMFL      
      DOUBLE PRECISION      CTR(2)      
      DOUBLE PRECISION      KMAT(2,3)
      DOUBLE PRECISION      IMGPL(2)
      DOUBLE PRECISION      D(4)
      DOUBLE PRECISION      HMIN, HMAX
      DOUBLE PRECISION      GMIN, GMAX
      DOUBLE PRECISION      LMIN, LMAX
      DOUBLE PRECISION      ZMIN, ZMAX
      DOUBLE PRECISION      G1, G2, G3

      REAL*4                HT(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                VX(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                VY(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                VZ(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                GD(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                LP(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                NH(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                ZH(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                AL(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                CUBE(-NTMP:NTMP,-NTMP:NTMP,6)

      INTEGER               I, I0, I1
      INTEGER               J, J0, J1
      INTEGER               IMIN
      INTEGER               IMAX
      INTEGER               JMIN
      INTEGER               JMAX
      INTEGER               K, K0, K1, K2
      INTEGER               NREC
      INTEGER               F
      INTEGER               L
      INTEGER               Q
      INTEGER               Q0, Q1
      INTEGER               NPX
      INTEGER               NLN
      INTEGER               SLEN

      CHARACTER*1           ANS
      CHARACTER*1           BITS
      CHARACTER*6           NAME
      CHARACTER*12          PICNM
      CHARACTER*40          MAPNM
      CHARACTER*40          BIGMAP
      CHARACTER*60          LINE
      CHARACTER*72          BLINE
      CHARACTER*72          INFILE
      CHARACTER*72          LMRKFILE
      CHARACTER*72          TMPFILE
      CHARACTER*72          OUTFILE
      character*4000        CLINE

      LOGICAL               EX
      LOGICAL               USE
      LOGICAL               HUSE(-NTMP:NTMP,-NTMP:NTMP)
      LOGICAL               ZUSE(-NTMP:NTMP,-NTMP:NTMP)
      LOGICAL               GUSE(-NTMP:NTMP,-NTMP:NTMP)
      LOGICAL               LUSE(-NTMP:NTMP,-NTMP:NTMP)

      WRITE(6,*) 'Input a NAME for your map (up to 40 characters)'
      read(5,fmt='(a40)') BIGMAP
      WRITE(6,*) 'Input HALF SIZE (px) and HORIZONTAL SCALE (m/px)'
      read(5,*) Q0, S0
      S0=S0/1000
      WRITE(6,*) 'Choose a CENTER OPTION for your map'
      WRITE(6,*) '   P.  Position in a sample image in IMAGES'
      WRITE(6,*) '   L.  Latitude and West Longitude'
      WRITE(6,*) '   M.  Location on another map'
      READ(5,FMT='(A1)') ANS
      IF ((ANS.EQ.'P').OR.(ANS.EQ.'p')) THEN
        WRITE(6,*) 'Input picture number (1 - 10)'
        READ(5,*) K
        PICNM='geometry    '
        WRITE(PICNM(9:10),FMT='(I2)') K
        IF(PICNM(9:9).EQ.' ') PICNM(9:9)='0'
        write(6,*) 'input patch center (imgpx,imgln)'
        read(5,*) IMGPL(1), IMGPL(2)  
        CALL IMGPL2V(PICNM,IMGPL,USE,V)
        CALL VHAT(V,UZ)
        GO TO 150
      ENDIF
      IF ((ANS.EQ.'L').OR.(ANS.EQ.'l')) THEN
        write(6,*) 'Input ltd, wlng (deg)'
        read(5,*) Z1, Z2
        CALL LATREC(1.d0,-Z2*RPD(),Z1*RPD(), UZ)
        CALL U2V(UZ,V)
        GO TO 150
      ENDIF
      IF ((ANS.EQ.'M').OR.(ANS.EQ.'m')) THEN
        write(6,*) 'input map name'
        read(5,fmt='(a40)') MAPNM
        I=SLEN(NAME)
        LMRKFILE='MAPFILES/'//MAPNM(1:I)//'.MAP'
        INQUIRE(FILE=LMRKFILE,EXIST=EX)
        IF(.NOT.EX) THEN
          LMRKFILE='DATA/MAPFILES/'//MAPNM(1:I)//'.MAP'
        ENDIF
        INQUIRE(FILE=LMRKFILE,EXIST=EX)
        IF(.NOT.EX) THEN
          WRITE(6,*) 'MAPFILE does not exist'
          STOP
        ENDIF
        CALL READ_MAP(LMRKFILE,NTMP,Q1,S1,V,UX,UY,UZ,HT,AL)
        do i=-Q1,Q1
        do j=-Q1,Q1
          if(al(i,j).le.(0.005)) ht(i,j)=0
        enddo
        enddo
        write(6,*) 'Input patch center (map p/l)'
        read(5,*) z1, z2
        Z1=Z1-Q1
        Z2=Z2-Q1
        Z3=0
        IF((ABS(Z1).LE.Q1).AND.(ABS(Z2).LE.Q1)) THEN
          Z3=HT(NINT(Z1),NINT(Z2))
        ENDIF
        V(1)=V(1)+S1*(Z1*UY(1)+Z2*UX(1)+Z3*UZ(1))
        V(2)=V(2)+S1*(Z1*UY(2)+Z2*UX(2)+Z3*UZ(2))
        V(3)=V(3)+S1*(Z1*UY(3)+Z2*UX(3)+Z3*UZ(3))
        GO TO 150
      ENDIF
      STOP

150   CONTINUE

      q1=49
      s1=q0*s0/q1
      infile='DATA/SHAPEFILES/SHAPE.TXT'
      CALL ORIENT(UX,UY,UZ)
      CALL get_heights(NTMP,q1,s1,ux,uy,uz,v,infile,huse,ht)
      z1=ht(0,0)
      do i=-q1,q1
      do j=-q1,q1
        if(huse(i,j)) ht(i,j)=ht(i,j)-z1
      enddo
      enddo
      do k=1,3
        V(k)=V(k)+Z1*S1*Uz(k)
      enddo
      call patch_coords(q1,huse,ht,ux,uy,uz)
      CALL ORIENT(UX,UY,UZ)
      CALL get_heights(ntmp,q1,s1,ux,uy,uz,v,infile,huse,ht)
      z1=ht(0,0)
      do k=1,3
        V(k)=V(k)+Z1*S1*Uz(k)
      enddo

      DO I=-Q0-1,Q0+1
      DO J=-Q0-1,Q0+1
        ZH(I,J)=0
        NH(I,J)=0
      ENDDO
      ENDDO

      OPEN(UNIT=66,FILE='DATA/TILEFILE.TXT',STATUS='OLD')
100     CONTINUE
        READ(66,FMT='(A60)') LINE
        IF(LINE(1:3).EQ.'END') GO TO 110
        NAME=LINE(1:6)
        READ(LINE(7:60),*) Q1, S1
        READ(66,*) (V1(L), L=1,3)
        READ(66,*) (WX(L), L=1,3)
        READ(66,*) (WY(L), L=1,3)
        READ(66,*) (WZ(L), L=1,3)
        IF(VDOT(UZ,WZ).LT.(0.50)) GO TO 100
        CALL VSUB(V,V1,W)
        IF(ABS(VDOT(W,UX)).GT.2*(S0*Q0+S1*Q1)) GO TO 100
        IF(ABS(VDOT(W,UY)).GT.2*(S0*Q0+S1*Q1)) GO TO 100
        IF(ABS(VDOT(W,UZ)).GT.2*(S0*Q0+S1*Q1)) GO TO 100
        LMRKFILE='DATA/MAPFILES/'//NAME//'.MAP'
        INQUIRE(FILE=LMRKFILE, EXIST=EX)
        IF(.NOT.EX) GO TO 100
        CALL READ_MAP(LMRKFILE,NTMP,Q1,S1,V1,WX,WY,WZ,HT,AL)
        DO i0=-Q1,Q1-1
        DO j0=-Q1,Q1-1
          K=-1
          IMAX=-Q0-1
          IMIN= Q0+1
          JMAX=-Q0-1
          JMIN= Q0+1
          do j1=0,1
          do i1=0,1
            K=K+1
            I=I0+I1
            J=J0+J1
            IF(AL(I,J).LT.(0.005)) GO TO 20
            w(1)=V1(1)-V(1)
            w(2)=V1(2)-V(2)
            w(3)=V1(3)-V(3)
            w(1)=w(1)+S1*(j*wx(1)+i*wy(1)+HT(I,J)*wz(1))
            w(2)=w(2)+S1*(j*wx(2)+i*wy(2)+HT(I,J)*wz(2))
            w(3)=w(3)+S1*(j*wx(3)+i*wy(3)+HT(I,J)*wz(3))
            AX(K)=VDOT(w,ux)/S0
            AY(K)=VDOT(w,uy)/S0
            AZ(K)=VDOT(w,uz)/S0
            IMAX=MAX(IMAX,NINT(AY(K)-0.5))
            IMIN=MIN(IMIN,NINT(AY(K)+0.5))
            JMAX=MAX(JMAX,NINT(AX(K)-0.5))
            JMIN=MIN(JMIN,NINT(AX(K)+0.5))
          enddo
          enddo
          IF((IMAX.LT.-Q0).OR.(IMIN.GT.Q0).OR.
     .       (JMAX.LT.-Q0).OR.(JMIN.GT.Q0)) GO TO 20
          AX(3)=AX(0)-AX(1)-AX(2)+AX(3)
          AX(1)=AX(1)-AX(0)
          AX(2)=AX(2)-AX(0)
          AY(3)=AY(0)-AY(1)-AY(2)+AY(3)
          AY(1)=AY(1)-AY(0)
          AY(2)=AY(2)-AY(0)
          AZ(3)=AZ(0)-AZ(1)-AZ(2)+AZ(3)
          AZ(1)=AZ(1)-AZ(0)
          AZ(2)=AZ(2)-AZ(0)
          IMIN=MAX(IMIN,-Q0)
          IMAX=MIN(IMAX, Q0)
          JMIN=MAX(JMIN,-Q0)
          JMAX=MIN(JMAX, Q0)
          DO I=IMIN,IMAX
          DO J=JMIN,JMAX
            Z1=AX(2)*AY(3)-AX(3)*AY(2)
            Z2=AX(2)*AY(1)-AX(1)*AY(2)+AX(0)*AY(3)-AX(3)*AY(0)
     .        +AX(3)*I-AY(3)*J      
            Z3=AX(0)*AY(1)-AX(1)*AY(0)+AX(1)*I-AY(1)*J
            IF(Z2**2.LE.4*Z1*Z3) GO TO 30
            IF(ABS(Z1).GT.1.d-10) THEN
              Z2=Z2*(SQRT(1.D0-4*Z1*Z3/Z2**2)-1.D0)/(2*Z1)
            ELSE 
              Z2=-Z3/Z2-Z1*Z3**2/Z2**3
            ENDIF
            Z1=(I-AY(0)-AY(2)*Z2)/(AY(1)+AY(3)*Z2)
            z3=((i0+z1)/q1)**4
            z4=((j0+z2)/q1)**4
            wt=(1-z3)*(1-z4)/((1+z3)*(1+z4)*s1**2)
            ZH(I,J)=ZH(I,J)+(AZ(0)+AZ(1)*Z1+AZ(2)*Z2+AZ(3)*Z1*Z2)*WT 
            NH(I,J)=NH(I,J)+WT
30          CONTINUE
          ENDDO
          ENDDO          
20        CONTINUE
        ENDDO
        ENDDO
        GO TO 100
110     CONTINUE
      CLOSE(UNIT=66)

      DO I=-Q0-1,Q0+1
      DO J=-Q0-1,Q0+1
        HT(I,J)=0.
        HUSE(I,J)=.FALSE.
      ENDDO
      ENDDO
      DO I=-Q0,Q0
      DO J=-Q0,Q0
        IF(NH(I,J).GT.0) THEN
          HT(I,J)=ZH(I,J)/NH(I,J)
          HUSE(I,J)=.TRUE.
        ENDIF
      ENDDO
      ENDDO

      I=SLEN(BIGMAP)
      LMRKFILE='MAPFILES/'//BIGMAP(1:I)//'.MAP'
      CALL WRITE_MAP(LMRKFILE,BIGMAP(1:6),NTMP,Q0,S0,
     .                     V,UX,UY,UZ,HT,HUSE)

C Compute X,Y,Z
      do j=-q0,q0
      do i=-q0,q0
C           VX(i,j)=V(1)+S0*(i*UY(1)+j*UX(1)+HT(i,j)*UZ(1))
           cube(i,j,4)=V(1)+S0*(i*UY(1)+j*UX(1)+HT(i,j)*UZ(1))
C           VY(i,j)=V(2)+S0*(i*UY(2)+j*UX(2)+HT(i,j)*UZ(2))
           cube(i,j,5)=V(2)+S0*(i*UY(2)+j*UX(2)+HT(i,j)*UZ(2))
C           VZ(i,j)=V(3)+S0*(i*UY(3)+j*UX(3)+HT(i,j)*UZ(3))
           cube(i,j,6)=V(3)+S0*(i*UY(3)+j*UX(3)+HT(i,j)*UZ(3))
      enddo
      enddo


      infile='DATA/SHAPEFILES/GEOID.TXT'
      CALL get_heights(NTMP,q0,s0,ux,uy,uz,v,infile,zuse,zh)
      hmin= 1.D10
      hmax=-1.D10
      gmin= 1.D10
      gmax=-1.D10
      lmin= 1.D10
      lmax=-1.D10
      zmin= 1.D10
      zmax=-1.D10
      do j=-q0,q0
      do i=-q0,q0
        guse(i,j)=.false.
        luse(i,j)=.false.
        if(huse(i,j).and.zuse(i,j)) then
          zh(i,j)=(ht(i,j)-zh(i,j))*s0
          cube(i,j,1)=zh(i,j)
          zmin=MIN(zmin,ZH(I,J))
          zmax=MAX(zmax,ZH(I,J))
        else
          zh(i,j)=0
          cube(i,j,1)=zh(i,j)
          zuse(i,j)=.false.
        endif
      enddo
      enddo
        
      norm(1)=0.
      norm(2)=0.
      norm(3)=1.
      do j=-q0,q0-1
      do i=-q0,q0-1
       if (zuse(i,j)) then 
C diagonal from i,j to i+1,j+1
         d1(1)=s0
         d1(2)=s0
         d1(3)=zh(i+1,j+1)-zh(i,j) 
C diagonal from i+1,j to i,j+1
         d2(1)=-s0
         d2(2)=s0
         d2(3)=zh(i,j+1)-zh(i+1,j) 
         call ucrss(d1,d2,dc)
C         write(*,*) d1(1),d1(2),d1(3)
C         write(*,*) d2(1),d2(2),d2(3)
C         write(*,*) dc(1),dc(2),dc(3)
C         write(*,*) acos(vdot(dc,norm))
C         stop
         cube(i,j,3)=acos(vdot(dc,norm)/vnorm(dc))
       endif
      enddo
      enddo
      do j=-q0,q0
      do i=-q0,q0
        if(huse(i,j)) then
          ht(i,j)=ht(i,j)*s0
          cube(i,j,2)=ht(i,j)
          hmin=MIN(hmin,HT(I,J))
          hmax=MAX(hmax,HT(I,J))
        endif
      enddo
      enddo
      do j=-q0,q0
      do i=-q0,q0
        if(huse(i+1,j).and.huse(i-1,j)) then
          gd(i,j)=(ht(i+1,j)-ht(i-1,j))/2
          gd(i,j)=gd(i,j)/s0
C          cube(i,j,3)=gd(i,j)
          guse(i,j)=.true.
          gmin=MIN(gmin,gd(i,j))
          gmax=MAX(gmax,gd(i,j))
        endif
      enddo
      enddo
      do j=-q0,q0
      do i=-q0,q0
        if(huse(i+1,j).and.huse(i-1,j).and.
     .     huse(i,j+1).and.huse(i,j-1).and.huse(i,j)) then
          lp(i,j)=-ht(i,j)+(ht(i+1,j)+ht(i-1,j)+ht(i,j+1)+ht(i,j-1))/4
          lp(i,j)=lp(i,j)/s0**2
          luse(i,j)=.true.
          lmin=MIN(lmin,lp(i,j))
          lmax=MAX(lmax,lp(i,j))
        endif
      enddo
      enddo

      I=SLEN(BIGMAP)
      OUTFILE='OUTPUT/'//BIGMAP(1:I)//'.cub'
      write(6,*) 'Writing ', OUTFILE
      open(unit=15, file=OUTFILE,form="unformatted", access='direct',
     .     recl=((2*NTMP+1)*(2*NTMP+1)*6*4))
      write(unit=15, rec=1) cube
      close(unit=15)
      OUTFILE='OUTPUT/'//BIGMAP(1:I)//'.lbl'
      write(6,*) 'Writing ', OUTFILE
      open(unit=15, file=OUTFILE)
      write (15,'(A31)')   'BITPIX = 16 / REAL or FLOAT DATA'
      write (15,'(A9)')    'NAXIS = 3'
      write (15,'(A9,I5)') 'NAXIS1 = ',2*NTMP+1
      write (15,'(A9,I5)') 'NAXIS2 = ',2*NTMP+1
      write (15,'(A9,I5)') 'NAXIS3 = ',6
      write (15,'(A14,I5,A15)') 'NAXIS1_LIVE = ',2*q0+1,
     .'/Size x of data'
      write (15,'(A14,I5,A15)') 'NAXIS2_LIVE = ',2*q0+1,
     .'/Size y of data'
      write (15,'(A11,I5)') 'NAXIS1_0 = ',NTMP-q0,'/Start x pixel'
      write (15,'(A11,I5)') 'NAXIS2_0 = ',NTMP-q0,'/Start y pixel'
      write (15,'(A7,f5.2)')'SCALE = ',S0*1000.,'/Scale in m'
      close (15)


C      OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_DTM.raw'
C      write(6,*) 'Writing ', OUTFILE
C      write(6,*) ht(10,10)
C      open(unit=15, file=OUTFILE,form="unformatted")
C      write(unit=15) ht
C      close(unit=15)
      
C      OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_DEM.raw'
C      write(6,*) 'Writing ', OUTFILE
C      open(unit=15, file=OUTFILE,form="unformatted")
C      write(unit=15) zh
C      close(unit=15)
      
C      OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_X.raw'
C      write(6,*) 'Writing ', OUTFILE
C      open(unit=15, file=OUTFILE,form="unformatted")
C      write(unit=15) VX
C      close(unit=15)
      
C      OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_Y.raw'
C      write(6,*) 'Writing ', OUTFILE
C      open(unit=15, file=OUTFILE,form="unformatted")
C      write(unit=15) VY
C      close(unit=15)

C      OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_Z.raw'
C      write(6,*) 'Writing ', OUTFILE
C      open(unit=15, file=OUTFILE,form="unformatted")
C      write(unit=15) VZ
C      close(unit=15)
      
      WRITE(6,*) 'Display Laplacian of heights? (y/n)'
      READ(5,FMT='(A1)') ANS
      IF(ANS.EQ.'y') THEN
        I=SLEN(BIGMAP)
        OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_LAP.pgm'
        bits='a'
        CALL DISPLAY(NTMP,Q0,S0,V,UX,UY,UZ,LP,LUSE,LMIN,LMAX,
     .               BITS,OUTFILE)
      ENDIF

      WRITE(6,*) 'Display gradient of heights? (y/n)'
      READ(5,FMT='(A1)') ANS
      IF(ANS.EQ.'y') THEN
        I=SLEN(BIGMAP)
        OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_GRD.pgm'
        bits='a'
        CALL DISPLAY(NTMP,Q0,S0,V,UX,UY,UZ,GD,GUSE,GMIN,GMAX,
     .               BITS,OUTFILE)
      ENDIF

      WRITE(6,*) 'Display plane relative DTM? (y/n)'
      READ(5,FMT='(A1)') ANS
      IF(ANS.EQ.'y') THEN
        write(6,*) 'a. 8  bit DTM'
        write(6,*) 'b. 16 bit DTM'
        read(5,fmt='(a1)') bits
        OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_DTM.pgm'
        CALL DISPLAY(NTMP,Q0,S0,V,UX,UY,UZ,HT,HUSE,HMIN,HMAX,
     .               BITS,OUTFILE)
      ENDIF

      WRITE(6,*) 'Display geoid relative DEM? (y/n)'
      READ(5,FMT='(A1)') ANS
      IF(ANS.EQ.'y') THEN
        write(6,*) 'a. 8  bit DTM'
        write(6,*) 'b. 16 bit DTM'
        read(5,fmt='(a1)') bits
        OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_DEM.pgm'
        CALL DISPLAY(NTMP,Q0,S0,V,UX,UY,UZ,ZH,ZUSE,ZMIN,ZMAX,
     .               BITS,OUTFILE)
      ENDIF

      WRITE(6,*) 'Display plane relative shaded relief? (y/n)'
      READ(5,FMT='(A1)') ANS
      IF(ANS.EQ.'y') THEN
        DO J=-Q0,Q0
        DO I=-Q0,Q0
          IF(HUSE(I,J)) THEN
            HT(I,J)=(HT(I,J)-hmin)/(hmax-hmin)
            NH(I,J)=(GD(I,J)-gmin)/(gmax-gmin)
          ELSE
            HT(I,J)=0
            NH(I,J)=0
          ENDIF
        ENDDO
        ENDDO

        
        tmpfile= 'view.dat' 
        open(unit=12, file=tmpfile, access='direct', 
     .       recl=6*Q0+3, status='UNKNOWN')
          do j=1,2*Q0+1
            do i=1,2*Q0+1
              z1=9*ht(i-Q0-1,j-Q0-1)
              if(z1.le.1) then
                g1=128*(1-z1)
                g2=0
                g3=255
                go to 12
              endif
              if(z1.le.3) then
                g1=0
                g2=255*(z1-1)/2
                g3=255
                go to 12
              endif
              if(z1.le.5) then
                g1=0
                g2=255
                g3=255*(5-z1)/2
                go to 12
              endif
              if(z1.le.7) then
                g1=255*(z1-5)/2
                g2=255
                g3=0
                go to 12
              endif
              if(z1.le.9) then
                g1=255
                g2=255*(9-z1)/2
                g3=0
                go to 12
              endif
              g1=0
              g2=0
              g3=0
12            continue
              cline(3*i-2:3*i-2)=char(nint(g1*nh(i-Q0-1,j-Q0-1)))
              cline(3*i-1:3*i-1)=char(nint(g2*nh(i-Q0-1,j-Q0-1)))
              cline(3*i:3*i)=char(nint(g3*nh(i-Q0-1,j-Q0-1)))
            enddo
            write(12,rec=j) cline(1:6*Q0+3)
          enddo
        CLOSE(UNIT=12)
        I=SLEN(BIGMAP)
        OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_CTM.ppm'
        J=2*Q0+1
        call raw2ppm(tmpfile,outfile,J,J)
        open(unit=12,file=tmpfile,status='unknown')
        close(unit=12,status='delete')
      ENDIF

      WRITE(6,*) 'Display geoid relative shaded relief? (y/n)'
      READ(5,FMT='(A1)') ANS
      IF(ANS.EQ.'y') THEN
        DO J=-Q0,Q0
        DO I=-Q0,Q0
          IF(ZUSE(I,J)) THEN
            ZH(I,J)=(ZH(I,J)-zmin)/(zmax-zmin)
            NH(I,J)=(GD(I,J)-gmin)/(gmax-gmin)
          ELSE
            ZH(I,J)=0
            NH(I,J)=0
          ENDIF
        ENDDO
        ENDDO
        tmpfile= 'view.dat' 
        open(unit=12, file=tmpfile, access='direct', 
     .       recl=6*Q0+3, status='UNKNOWN')
          do j=1,2*Q0+1
            do i=1,2*Q0+1
              z1=9*zh(i-Q0-1,j-Q0-1)
              if(z1.le.1) then
                g1=128*(1-z1)
                g2=0
                g3=255
                go to 14
              endif
              if(z1.le.3) then
                g1=0
                g2=255*(z1-1)/2
                g3=255
                go to 14
              endif
              if(z1.le.5) then
                g1=0
                g2=255
                g3=255*(5-z1)/2
                go to 14
              endif
              if(z1.le.7) then
                g1=255*(z1-5)/2
                g2=255
                g3=0
                go to 14
              endif
              if(z1.le.9) then
                g1=255
                g2=255*(9-z1)/2
                g3=0
                go to 14
              endif
              g1=0
              g2=0
              g3=0
14            continue
              cline(3*i-2:3*i-2)=char(nint(g1*nh(i-Q0-1,j-Q0-1)))
              cline(3*i-1:3*i-1)=char(nint(g2*nh(i-Q0-1,j-Q0-1)))
              cline(3*i:3*i)=char(nint(g3*nh(i-Q0-1,j-Q0-1)))
            enddo
            write(12,rec=j) cline(1:6*Q0+3)
          enddo
        CLOSE(UNIT=12)
        I=SLEN(BIGMAP)
        OUTFILE='OUTPUT/'//BIGMAP(1:I)//'_CEM.ppm'
        J=2*Q0+1
        call raw2ppm(tmpfile,outfile,J,J)
        open(unit=12,file=tmpfile,status='unknown')
        close(unit=12,status='delete')
      ENDIF

      STOP
      END

c   ................................................
      SUBROUTINE ORIENT(UX,UY,UZ)
c   ................................................

      IMPLICIT NONE

      DOUBLE PRECISION      UX(3)
      DOUBLE PRECISION      UY(3)
      DOUBLE PRECISION      UZ(3)

      IF(UZ(3).GT.(0.99)) THEN
        UZ(1)=0
        UZ(2)=0
        UZ(3)=1
        UY(1)=0
        UY(2)=1
        UY(3)=0
      ELSE
      IF(UZ(3).LT.(-0.99)) THEN
        UZ(1)=0
        UZ(2)=0
        UZ(3)=-1
        UY(1)=0
        UY(2)=1
        UY(3)=0
      ELSE
        UY(1)=-UZ(2)
        UY(2)=+UZ(1)                               
        UY(3)=0
        CALL VHAT(UY,UY)
      ENDIF
      ENDIF
      CALL UCRSS(UY,UZ,UX)

      RETURN
      END

c   ................................................
      SUBROUTINE READ_MAP(LMRKFILE,NTMP,QSZ,SCALE,V,UX,UY,UZ,HT,ALB)
c   ................................................

      IMPLICIT NONE
      
      INTEGER               NTMP
      INTEGER               QSZ
      INTEGER               I
      INTEGER               J
      INTEGER               K
      INTEGER               K0
      INTEGER               IX(24)
      INTEGER               JX(24)
      INTEGER               NREC

      DOUBLE PRECISION      SCALE
      DOUBLE PRECISION      HSCALE
      DOUBLE PRECISION      V(3)
      DOUBLE PRECISION      UX(3)
      DOUBLE PRECISION      UY(3)
      DOUBLE PRECISION      UZ(3)

      REAL*4                HT(-NTMP:NTMP,-NTMP:NTMP)
      REAL*4                ALB(-NTMP:NTMP,-NTMP:NTMP)

      CHARACTER*72          BLINE
      CHARACTER*72          LMRKFILE

      CHARACTER*1           CH1
      CHARACTER*2           CH2, CH2F
      CHARACTER*4           CH4, CH4F
      INTEGER*2             IX2
      REAL*4                RL4
      EQUIVALENCE          (IX2,CH2)
      EQUIVALENCE          (RL4,CH4)

      CHARACTER*2           C2
      INTEGER*2             I2
      EQUIVALENCE          (I2,C2)
      LOGICAL               LFLAG

      c2='69'
      LFLAG=.TRUE.
      if(i2.eq.13881) LFLAG=.FALSE.

      OPEN(UNIT=20, FILE=LMRKFILE, ACCESS='DIRECT',
     .     RECL=72, status='OLD')

        READ(20,REC=1) BLINE
        CH4f=BLINE(7:10)
        call flip(4,lflag,ch4f,ch4)
        SCALE=RL4
        QSZ=ICHAR(BLINE(11:11))
     .     +ICHAR(BLINE(12:12))*256
        DO K=1,3
          CH4f=BLINE(12+4*K:15+4*K)
          call flip(4,lflag,ch4f,ch4)
          V(K)=RL4
          CH4f=BLINE(24+4*K:27+4*K)
          call flip(4,lflag,ch4f,ch4)
          UX(K)=RL4
          CH4f=BLINE(36+4*K:39+4*K)
          call flip(4,lflag,ch4f,ch4)
          UY(K)=RL4
          CH4f=BLINE(48+4*K:51+4*K)
          call flip(4,lflag,ch4f,ch4)
          UZ(K)=RL4
        ENDDO
        CH4f=BLINE(64:67)
        call flip(4,lflag,ch4f,ch4)
        HSCALE=RL4

        DO I=-QSZ,QSZ
        DO J=-QSZ,QSZ
          ALB(I,J)=0
          HT(I,J)=0
        ENDDO
        ENDDO
        
        NREC=1        
        K=0
        DO J=-QSZ,QSZ
        DO I=-QSZ,QSZ
          K=K+1
          IX(K)=I
          JX(K)=J
          IF(K.EQ.24) THEN
            NREC=NREC+1
            READ(20,REC=NREC) BLINE
            DO K=1,24
              CH1=BLINE(3*K:3*K)
              IF(ICHAR(CH1).NE.0) THEN
                CH2f=BLINE(3*K-2:3*K-1)
                call flip(2,lflag,ch2f,ch2)
                HT(IX(K),JX(K))=HSCALE*IX2
                ALB(IX(K),JX(K))=.01*ICHAR(CH1)
              ENDIF
            ENDDO
            K=0
          ENDIF
        enddo
        enddo
        IF(K.NE.0) THEN
          K0=K
          NREC=NREC+1
          READ(20,REC=NREC) BLINE
          DO K=1,K0
            CH1=BLINE(3*K:3*K)
            CH2f=BLINE(3*K-2:3*K-1)
            call flip(2,lflag,ch2f,ch2)
            HT(IX(K),JX(K))=HSCALE*IX2
            ALB(IX(K),JX(K))=.01*ICHAR(CH1)
          ENDDO
        ENDIF

      CLOSE(UNIT=20)
      
      RETURN
      END     

c   ................................................
      SUBROUTINE WRITE_MAP(LMRKFILE,LMKNM,NTMP,QSZ,SCALE,
     .                     V,UX,UY,UZ,HT,HUSE)
c   ................................................

      IMPLICIT NONE
      
      INTEGER               NTMP
      INTEGER               QSZ
      INTEGER               I
      INTEGER               J
      INTEGER               K
      INTEGER               NREC

      DOUBLE PRECISION      SCALE
      DOUBLE PRECISION      HSCALE
      DOUBLE PRECISION      V(3)
      DOUBLE PRECISION      UX(3)
      DOUBLE PRECISION      UY(3)
      DOUBLE PRECISION      UZ(3)
      DOUBLE PRECISION      ETA
      DOUBLE PRECISION      Z1

      REAL*4                HT(-NTMP:NTMP,-NTMP:NTMP)

      CHARACTER*6           LMKNM
      CHARACTER*72          BLINE
      CHARACTER*72          LMRKFILE

      LOGICAL               HUSE(-NTMP:NTMP,-NTMP:NTMP)

      CHARACTER*1           CH1
      CHARACTER*2           CH2, CH2F
      CHARACTER*4           CH4, CH4F
      INTEGER*2             IX2
      REAL*4                RL4
      EQUIVALENCE          (IX2,CH2)
      EQUIVALENCE          (RL4,CH4)

      CHARACTER*2           C2
      INTEGER*2             I2
      EQUIVALENCE          (I2,C2)
      LOGICAL               LFLAG

      c2='69'
      LFLAG=.TRUE.
      if(i2.eq.13881) LFLAG=.FALSE.

      OPEN(UNIT=10,FILE=LMRKFILE,ACCESS='DIRECT',
     .     RECL=72,STATUS='UNKNOWN')
        DO K=1,72
          BLINE(K:K)=CHAR(0)
        ENDDO
        BLINE(1:6)=LMKNM
        RL4=SCALE
        call flip(4,lflag,ch4,ch4f)
        BLINE(7:10)=CH4f
        BLINE(11:11)=CHAR(QSZ-256*(qsz/256)) 
        BLINE(12:12)=CHAR(qsz/256)
        BLINE(13:13)=CHAR(0)
        BLINE(14:14)=CHAR(0)
        BLINE(15:15)=CHAR(0)
        DO K=1,3
          RL4=V(K)
          call flip(4,lflag,ch4,ch4f)
          BLINE(12+4*K:15+4*K)=CH4f
          RL4=UX(K)
          call flip(4,lflag,ch4,ch4f)
          BLINE(24+4*K:27+4*K)=CH4f
          RL4=UY(K)
          call flip(4,lflag,ch4,ch4f)
          BLINE(36+4*K:39+4*K)=CH4f
          RL4=UZ(K)
          call flip(4,lflag,ch4,ch4f)
          BLINE(48+4*K:51+4*K)=CH4f
        ENDDO
        z1=1.0
        do j=-QSZ,QSZ
        do i=-QSZ,QSZ
        if(huse(i,j)) then
          z1=max(z1,ABS(ht(i,j)))
        endif
        enddo
        enddo
        HSCALE=z1/30000
        RL4=HSCALE
        call flip(4,lflag,ch4,ch4f)
        BLINE(64:67)=CH4f
        NREC=1
        WRITE(10,REC=NREC) BLINE
        K=0
        do j=-QSZ,QSZ
        do i=-QSZ,QSZ
          if(huse(i,j)) then
            eta=1
          else
            HT(I,J)=0
            eta=0
          endif
          K=K+1
          IX2=NINT(HT(I,J)/HSCALE)
          CH1=CHAR(NINT(100*ETA))
          call flip(2,lflag,ch2,ch2f)
          BLINE(3*K-2:3*K-1)=CH2f
          BLINE(3*K:3*K)=CH1
          IF(K.EQ.24) THEN
            NREC=NREC+1
            write(10, REC=NREC) BLINE
            DO K=1,72
              BLINE(K:K)=CHAR(0)
            ENDDO
            K=0
          ENDIF
        enddo
        enddo
        IF(K.NE.0) THEN
          NREC=NREC+1
          write(10, REC=NREC) BLINE
        ENDIF
      CLOSE(UNIT=10)

      RETURN
      END     

c   ..................................................
      subroutine flip(n,lflag,ch1,ch2)
c   ..................................................

      integer*4        n, i
      character*(*)    ch1, ch2
      logical          lflag

      if(lflag) then
        do i=1,n
          ch2(i:i)=ch1(n-i+1:n-i+1)
        enddo
      else
        ch2=ch1
      endif
      
      return
      end
      
c  ...............................................................
      subroutine u2v(UZ,V)
c  ...............................................................

      IMPLICIT NONE
      
      real*8        w1, w2, a, b, c,
     .              x0, x1, x2, x3, y0, y1, y2, y3, z0, z1, z2, z3, eps,
     .              p0, p1, p2, p3, vec(3,0:512,0:512,6)

      integer*4     q, i, j, f
      
      DOUBLE PRECISION      VDOT
      DOUBLE PRECISION      VNORM
      DOUBLE PRECISION      V(3)
      DOUBLE PRECISION      UX(3)      
      DOUBLE PRECISION      UY(3)
      DOUBLE PRECISION      UZ(3)

      CHARACTER*72          INFILE
      
      infile='DATA/SHAPEFILES/SHAPE.TXT'
      call get_model(infile,q,vec)
      eps=1.d-5

      Z0=1
      DO I=1,3
        IF(ABS(UZ(I)).LT.Z0) THEN
          J=I
          Z0=ABS(UZ(I))
        ENDIF
        UX(I)=0
      ENDDO
      UX(J)=1      
      CALL UCRSS(UZ,UX,UY)
      CALL UCRSS(UY,UZ,UX)

      do f=1,6
      do i=0,q-1
      do j=0,q-1
        z0=VDOT(Uz,vec(1,i,j,f))/VNORM(vec(1,i,j,f))
        if(z0.lt.(1-10.d0/q**2)) go to 10
        x0=VDOT(Ux,vec(1,i,j,f))/VNORM(vec(1,i,j,f))
        y0=VDOT(Uy,vec(1,i,j,f))/VNORM(vec(1,i,j,f))
        x1=VDOT(Ux,vec(1,i+1,j,f))/VNORM(vec(1,i+1,j,f))
        y1=VDOT(Uy,vec(1,i+1,j,f))/VNORM(vec(1,i+1,j,f))
        x2=VDOT(Ux,vec(1,i,j+1,f))/VNORM(vec(1,i,j+1,f))
        y2=VDOT(Uy,vec(1,i,j+1,f))/VNORM(vec(1,i,j+1,f))
        x3=VDOT(Ux,vec(1,i+1,j+1,f))/VNORM(vec(1,i+1,j+1,f))
        y3=VDOT(Uy,vec(1,i+1,j+1,f))/VNORM(vec(1,i+1,j+1,f))
        p0=x0*y1-y0*x1
        p1=x1*y3-y1*x3
        p2=x3*y2-y3*x2
        p3=x2*y0-y2*x0
        if((p0.le.eps).and.(p1.le.eps).and.
     .     (p2.le.eps).and.(p3.le.eps)) then
          z0=VNORM(vec(1,i,j,f))
          z1=VNORM(vec(1,i+1,j,f))
          z2=VNORM(vec(1,i,j+1,f))
          z3=VNORM(vec(1,i+1,j+1,f))
          x3=x0-x1-x2+x3
          x1=x1-x0
          x2=x2-x0
          y3=y0-y1-y2+y3
          y1=y1-y0
          y2=y2-y0
          z3=z0-z1-z2+z3
          z1=z1-z0
          z2=z2-z0
          a=x1*y3-x3*y1
          b=x1*y2-x2*y1-y0*x3+x0*y3
          c=x0*y2-y0*x2
          if(b**2.gt.4*a*c) then
            if(abs(a).gt.(1.d-8)) then
              w1=(sqrt(1-4*a*c/b**2)-1)*b/(2*a)
            else
              w1=-c/b-a*c**2/b**3
            endif
            w2=-(x0+x1*w1)/(x2+x3*w1)
            Z0=z0+z1*w1+z2*w2+z3*w1*w2            
            CALL VSCL(Z0,UZ,V)
            RETURN
          endif
        endif
10      CONTINUE
      enddo
      enddo
      enddo

      return
      end

c  ...............................................................
      subroutine get_heights(ntmp,qsz,scale,ux,uy,uz,v0,infile, 
     .                       huse,ht)
c  ...............................................................

      integer*4     ntmp, qsz  

      real*8        ux(3), uy(3), uz(3), v0(3), scale, w1, w2, a, b, c,
     .              x0, x1, x2, x3, y0, y1, y2, y3, z0, z1, z2, z3, 
     .              vk(3), vec(3,0:512,0:512,6), epsilon

      DOUBLE PRECISION      VDOT

      real*4        ht(-ntmp:ntmp,-ntmp:ntmp), real4

      integer*4     q, i, j, f, i0, j0,
     .              imin, imax, jmin, jmax 
      
      logical       huse(-ntmp:ntmp,-ntmp:ntmp)

      character*72  infile

      epsilon=0.01
	        call get_model(infile,q,vec)

      do i0=-qsz,qsz
      do j0=-qsz,qsz
        ht(i0,j0)=-1.d10
        huse(i0,j0)=.false.
      enddo
      enddo
      
      do f=1,6
      do i=0,q-1
      do j=0,q-1
        vk(1)=vec(1,i,j,f)-v0(1)
        vk(2)=vec(2,i,j,f)-v0(2)
        vk(3)=vec(3,i,j,f)-v0(3)
        x0=VDOT(ux,vk)/scale
        y0=VDOT(uy,vk)/scale
        z0=VDOT(uz,vk)/scale
        vk(1)=vec(1,i+1,j,f)-v0(1)
        vk(2)=vec(2,i+1,j,f)-v0(2)
        vk(3)=vec(3,i+1,j,f)-v0(3)
        x1=VDOT(ux,vk)/scale
        y1=VDOT(uy,vk)/scale
        z1=VDOT(uz,vk)/scale
        vk(1)=vec(1,i,j+1,f)-v0(1)
        vk(2)=vec(2,i,j+1,f)-v0(2)
        vk(3)=vec(3,i,j+1,f)-v0(3)
        x2=VDOT(ux,vk)/scale
        y2=VDOT(uy,vk)/scale
        z2=VDOT(uz,vk)/scale
        vk(1)=vec(1,i+1,j+1,f)-v0(1)
        vk(2)=vec(2,i+1,j+1,f)-v0(2)
        vk(3)=vec(3,i+1,j+1,f)-v0(3)
        x3=VDOT(ux,vk)/scale
        y3=VDOT(uy,vk)/scale
        z3=VDOT(uz,vk)/scale

        jmax=min( qsz,nint(max(x0,x1,x2,x3))+1)
        jmin=max(-qsz,nint(min(x0,x1,x2,x3))-1)
        imax=min( qsz,nint(max(y0,y1,y2,y3))+1)
        imin=max(-qsz,nint(min(y0,y1,y2,y3))-1)

        x3=x0-x1-x2+x3
        x1=x1-x0
        x2=x2-x0
        y3=y0-y1-y2+y3
        y1=y1-y0
        y2=y2-y0
        z3=z0-z1-z2+z3
        z1=z1-z0
        z2=z2-z0
              
        do i0=imin,imax
        do j0=jmin,jmax
          a=x1*y3-x3*y1
          b=x1*y2-x2*y1+(i0-y0)*x3-(j0-x0)*y3
          c=(i0-y0)*x2-(j0-x0)*y2
          if(b**2.gt.4*a*c) then
            if(abs(a).gt.(1.d-8)) then
              w1=(sqrt(1-4*a*c/b**2)-1)*b/(2*a)
            else
              w1=-c/b-a*c**2/b**3
            endif
            w2=(j0-x0-x1*w1)/(x2+x3*w1)
            if((w1.ge.-epsilon).and.(w1.le.1+epsilon).and.
     .         (w2.ge.-epsilon).and.(w2.le.1+epsilon)) then
              real4=z0+z1*w1+z2*w2+z3*w1*w2
              ht(i0,j0)=max(ht(i0,j0),real4)
              huse(i0,j0)=.true.
            endif
          endif
        enddo
        enddo

      enddo
      enddo
      enddo

      do i=-qsz,qsz
      do j=-qsz,qsz
      if(.not.huse(i,j)) then
        ht(i,j)=0
      endif
      enddo
      enddo
                
      return
      end

c   ..................................................
      subroutine get_model(infile,q,vec)
c   ..................................................

      implicit none

      real*8           vec(3,0:512,0:512,6)
      integer*4        i, j, k, f, q
      character*72     infile
	  
      open(unit=49,file=infile,status='old')
        read(49,*) q
        do f=1,6
        do j = 0,q
        do i = 0,q
          read(49,*) (vec(k,i,j,f), k=1,3)
        enddo
        enddo
        enddo
      close(unit=49)
      
      return
      end

c  ..............................................
      subroutine raw2pgm(infile,outfile,npx,nln)
c  ..............................................

      implicit none
      
      integer*4             npx, nln, i, j, k
      character*10          line
      character*50          header
      character*72          infile, outfile
      character*30000000    dn

      
      header(1:2)='P5'
      header(3:3)=char(10)
      header(4:5)='#.'
      header(6:6)=char(10)
      k=7
      write(line,fmt='(i10)') npx
      do i=1,10
      if(line(i:i).ne.' ') then
        header(k:k+10-i) = line(i:10)
        k=k+11-i
        go to 10
      endif
      enddo
10    continue
      header(k:k)=' '
      k=k+1
      write(line,fmt='(i10)') nln
      do i=1,10
      if(line(i:i).ne.' ') then
        header(k:k+10-i) = line(i:10)
        k=k+11-i
        go to 20
      endif
      enddo
20    continue
      header(k:k)=char(10)
      k=k+1
      header(k:k+2)='255'
      k=k+3
      header(k:k)=char(10)
      
      open(unit=10, file=infile, recl=npx, access='direct', 
     .     status='old')
        dn(1:k)=header(1:k)
        do j=1,nln
          read(10,rec=j) dn(k+(j-1)*npx+1:k+j*npx)
        enddo
      close(unit=10)
      open(unit=20, file=outfile, recl=npx, access='direct', 
     .     status='unknown')
        do j=1,nln
          write(20,rec=j) dn(1+(j-1)*npx:j*npx)
        enddo
      close(unit=20)
      open(unit=20, file=outfile, recl=1, access='direct', 
     .     status='unknown')
        do j=npx*nln+1,npx*nln+k
          write(20,rec=j) dn(j:j)
        enddo
      close(unit=20)
      
      return
      end

c  ..............................................
      subroutine raw2ppm(infile,outfile,npx,nln)
c  ..............................................

      implicit none
      
      integer*4           npx, nln, k, i, j
      character*10        line
      character*50        header
      character*72        infile, outfile
      character*60000000  dn
      
      header(1:2)='P6'
      header(3:3)=char(10)
      header(4:5)='#.'
      header(6:6)=char(10)
      k=7
      write(line,fmt='(i10)') npx
      do i=1,10
      if(line(i:i).ne.' ') then
        header(k:k+10-i) = line(i:10)
        k=k+11-i
        go to 10
      endif
      enddo
10    continue
      header(k:k)=' '
      k=k+1
      write(line,fmt='(i10)') nln
      do i=1,10
      if(line(i:i).ne.' ') then
        header(k:k+10-i) = line(i:10)
        k=k+11-i
        go to 20
      endif
      enddo
20    continue
      header(k:k)=char(10)
      k=k+1
      header(k:k+2)='255'
      k=k+3
      header(k:k)=char(10)
      
      open(unit=10, file=infile, recl=3*npx, access='direct', 
     .     status='old')
        dn(1:k)=header(1:k)
        do j=1,nln
          read(10,rec=j) dn(k+3*(j-1)*npx+1:k+3*j*npx)
        enddo
      close(unit=10)
      open(unit=20, file=outfile, recl=3*npx, access='direct', 
     .     status='unknown')
        do j=1,nln
          write(20,rec=j) dn(1+3*(j-1)*npx:3*j*npx)
        enddo
      close(unit=20)
      open(unit=20, file=outfile, recl=1, access='direct', 
     .     status='unknown')
        do j=3*npx*nln+1,3*npx*nln+k
          write(20,rec=j) dn(j:j)
        enddo
      close(unit=20)

      return
      end
     
C   ..................................................
      FUNCTION SLEN(STRING)
C   ..................................................

      IMPLICIT NONE
      
      INTEGER*4        SLEN, I
      CHARACTER*(*)     STRING
      
      SLEN=LEN(STRING)
      DO I=1,SLEN
      IF(STRING(I:I).EQ.' ') THEN
        SLEN=I-1
        RETURN
      ENDIF
      ENDDO

      RETURN
      END            
      
c  ...............................................................
      SUBROUTINE IMGPL2V(PICNM,IMGPL,USE,V)
c  ...............................................................

      IMPLICIT NONE

      integer*4             I, SLEN
      
      DOUBLE PRECISION      V0(3)
      DOUBLE PRECISION      V(3)
      DOUBLE PRECISION      CX(3)      
      DOUBLE PRECISION      CY(3)
      DOUBLE PRECISION      CZ(3)
      DOUBLE PRECISION      IMGPL(2)
      DOUBLE PRECISION      MMFL
      DOUBLE PRECISION      KMAT(2,3)
      DOUBLE PRECISION      D(4)
      DOUBLE PRECISION      CTR(2)
      DOUBLE PRECISION      MM(2)

      CHARACTER*12          PICNM
      CHARACTER*72          PICTFILE
      character*80          LINE

      LOGICAL               EX
      LOGICAL               USE

      I=SLEN(PICNM)
      PICTFILE='DATA/SUMFILES/'//PICNM(1:I)//'.SUM'
      INQUIRE(FILE=PICTFILE, EXIST=EX)
      IF(.NOT.EX) THEN
        WRITE(6,*) PICTFILE
c        RETURN
      ENDIF
      OPEN(UNIT=10,FILE=PICTFILE,STATUS='OLD')
        READ(10,*)
        READ(10,*)
        READ(10,*)
        READ(10,*) MMFL, CTR(1), CTR(2)
        READ(10,*) (V0(I), I=1,3) 
        READ(10,*) (CX(I), I=1,3)
        READ(10,*) (CY(I), I=1,3)
        READ(10,*) (CZ(I), I=1,3)
        READ(10,*)! SZ
        READ(10,*)  KMAT(1,1), KMAT(1,2), KMAT(1,3),
     .              KMAT(2,1), KMAT(2,2), KMAT(2,3)
        READ(10,FMT='(A80)') LINE
        IF(LINE(64:73).EQ.'DISTORTION') THEN
          READ(LINE,*) (D(I), I=1,4)
        ELSE
          D(1)=0.D0
          D(2)=0.D0
          D(3)=0.D0
          D(4)=0.D0
        ENDIF
      CLOSE(UNIT=10)

      CALL PXMM(KMAT,CTR,D,IMGPL,MM)
      CALL MM2V(MM,MMFL,V0,CX,CY,CZ,USE,V)

      return
      end

C   ..................................................
      SUBROUTINE MMPX(KMAT,CTR,D,MM,PX)
C   ..................................................

      IMPLICIT NONE
          
      DOUBLE PRECISION      KMAT(2,3)
      DOUBLE PRECISION      CTR(2)
      DOUBLE PRECISION      MM(2)  
      DOUBLE PRECISION      PX(2)
      DOUBLE PRECISION      D(4)
      DOUBLE PRECISION      X
      DOUBLE PRECISION      Y
      DOUBLE PRECISION      Z
      DOUBLE PRECISION      RSQ
      INTEGER               K
      LOGICAL               USE
      
      X=MM(1)*(1.d0+D(1))
      Y=MM(2)*(1.d0+D(1))

      USE=.FALSE.
      DO K=2,4
        USE=USE.OR.(D(K).NE.0)
      ENDDO
      
      IF(USE) THEN
        RSQ=X**2+Y**2
        Z=1.d0+D(2)*RSQ+D(3)*Y+D(4)*X
        X=X*Z
        Y=Y*Z
      ENDIF
          
      PX(1)=CTR(1)+KMAT(1,1)*X+KMAT(1,2)*Y+KMAT(1,3)*X*Y
      PX(2)=CTR(2)+KMAT(2,1)*X+KMAT(2,2)*Y+KMAT(2,3)*X*Y
          
      RETURN
      END

C   ..................................................
      SUBROUTINE PXMM(KMAT,CTR,D,PX,MM)
C   ..................................................

      IMPLICIT NONE
          
      DOUBLE PRECISION      KMAT(2,3)
      DOUBLE PRECISION      CTR(2)
      DOUBLE PRECISION      PX0(2)
      DOUBLE PRECISION      MM(2)  
      DOUBLE PRECISION      PX(2)
      DOUBLE PRECISION      D(4)
      INTEGER               I
          
      MM(1)=0
      MM(2)=0
      PX0(1)=CTR(1)
      PX0(2)=CTR(2)
      
      DO I=1,20
        MM(1)=MM(1)+(PX(1)-PX0(1))/KMAT(1,1)
        MM(2)=MM(2)+(PX(2)-PX0(2))/KMAT(2,2)
        CALL MMPX(KMAT,CTR,D,MM,PX0)
        IF(((PX(1)-PX0(1))**2
     .    + (PX(2)-PX0(2))**2).LT.(1.d-8)) RETURN
      ENDDO

      RETURN
      END

c  ...............................................................
      SUBROUTINE MM2V(MM,MMFL,V0,CX,CY,CZ,USE,V)
c  ...............................................................

      IMPLICIT NONE

      real*8        w1, w2, a, b, c, d0,
     .              x(0:3), y(0:3), z(0:3), x0, y0,
     .              v0dotux, v0dotuy, v0dotuz, p0, p1, p2, p3,  
     .              vec(3,0:512,0:512,6)

      integer*4     q, i, j, f, l, n, SLEN, i0, j0, f0
      
      DOUBLE PRECISION      VDOT
      DOUBLE PRECISION      VNORM
      DOUBLE PRECISION      V0(3)
      DOUBLE PRECISION      V(3)
      DOUBLE PRECISION      CX(3)      
      DOUBLE PRECISION      CY(3)
      DOUBLE PRECISION      CZ(3)
      DOUBLE PRECISION      UX(3)      
      DOUBLE PRECISION      UY(3)
      DOUBLE PRECISION      UZ(3)
      DOUBLE PRECISION      IMGPL(2)
      DOUBLE PRECISION      MMFL
      DOUBLE PRECISION      KMAT(2,3)
      DOUBLE PRECISION      D(4)
      DOUBLE PRECISION      CTR(2)
      DOUBLE PRECISION      MM(2)
      DOUBLE PRECISION      mx2

      CHARACTER*12          PICNM
      CHARACTER*72          PICTFILE
      CHARACTER*72          INFILE

      LOGICAL               EX
      LOGICAL               USE

      INFILE='DATA/SHAPEFILES/SHAPE.TXT'
      call get_model(infile,q,vec)

      mx2=0
      do f=1,6
      do j=0,q-1
      do i=0,q-1
        CALL VSUB(VEC(1,I,J,F),VEC(1,I+1,J+1,F),V)
        mx2=MAX(mx2,VNORM(V))
        CALL VSUB(VEC(1,I+1,J,F),VEC(1,I,J+1,F),V)
        mx2=MAX(mx2,VNORM(V))
      enddo
      enddo
      enddo
      mx2=mx2**2

      DO I=1,3
        UZ(I)=MM(1)*CX(I)+MM(2)*CY(I)+MMFL*CZ(I)
      ENDDO
      CALL VHAT(UZ,UZ)
      CALL UCRSS(UZ,CX,UY)
      CALL UCRSS(UY,UZ,UX)

      v0dotux=VDOT(V0,UX)
      v0dotuy=VDOT(V0,UY)
      v0dotuz=VDOT(V0,UZ)

      d0=1.d10
      USE=.FALSE.

      do f=1,6
      do j=0,q-1
      do i=0,q-1
        call vequ(vec(1,i,j,f),v)
        x(0)=VDOT(ux,v)+v0dotux
        y(0)=VDOT(uy,v)+v0dotuy
        z(0)=VDOT(uz,v)+v0dotuz
        IF((x(0)**2+y(0)**2).gt.mx2) GO TO 10
        call vequ(vec(1,i+1,j,f),v)
        x(1)=VDOT(ux,v)+v0dotux
        y(1)=VDOT(uy,v)+v0dotuy
        z(1)=VDOT(uz,v)+v0dotuz
        IF((x(1)**2+y(1)**2).gt.mx2) GO TO 10
        call vequ(vec(1,i,j+1,f),v)
        x(2)=VDOT(ux,v)+v0dotux
        y(2)=VDOT(uy,v)+v0dotuy
        z(2)=VDOT(uz,v)+v0dotuz
        IF((x(2)**2+y(2)**2).gt.mx2) GO TO 10
        call vequ(vec(1,i+1,j+1,f),v)
        x(3)=VDOT(ux,v)+v0dotux
        y(3)=VDOT(uy,v)+v0dotuy
        z(3)=VDOT(uz,v)+v0dotuz
        IF((x(3)**2+y(3)**2).gt.mx2) GO TO 10
        p0=x(0)*y(1)-y(0)*x(1)
        p1=x(1)*y(3)-y(1)*x(3)
        p2=x(3)*y(2)-y(3)*x(2)
        p3=x(2)*y(0)-y(2)*x(0)
        if((p0.gt.0).and.(p1.gt.0).and.(p2.gt.0).and.(p3.gt.0)) then
          x(3)=x(0)-x(1)-x(2)+x(3)
          x(1)=x(1)-x(0)
          x(2)=x(2)-x(0)
          y(3)=y(0)-y(1)-y(2)+y(3)
          y(1)=y(1)-y(0)
          y(2)=y(2)-y(0)
          z(3)=z(0)-z(1)-z(2)+z(3)
          z(1)=z(1)-z(0)
          z(2)=z(2)-z(0)
          a=x(1)*y(3)-x(3)*y(1)
          b=x(1)*y(2)-x(2)*y(1)-y(0)*x(3)+x(0)*y(3)
          c=-y(0)*x(2)+x(0)*y(2)
          if(b**2.gt.4*a*c) then
            if(abs(a).gt.(1.d-8)) then
              w1=(sqrt(1-4*a*c/b**2)-1)*b/(2*a)
            else
              w1=-c/b-a*c**2/b**3
            endif
            w2=(-x(0)-x(1)*w1)/(x(2)+x(3)*w1)
            d0=min(d0,(z(0)+z(1)*w1+z(2)*w2+z(3)*w1*w2))
            USE=.TRUE.
          endif
        endif
10      continue
      enddo
      enddo
      enddo

      v(1)=D0*UZ(1)-V0(1)
      v(2)=D0*UZ(2)-V0(2)
      v(3)=D0*UZ(3)-V0(3)

      return
      end
	
c  ...............................................................
      SUBROUTINE DISPLAY(NTMP,Q0,S0,V,UX,UY,UZ,
     .                   HT,HUSE,HMIN,HMAX,BITS,FILENAME)
c  ...............................................................

      IMPLICIT NONE

      INTEGER               NTMP

      DOUBLE PRECISION      S0
      DOUBLE PRECISION      V(3)
      DOUBLE PRECISION      UX(3)
      DOUBLE PRECISION      UY(3)
      DOUBLE PRECISION      UZ(3)
      DOUBLE PRECISION      HMIN, HMAX
      DOUBLE PRECISION      Z3

      REAL*4                HT(-NTMP:NTMP,-NTMP:NTMP)

      INTEGER               Q0
      INTEGER               I
      INTEGER               J
      INTEGER               K, K1, K2

      CHARACTER*1           BITS
      CHARACTER*72          FILENAME
      CHARACTER*80          LINE
      CHARACTER*138         HEADER
      CHARACTER*4000        CLINE
 
      LOGICAL               HUSE(-NTMP:NTMP,-NTMP:NTMP)

      CHARACTER*2           C2
      INTEGER*2             I2
      EQUIVALENCE          (I2,C2)
      LOGICAL               LFLAG
      c2='69'
      LFLAG=.TRUE.
      if(i2.eq.13881) LFLAG=.FALSE.

      LFLAG=.FALSE.        ! comment out this line to allow little-endian

      k=1
      write(line(1:10),fmt='(i10)') 2*q0+1
      do k1=1,10
      if(line(k1:k1).ne.' ') then
        header(k:k+10-k1) = line(k1:10)
        k=k+11-k1
        go to 10
      endif
      enddo
10    continue
      header(k:k)=' '
      k=k+1
      write(line(1:10),fmt='(i10)') 2*q0+1
      do k1=1,10
      if(line(k1:k1).ne.' ') then
        header(k:k+10-k1) = line(k1:10)
        k=k+11-k1
        go to 20
      endif
      enddo
20    continue
      header(k:k)=char(10)
      k=k+1
      if(bits.eq.'a') then
        Z3=(hmax-hmin)/254
        header(k:k+2)='255'
        k=k+3
      else
        Z3=(hmax-hmin)/65534
        header(k:k+4)='65535'
        k=k+5
      endif
      header(k:k)=char(10)
      k2=k
      
      open(unit=20, file=filename, recl=1, access='direct', 
     .     status='unknown')
        write(20,rec=1) 'P'
        write(20,rec=2) '5'
        write(20,rec=3) char(10)
        k=3
        k=k+1
        write(20,rec=k) '#'
        WRITE(LINE,FMT='(2I20,20X,A20)') 2*Q0+1, 2*Q0+1,
     .                                '  IMAX, JMAX       ' 
        do k1=1,80
          k=k+1
          write(20,rec=k) line(k1:k1)
        enddo
        k=k+1
        write(20,rec=k) char(10)
        k=k+1
        write(20,rec=k) '#'
        WRITE(LINE,FMT='(2D20.10,20X,A20)') S0, Z3,
     .                                '  SCL, HTSCL       ' 
        do k1=1,80
          k=k+1
          write(20,rec=k) line(k1:k1)
        enddo
        k=k+1
        write(20,rec=k) char(10)
        k=k+1
        write(20,rec=k) '#'
        WRITE(LINE,FMT='(2D20.10,20X,A20)') hmin, hmax,
     .                                '  HTMIN, HTMAX     ' 
        do k1=1,80
          k=k+1
          write(20,rec=k) line(k1:k1)
        enddo
        k=k+1
        write(20,rec=k) char(10)
        k=k+1
        write(20,rec=k) '#'
        WRITE(LINE,FMT='(3D20.10,A20)') (V(I), I=1,3),
     .                                '  VLM              ' 
        do k1=1,80
          k=k+1
          write(20,rec=k) line(k1:k1)
        enddo
        k=k+1
        write(20,rec=k) char(10)
        k=k+1
        write(20,rec=k) '#'
        WRITE(LINE,FMT='(3D20.10,A20)') (UX(I), I=1,3),
     .                                '  UX               ' 
        do k1=1,80
          k=k+1
          write(20,rec=k) line(k1:k1)
        enddo
        k=k+1
        write(20,rec=k) char(10)
        k=k+1
        write(20,rec=k) '#'
        WRITE(LINE,FMT='(3D20.10,A20)') (UY(I), I=1,3),
     .                                '  UY               ' 
        do k1=1,80
          k=k+1
          write(20,rec=k) line(k1:k1)
        enddo
        k=k+1
        write(20,rec=k) char(10)
        k=k+1
        write(20,rec=k) '#'
        WRITE(LINE,FMT='(3D20.10,A20)') (UZ(I), I=1,3),
     .                                '  UZ               ' 
        do k1=1,80
          k=k+1
          write(20,rec=k) line(k1:k1)
        enddo
        k=k+1
        write(20,rec=k) char(10)
        do k1=1,k2
          k=k+1
          write(20,rec=k) header(k1:k1)
        enddo
        
        DO J=-Q0,Q0
        DO I=-Q0,Q0
          if(bits.eq.'a') then
            K=K+1
            IF(HUSE(I,J)) THEN
              K1=1+NINT(254*(HT(I,J)-hmin)/(hmax-hmin))
            ELSE
              K1=0
            ENDIF
            write(20,rec=k) char(k1)
          else
            K=K+2
            IF(HUSE(I,J)) THEN
              K1=1+NINT(65534*(HT(I,J)-hmin)/(hmax-hmin))
            ELSE
              K1=0
            ENDIF
            K2=K1/256
            K1=K1-256*K2
            if(lflag) then
              write(20,rec=k-1) char(k1)
              write(20,rec=k)   char(k2)
            else
              write(20,rec=k-1) char(k2)
              write(20,rec=k)   char(k1)
            endif
          endif
        ENDDO
        ENDDO
      close(unit=20)

      RETURN
      END

c  ...............................................................
      subroutine patch_coords(qsz,huse,ht,ux,uy,uz)
c  ...............................................................

      implicit none

      integer*4       ntmp
      PARAMETER       (NTMP=513)

      integer*4       i, j, i0, j0, i1, j1, 
     .                qsz, n(-ntmp:ntmp,-ntmp:ntmp)

      real*8          mat(6,6), imat(6,6), w(6), z1, z2, VDOT,
     .                ux(3), uy(3), uz(3), wx(3), wy(3), wz(3)      

      real*4          ht(-ntmp:ntmp,-ntmp:ntmp),
     .                ht0(-ntmp:ntmp,-ntmp:ntmp)
      logical         huse(-ntmp:ntmp,-ntmp:ntmp),
     .                huse0(-ntmp:ntmp,-ntmp:ntmp) 

      do i=1,3
      do j=1,3
        mat(i,j)=0
      enddo
      enddo
      do i=1,3
        w(i)=0
      enddo

      do i=-qsz,qsz
      do j=-qsz,qsz
      if(huse(i,j)) then
        mat(1,1)=mat(1,1)+1
        mat(1,2)=mat(1,2)+i
        mat(1,3)=mat(1,3)+j
        mat(2,1)=mat(2,1)+i
        mat(2,2)=mat(2,2)+i*i
        mat(2,3)=mat(2,3)+i*j
        mat(3,1)=mat(3,1)+j
        mat(3,2)=mat(3,2)+i*j
        mat(3,3)=mat(3,3)+j*j
        w(1)=w(1)+ht(i,j)
        w(2)=w(2)+i*ht(i,j)
        w(3)=w(3)+j*ht(i,j)
      endif
      enddo
      enddo

      call INVERTN(3,mat,imat)
      z1=imat(2,1)*w(1)+imat(2,2)*w(2)+imat(2,3)*w(3)
      z2=imat(3,1)*w(1)+imat(3,2)*w(2)+imat(3,3)*w(3)
      wz(1)=uz(1)-z1*uy(1)-z2*ux(1)
      wz(2)=uz(2)-z1*uy(2)-z2*ux(2)
      wz(3)=uz(3)-z1*uy(3)-z2*ux(3)
      
      CALL VHAT(WZ,WZ)
      CALL VEQU(UY,WY)
      CALL UCRSS(WY,WZ,WX) 
      CALL UCRSS(WZ,WX,WY) 

      mat(1,1)=VDOT(wx,ux)
      mat(1,2)=VDOT(wx,uy)
      mat(1,3)=VDOT(wx,uz)
      mat(2,1)=VDOT(wy,ux)
      mat(2,2)=VDOT(wy,uy)
      mat(2,3)=VDOT(wy,uz)
      mat(3,1)=VDOT(wz,ux)
      mat(3,2)=VDOT(wz,uy)
      mat(3,3)=VDOT(wz,uz)

      do i=-qsz,qsz
      do j=-qsz,qsz
        n(i,j)=0
        ht0(i,j)=0
        huse0(i,j)=.false.
      enddo
      enddo
        
      do i=-qsz,qsz
      do j=-qsz,qsz
      if(huse(i,j)) then
        do i0=-2,2
        do j0=-2,2
          z1=i+i0/5.
          z2=j+j0/5.
          w(1)=mat(1,1)*z2+mat(1,2)*z1+mat(1,3)*ht(i,j)
          w(2)=mat(2,1)*z2+mat(2,2)*z1+mat(2,3)*ht(i,j)
          w(3)=mat(3,1)*z2+mat(3,2)*z1+mat(3,3)*ht(i,j)
          i1=nint(w(2))
          j1=nint(w(1))
          if((abs(i1).le.qsz).and.(abs(j1).le.qsz)) then
            n(i1,j1)=n(i1,j1)+1
            ht0(i1,j1)=ht0(i1,j1)+w(3)
            huse0(i1,j1)=.true.
          endif
        enddo
        enddo
      endif
      enddo
      enddo
          
      z1=ht0(0,0)/n(0,0)
      do i=-qsz,qsz
      do j=-qsz,qsz
        huse(i,j)=.false.
        ht(i,j)=0
        if(huse0(i,j)) then
          huse(i,j)=.true.
          ht(i,j)=ht0(i,j)/n(i,j)-z1
        endif
      enddo
      enddo

      call vequ(wx,ux)      
      call vequ(wy,uy)      
      call vequ(wz,uz)      

      return
      end

c   ........................................................
      SUBROUTINE INVERTN(N,M,MINV)
c   ........................................................

      IMPLICIT NONE
      INTEGER*4    J, K, L, N
      REAL*8       M(6,6), MINV(6,6)
      REAL*8       U(6,6), V(6,6), D(6)

      DO K=1,N
      DO L=1,N
        U(K,L)=0.
      ENDDO
      ENDDO

      DO L=1,N
       U(L,N)=M(L,N)/M(N,N)
       D(L)=M(L,L)
       U(L,L)=1.
      ENDDO

      DO L=N-1,1,-1
       DO J=L+1,N
        D(L)=D(L)-U(L,J)*U(L,J)*D(J)
       ENDDO
       IF (L.GT.1) THEN
        DO K=1,L-1
         U(K,L)=M(K,L)
         DO J=L+1,N
          U(K,L)=U(K,L)-U(K,J)*U(L,J)*D(J)
         ENDDO
         U(K,L)=U(K,L)/D(L)
        ENDDO
       ENDIF
      ENDDO

      DO K=1,N
       DO L=1,N
        V(K,L)=0.
       ENDDO
       V(K,K)=1.
      ENDDO
      
      DO K=1,N  
       DO L=N-1,1,-1
        DO J=L+1,N             
         V(L,K)=V(L,K)-U(L,J)*V(J,K)
        ENDDO
       ENDDO
      ENDDO

      DO K=1,N
      DO L=1,N
       MINV(K,L)=0.
       DO J=1,N
        MINV(K,L)=MINV(K,L)+V(J,K)*V(J,L)/D(J)
       ENDDO
      ENDDO
      ENDDO

      RETURN
      END

