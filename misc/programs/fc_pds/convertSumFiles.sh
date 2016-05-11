#!/bin/sh
imagedir='images'
sumdir='sumfiles_test'
infoimagelist='imagelist.txt'
sumimagelist=$sumdir'/imagelist.txt'

rm $sumimagelist

for i in `cat $infoimagelist | sed 's/FIT /FIT,/'`
do
  imagefilename=`echo $i | sed 's/,.*//'`
  date=`echo $i | sed 's/^.*,//'`
#  echo $imagefilename $date
 
  oldsumfilename=`echo $imagefilename | sed 's/_.*$/.SUM/' |  sed 's/FC21B/FC2_0/'`
  newsumfilename=`echo $imagefilename | sed 's/\.FIT/.SUM/'`
  if ls $sumdir/$oldsumfilename 2> /dev/null
  then
    echo 'renaming ' $sumdir/$oldsumfilename ' to ' $sumdir/$newsumfilename
    mv $sumdir/$oldsumfilename $sumdir/$newsumfilename
    echo $imagefilename $date >> $sumimagelist
  else
    echo 'no sumfile found corresponding to image file' $sumdir/$oldsumfilename
  fi
done

