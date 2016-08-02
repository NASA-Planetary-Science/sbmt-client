# Makefile to update the CERES images in the /project/nearsdc/data
# directory.
#
# The /project/sbmtpipeline/rawdata and /project/sbmtpipeline/processed
# directories are used for permanent file storage and as intermediary 
# storage during image processing. 
#
# Steps required to update image data in /project/nearsdc/data:
# 1.  Get new 1b_rdr .FIT files and .LBL files from the mission. 
# 2.  Run create_info_files_ceres to create '.INFO' files for all of the 
#     relavent new .FIT files.
# 3.  Update the image list files
# 4.  Add the new files to the database
# 5.  Test to make sure some of the new files can be found by the image
#     search feature in the client, and mapped on the body.
#
# Desired Enhancements and 'gotchas'
# ----------------------------------
# (1) It is desired to be able to update the images in the /project/nearsdc/data
# directly without creating so much duplicate storage on the
# /project/sbmtpipeline disk.  New .FIT files *could* be copied directly
# to /project/nearsdc/data, and new .INFO files *could* be created directly
# in /project/nearsdc/data, BUT, the 'create_info_files_ceres' program
# currently requires the .FIT files and .LBL files to be located in the
# same directory when it in order to create the .INFO files from them.
# It would NOT be desired to copy all of the .LBL files to
# /project/nearsdc/data. Therefore, 'create_info_files_ceres' would
# need to be modified to accept a new input parameter telling it
# where the .LBL files are located, before modifying this Makefile
# to update the image data directly to /project/nearsdc/data.
#
# (2) It is probably desireable to maintain the ability to use the intermediary
# directories for testing purposes, so I would not recommend removing
# that capability from this Makefile.
#
# (3) Would like to look into using the url parameter to the database generator
# it might prevent from having to run the install_images target before
# running the database target to put new images in the beta tables.
#
# (4) Would like to add a target to enable rebuilding and installing the
# the small body model in case an update to the model is recevied.
#
# (5) Need to find out if there is ever a reason to remake or update
# the sumfiles and remake the or update the gaskell tables in the
# database.
#

# First, Include global variables needed by this makefile.
include ${SBMTROOT}/config/Makefiles/Variables

# Set the body name. This value is used as an input paramter to
# the database generator program.
BODYNAME := CERES

# Set the name of the program that will create the .INFO files.
# The program is expected to be located in the same directory
# as this Makefile.
CREATE_INFOFILES_PROC := ./create_info_files_ceres
SOURCES_MAIN          := create_info_files_ceres.cpp

##################################################################################
#
# DIRECTORIES used by this Makefile
#
##################################################################################

# Source location for all CERES Mission data
CERES_DATA_DIR     := /project/dawn/data2

# Location of SPICE files. IMPORTANT!! Change the next line to the path 
# of your c-spice installation! Must be at least SPICE version N0064.
SPICE_DIR=${NEARSDCROOT}/software/spice/cspice

# SBMT pipeline and live processing locations
RAWDATA_DIR         := ${PIPELINEROOT}/rawdata/dawn/ceres
PROCESSED_DIR       := ${PIPELINEROOT}/processed/dawn/ceres

LOCAL_IMAGES_DIR    := ${RAWDATA_DIR}/fc
LOCAL_INFOFILES_DIR := ${PROCESSED_DIR}/infofiles
LOCAL_GALLERY_DIR   := ${PROCESSED_DIR}/gallery

NEARSDC_DATA_DIR      := ${NEARSDCROOT}/data
LIVE_FC_DIR            := ${NEARSDC_DATA_DIR}/GASKELL/CERES/FC
LIVE_IMAGES_DIR_SUFFIX := /GASKELL/CERES/FC/images
LIVE_IMAGES_DIR        := ${NEARSDC_DATA_DIR}${LIVE_IMAGES_DIR_SUFFIX}
LIVE_INFOFILES_DIR     := ${LIVE_FC_DIR}/infofiles
LIVE_GALLERY_DIR       := ${LIVE_FC_DIR}/gallery

##################################################################################
#
# FILES used by this Makefile
#
##################################################################################

# Used by metakernel target, the .pl script creates the .mk file in the PROCESSED_DIR.
METAKERNEL_SCRIPT := ./mkmetakernel.pl
METAKERNEL_FILE   := kernels_ceres.mk

# The imagelist.txt file is used by the client to display a list of files
# for the 'fixed list' query.  It contains the full list of files that have 
# corresponding .INFO files. The fixed list query is only used when the images
# are not in the database. 
IMAGELIST    := imagelist.txt

# The imagelist-fullpath.txt file is used by the DatabaseGeneratorSql program.  It
# contains the list of files (with relative path) that the database generator
# should try to add to the database. The database generator will only add those files
# to the database if there is a corresponding .INFO file. need to be added to the database.
# This file may or may not have a full list of files in it. If the last operation
# was to add files to the database, then this file will contain only the list of new
# files to be added. This file is not used by the client.
#IMAGELIST_FULLPATH := imagelist-fullpath.txt
IMAGELIST_FULLPATH := uniqFcFiles.txt

# Lists of fit files with full path. These lists are needed by the 
# create_info_files_ceres program. 
ALL_FIT_FILES := allFcFiles.txt
NEW_FIT_FILES := newFitFiles.txt

##################################################################################
#
# FILE LOCATIONS used by this Makefile - shorthands for referencing files.
#
##################################################################################

LOCAL_ALL_FIT_FILES      := ${RAWDATA_DIR}/${ALL_FIT_FILES}
LOCAL_NEW_FIT_FILES      := ${RAWDATA_DIR}/${NEW_FIT_FILES}
LOCAL_IMAGELIST          := ${PROCESSED_DIR}/${IMAGELIST}
LOCAL_IMAGELIST_FULLPATH := ${PROCESSED_DIR}/${IMAGELIST_FULLPATH}
TEMP_LIST := ${PROCESSED_DIR}/_tempList.txt

LIVE_IMAGELIST           := ${LIVE_FC_DIR}/${IMAGELIST}
LIVE_IMAGELIST_FULLPATH  := ${LIVE_FC_DIR}/${IMAGELIST_FULLPATH}
LIVE_ALL_FIT_FILES       :=${LIVE_FC_DIR}/${ALL_FIT_FILES}



PHONY_TARGET : 




###########################################################################################
#
# Targets
#
###########################################################################################
#
# all :
# install :
#    Nothing to do for 'all' and 'install'. You must use specialized target names
#    in this makefile.
${most_supported_targets} : 

#
# metakernel : Creates the ${PROCESSED_DIR}/kernels_ceres.mk file.
# which is needed for making the infofiles.
#
metakernel : 
	chmod +x ${METAKERNEL_SCRIPT}
	${PERL} ${METAKERNEL_SCRIPT}

#
# images : 
#   This target copies the new .FIT files and .LBL files from the mission data directory,
#   and create the image list files.
# 
images :
ifdef REMAKE_EVERYTHING
	find ${CERES_DATA_DIR} \( -path "*/fc/ceres/*/FC11B*.FIT" -or -path "*/fc/ceres/*/FC21B*.FIT" \
	-or -path "*/fc/ceres/*/FC11B*.LBL" -or -path "*/fc/ceres/*/FC21B*.LBL" \) -and \( ! -path "*/fc/ceres/*/old/*" \) \
	| xargs cp -t ${LOCAL_IMAGES_DIR}
else
	find ${CERES_DATA_DIR} \( -path "*/fc/ceres/*/FC11B*.FIT" -or -path "*/fc/ceres/*/FC21B*.FIT" \
	-or -path "*/fc/ceres/*/FC11B*.LBL" -or -path "*/fc/ceres/*/FC21B*.LBL" \) -and \( ! -path "*/fc/ceres/*/old/*" \) \
	| xargs cp -uv -t ${LOCAL_IMAGES_DIR} > ${TEMP_LIST}
endif
	find -L ${LOCAL_IMAGES_DIR} -name "*.FIT" -type f | sort > ${LOCAL_ALL_FIT_FILES}
	awk -F: -v var=${LOCAL_IMAGES_DIR} '{gsub(var,"/GASKELL/CERES/FC/images");print}' ${LOCAL_ALL_FIT_FILES} >${LOCAL_IMAGELIST_FULLPATH}

#
# infofiles :
#   Creates the *.INFO files for all of the *.FIT files, and generates the
#   imagelist file that is used for fixed queries. The imagelist file needs to 
#   contain the full list of images.
#
infofiles : ${CREATE_INFOFILES_PROC} metakernel
ifdef REMAKE_EVERYTHING
	rm -rf ${LOCAL_INFOFILES_DIR}
	mkdir -p ${LOCAL_INFOFILES_DIR}
endif
	${CREATE_INFOFILES_PROC} ${PROCESSED_DIR}/${METAKERNEL_FILE} ${LOCAL_ALL_FIT_FILES} ${LOCAL_INFOFILES_DIR} ${BODYNAME} ${LOCAL_IMAGELIST}

#
# gallery : NOT USED YET.
#   Creates all of the jpeg files and webpage html file for the image gallery
#
gallery : 
ifdef REMAKE_EVERYTHING
	rm -rf ${LOCAL_GALLERY_DIR}
	mkdir -pv ${LOCAL_GALLERY_DIR}
endif
	${BINROOT}/fits2thumbnails.py "${LOCAL_IMAGES_DIR}" "${LOCAL_GALLERY_DIR}"
	${BINROOT}/make_gallery_webpage.py "*.FIT" "${LOCAL_IMAGES_DIR}" "${LOCAL_GALLERY_DIR}" "${BODYNAME} Image Gallery"

#
# database :
#   This target executes the database generator. The database generator uses the 
#   IMAGELIST_FULLPATH file to update the tables in the database.  By default, 
#   the program will only update the 'beta' tables, it doesn't update the live
#   tables (see the 'install_database' target to modify the live tables). The
#   database generator reads all files from the IMAGELIST_FULLPATH and adds each
#   file to the database. You can save processing time if you provide the 
#   '--append-tables' parameter. This will tell the database generator to skip files
#   listed in the IMAGELIST_FULLPATH file that are already in the database. 
#
#   Caveats: 
#   (1) Requires that the IMAGELIST_FULLPATH file be installed in the LIVE DIRECTORY.
#       There is a work-a-round for this, but, it might screw up the client.  There
#       is a parameter that lets you provide the url of where the files are located.
#   (2) Before running this program, you need to create a file called mysql-login.txt 
#       in the folder ${HOME}/.neartool that contains the MySQL database username on 
#       the first line and the password on the second line. Note these are different 
#       from your username/password  used to access the website
#       
#   Body name must be the last parameter. Options must come first.
#

database : ${DATABASE_GENERATOR}
ifdef REMAKE_EVERYTHING
	${DATABASE_GENERATOR} 2 ${BODYNAME} > _create_beta.log
else
	${DATABASE_GENERATOR} --append-tables 2 ${BODYNAME} > _append_beta.log
endif 
#
# ${DATABASE_GENERATOR} :
#  TBD - this file needs to be made by the release process and installed
#  in SBMTROOT/bin but that is not working yet. So, this is a placeholder
#  until that can be put in place.
${DATABASE_GENERATOR} :

#
# install_images :
#  Don't use rsync because you don't want to also copy the .LBL files to the live images directory.
install_images :
	find -L ${LOCAL_IMAGES_DIR} -name "*.FIT" -type f | xargs cp -uv -t ${LIVE_IMAGES_DIR} > ${PROCESSED_DIR}/_installed_images.txt
	mv ${LIVE_IMAGELIST_FULLPATH} ${LIVE_IMAGELIST_FULLPATH}.bak
	cp ${LOCAL_IMAGELIST_FULLPATH} ${LIVE_IMAGELIST_FULLPATH}
	chmod 664 ${LIVE_IMAGELIST_FULLPATH}
	mv ${LIVE_ALL_FIT_FILES} ${LIVE_ALL_FIT_FILES}.bak
	cp ${LOCAL_ALL_FIT_FILES} ${LIVE_ALL_FIT_FILES}
	chmod 664 ${LIVE_ALL_FIT_FILES}

#
# install_infofiles :
#   not using --delete option because I don't want to delete any of the infofiles that were in the infofiles directory
#   directory.
#
install_infofiles :
	cp -au ${LOCAL_INFOFILES_DIR}/. ${LIVE_INFOFILES_DIR}/
	mv ${LIVE_IMAGELIST} ${LIVE_IMAGELIST}.bak
	cp ${LOCAL_IMAGELIST} ${LIVE_IMAGELIST}
	chmod 664 ${LIVE_IMAGELIST}

#
# install_gallery :
#  Makes sure the live image gallery directory allready exists. before synching.
#  rsync gives the following error and the make stops.
#  rsync: failed to set times on "/project/nearsdc/data/GASKELL/CERES/FC/gallery/.": Operation not permitted (1)
#  So I use cp instead
#  
install_gallery : 
	mkdir -p ${LIVE_GALLERY_DIR}
	cp -au ${LOCAL_GALLERY_DIR}/. ${LIVE_GALLERY_DIR}/
	
#where does the database generator expect to be run from?  What are the files it expects and where are the
#expected to be located. It expects to be run from sbmt/build/sbmt-extras/bin
#
# by default modifies the beta tables. Option --modify-main is required to modify the main database.
# the --apend-tables option is needed to just update and not regenerate.
#
# install_database :
#   
install_database :
ifdef REMAKE_EVERYTHING
	${DATABASE_GENERATOR} --modify-main 2 ${BODYNAME} > _create_main.log
else
	${DATABASE_GENERATOR} --append-tables --modify-main 2 ${BODYNAME} > _append_main.log
endif 

${CREATE_INFOFILES_PROC}  : ${SOURCES_MAIN}
	g++ -Wall ${SOURCES_MAIN} -I ${SPICE_DIR}/include ${SPICE_DIR}/lib/cspice.a -o ${CREATE_INFOFILES_PROC}


# Don't want gallery for right now.
# Note: install_images and install_infofiles needs to be run before 'database'
update_ceres_fc_images : images infofiles gallery install_images install_infofiles install_gallery database 

# this target will be run by the cron job.
install_ceres_fc_images : images infofiles gallery install_images install_infofiles install_gallery install_database

include ${SBMTROOT}/config/Makefiles/Master
