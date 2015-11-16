#!/bin/bash

#DIR=/project/nearsdc/data/GASKELL/RQ36_V3
cd `dirname $0`

SUMFILE_MAP=make_sumfiles.in

#Rename sumfiles to same root name as image files. 
#Use the mapping in make_sumfiles.in to determine
#the time corresponding to a given sumfile. Locate
#the image file name using this time string. Apply
#the time string and the remaining characters in
#the image file's root name to rename the sumfile.
runRenameSumfiles() 
{
    IMAGE_DIR=$1/images
    
    #replace colons with underscores in filenames
    for i in $IMAGE_DIR/*:*; 
    do
       mv "$i" "${i//:/_}"
    done

    SUMFILE_DIR=$1/$2
    SUMFILE_DIR_RENAMED=$1/sumfiles_renamed
    if [ -d $SUMFILE_DIR_RENAMED ]
	then
	echo "removing $SUMFILE_DIR_RENAMED"
	rm -r $SUMFILE_DIR_RENAMED
    fi

    mkdir $SUMFILE_DIR_RENAMED

    #rename ".sum" to ".SUM"
    for i in $SUMFILE_DIR/*.sum
    do
       mv $i ${i%.sum}.SUM
    done

    #read SUMFILE_MAP
    while read -r line
    do
    	while read SUMFILENAME f2 f3 f4 f5 TIMESTR
    	do
    	    if [ -f $SUMFILE_DIR/$SUMFILENAME.SUM ] 
    	    then
                #replace colons with underscores in time string
    		TIME=${TIMESTR//:/_}
#		echo "$TIME $SUMFILENAME"

    		#find image file corresponding to this sumfile
    		IMAGEFILE=$(find $IMAGE_DIR -name "$TIME*")
     
    		LEN=${#IMAGEFILE}
    		if [ $LEN -gt 0 ] 
    		then
    		
    		    #write to imagelist.txt
    		    echo "$(basename $IMAGEFILE) ${TIMESTR:0:23}" >> $SUMFILE_DIR_RENAMED/imagelist.txt
    		    
    		    #extract the ending of the root image filename
    		    SUFFIX=${IMAGEFILE:$LEN-17:13}
                        echo "renaming $SUMFILE_DIR/$SUMFILENAME.SUM to $SUMFILE_DIR_RENAMED/$TIME$SUFFIX.SUM"
    
                        #Rename the sumfile using the time and suffix
    		    cp $SUMFILE_DIR/$SUMFILENAME.SUM $SUMFILE_DIR_RENAMED/$TIME$SUFFIX.SUM
    		    
    		fi
#    	    else
#    		echo "Cannot find image file $IMAGE_DIR/*$TIME*"
            fi
    	done    
    done < "$SUMFILE_MAP"
    
    chmod -R g+w $SUMFILE_DIR_RENAMED 
}

runRenameSumfiles MAPCAM sumfiles_all
runRenameSumfiles POLYCAM sumfiles_orig