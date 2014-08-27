c  g77  SOURCE/SHAPE2PLATES.f -o EXECUTABLES/SHAPE2PLATES.e
c  ftn  SOURCE/SHAPE2PLATES.f -o EXECUTABLES/SHAPE2PLATES.e


      implicit none

      real*8           vec(3,0:512,0:512,6), w1(3), w2(3), z1, z2
      integer*4        i, j, k, f, q, n0
      integer*4        n(0:512,0:512,6)
      character*72     infile
      character*72     outfile


      write(6,*) 'Input infile'
      read(5,fmt='(a72)') infile
      write(6,*) 'Input outfile'
      read(5,fmt='(a72)') outfile

      open(unit=40,file=infile,status='old')
      open(unit=30,file=outfile,status='unknown')
        read(40,*) q
        write(30,*) 6*(q+1)**2
        n0=0      
        do f=1,6
        do j = 0,q
        do i = 0,q
          read(40,*) (vec(k,i,j,f), k=1,3)
          n0=n0+1
          write(30,fmt='(I10,3f15.5)') n0, (vec(k,i,j,f), k=1,3)
          n(i,j,f)=n0
        enddo
        enddo
        enddo
        do i=1,q-1
          n(i,q,6)=n(q-i,q,4)
          n(i,0,6)=n(i,q,2)
          n(i,0,5)=n(q,q-i,1)
          n(i,0,4)=n(q-i,0,1)
          n(i,0,3)=n(0,i,1)
          n(i,0,2)=n(i,q,1)
        enddo
        do j=1,q-1
          n(q,j,6)=n(j,q,5)
          n(q,j,5)=n(0,j,4)
          n(q,j,4)=n(0,j,3)
          n(q,j,3)=n(0,j,2)
          n(0,j,6)=n(q-j,q,3)
          n(0,j,5)=n(q,j,2)
        enddo
        n(0,0,3)=n(0,0,1)
        n(q,0,4)=n(0,0,1)
        n(0,0,2)=n(0,q,1)
        n(q,0,3)=n(0,q,1)
        n(0,0,4)=n(q,0,1)
        n(q,0,5)=n(q,0,1)
        n(0,0,5)=n(q,q,1)
        n(q,0,2)=n(q,q,1)
        n(0,0,6)=n(0,q,2)
        n(q,q,3)=n(0,q,2)
        n(0,q,5)=n(q,q,2)
        n(q,0,6)=n(q,q,2)
        n(q,q,4)=n(0,q,3)
        n(0,q,6)=n(0,q,3)
        n(q,q,5)=n(0,q,4)
        n(q,q,6)=n(0,q,4)
        write(30,*) 12*q**2
        n0=0
        do f=1,6
        do i=0,q-1
        do j=0,q-1
          w1(1)=vec(2,i,j,f)*vec(3,i+1,j+1,f)
     .         -vec(3,i,j,f)*vec(2,i+1,j+1,f)
          w1(2)=vec(3,i,j,f)*vec(1,i+1,j+1,f)
     .         -vec(1,i,j,f)*vec(3,i+1,j+1,f)
          w1(3)=vec(1,i,j,f)*vec(2,i+1,j+1,f)
     .         -vec(2,i,j,f)*vec(1,i+1,j+1,f)
          w2(1)=vec(2,i+1,j,f)*vec(3,i,j+1,f)
     .         -vec(3,i+1,j,f)*vec(2,i,j+1,f)
          w2(2)=vec(3,i+1,j,f)*vec(1,i,j+1,f)
     .         -vec(1,i+1,j,f)*vec(3,i,j+1,f)
          w2(3)=vec(1,i+1,j,f)*vec(2,i,j+1,f)
     .         -vec(2,i+1,j,f)*vec(1,i,j+1,f)
          z1=w1(1)**2+w1(2)**2+w1(3)**2
          z2=w2(1)**2+w2(2)**2+w2(3)**2
          if(z1.le.z2) then
            n0=n0+1
            write(30,fmt='(4I10)') n0, n(i,j,f), 
     .                             n(i+1,j+1,f), n(i+1,j,f)
            n0=n0+1
            write(30,fmt='(4I10)') n0, n(i,j,f), 
     .                             n(i,j+1,f), n(i+1,j+1,f)
          else
            n0=n0+1
            write(30,fmt='(4I10)') n0, n(i,j,f), 
     .                             n(i,j+1,f), n(i+1,j,f)
            n0=n0+1
            write(30,fmt='(4I10)') n0, n(i+1,j,f), 
     .                             n(i,j+1,f), n(i+1,j+1,f)
          endif
        enddo
        enddo
        enddo
      close(unit=30)
      close(unit=40)
      
      stop
      end

