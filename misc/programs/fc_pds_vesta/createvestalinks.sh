#!/bin/sh

mkdir /project/nearsdc/data/VESTA/FC/images-soft-links

find /project/dawn/data/1a_edr/fc/vesta -name "FC*.FIT" -or -name "FC*.LBL" | grep --invert-match 'old_versions' | grep --invert-match '/delme/' | grep --invert-match '.old_files' | xargs ln -s -t /project/nearsdc/data/VESTA/FC/images-soft-links

find /project/dawn/data/1b_rdr/fc/vesta -name "FC*.FIT" -or -name "FC*.LBL" | grep --invert-match 'old_versions' | grep --invert-match '/delme/' | grep --invert-match '.old_files' | xargs ln -s -t /project/nearsdc/data/VESTA/FC/images-soft-links

#Verify images-soft-links contains the images before executing the following
#rm -r /project/nearsdc/data/VESTA/FC/images
#mv /project/nearsdc/data/VESTA/FC/images-soft-links /project/nearsdc/data/VESTA/FC/images