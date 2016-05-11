imagelist.txt contains as many lines as there are info files.

make metakernel

Executes mkmetakernel.pl to create the /project/sbmtpipeline/processed/dawn/ceres/metakernel.mk file 

make images

Copies new images to /project/sbmtpipeline/rawdata/dawn/ceres/fc
Creates /project/sbmtpipeline/rawdata/dawn/ceres/allFcFiles.txt
Creates /project/sbmtpipeline/processed/dawn/ceres/uniqFcFiles.txt

make infofiles

Compiles and executes create_info_files_ceres to create new infofiles in 
/project/sbmtpipeline/processed/dawn/ceres/infofiles/
Creates /project/sbmtpipeline/processed/dawn/ceres/imagelist.txt

make gallery

Executes /project/sbmtpipeline/sbmt/bin/fits2thumbnails.py to create large and 
small .jpeg files for all fits files in /project/sbmtpipeline/processed/dawn/ceres/gallery
Executes /project/sbmtpipeline/sbmt/bin/make_gallery_webpage.py to combine all .jpeg 
files into an image gallery index.html page in 
/project/sbmtpipeline/processed/dawn/ceres/gallery

make database

Executes /project/sbmtpipeline/sbmt/bin/DatabaseGeneratorSql to update
the beta tables in with the new images.

make install_images

Copies new .FIT files to /project/nearsdc/data/GASKELL/CERES/FC/images
Copies uniqFcFiles.txt to /project/nearsdc/data/GASKELL/CERES/FC/

make install_infofiles

Rsyncs /project/sbmtpipeline/processed/dawn/ceres/infofiles with /project/nearsdc/data/GASKELL/CERES/FC/infofiles
Copies imagelist.txt to /project/nearsdc/data/GASKELL/CERES/FC/

make install_gallery

Rsyncs /project/sbmtpipeline/processed/dawn/ceres/gallery with /project/nearsdc/data/GASKELL/CERES/FC/gallery

make install_database

Executes /project/sbmtpipeline/sbmt/bin/DatabaseGeneratorSql to update
the main tables in with the new images.

make update_ceres_fc_images

Executes all of the following:
   make images
   make infofiles
   make gallery
   make install_images
   make install_infofiles
   make install_gallery
   make database


make install_ceres_fc_images

Executes all of the following:
   make udpate_ceres_fc_images
   make install_database





