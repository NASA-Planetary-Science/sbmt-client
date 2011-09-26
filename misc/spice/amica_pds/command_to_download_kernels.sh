# use this command to download the spice kernel files needed to run create_info_files into the current directory
lftp -e "mirror --delete --only-newer --verbose pub/naif/pds/data/hay-a-spice-6-v1.0/haysp_1000/data/ ." ftp://naif.jpl.nasa.gov
