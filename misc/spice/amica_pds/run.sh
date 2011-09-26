#!/bin/sh

./create_info_files kernels.txt /project/nearsdc/data/ITOKAWA/AMICA/fitfilelist.txt

# move the created INFO files to their own folder
mkdir -p /project/nearsdc/data/ITOKAWA/AMICA/infofiles
rm -rf /project/nearsdc/data/ITOKAWA/AMICA/infofiles/*.INFO
mv /project/nearsdc/data/ITOKAWA/AMICA/images/*.INFO /project/nearsdc/data/ITOKAWA/AMICA/infofiles
