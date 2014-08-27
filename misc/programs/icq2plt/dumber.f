c  g77  SOURCE/DUMBER.f -o EXECUTABLES/DUMBER.e
c  ftn  SOURCE/DUMBER.f -o EXECUTABLES/DUMBER.e

      implicit none

      real*8           vec(3,0:512,0:512,6)
      integer*4        i, j, k, f, q, m
      character*72     infile
      character*72     outfile


      write(6,*) 'Input infile'
      read(5,fmt='(a72)') infile
      write(6,*) 'Input outfile'
      read(5,fmt='(a72)') outfile
      write(6,*) 'Input m (2,4,8 ...)'
      read(5,*) m

      open(unit=40,file=infile,status='old')
      open(unit=30,file=outfile,status='unknown')
        read(40,*) q
        write(30,*) q/m
        do f=1,6
        do j = 0,q
        do i = 0,q
          read(40,*) (vec(k,i,j,f), k=1,3)
          if((i.eq.(m*(i/m))).and.(j.eq.(m*(j/m)))) then
            write(30,fmt='(3f12.5)') (vec(k,i,j,f), k=1,3)
          endif
        enddo
        enddo
        enddo
      close(unit=30)
      close(unit=40)

      stop
      end
