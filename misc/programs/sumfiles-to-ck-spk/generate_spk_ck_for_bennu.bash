#!/bin/bash
#Script for generating SPKs and CKs with sumfiles
#will check if ck or spk exists and will remove
#Requires to have built process_sumfiles and spice toolkit
#Need to make sure path to process_sumfiles and spice toolit is correct
#Make sure flipX, flipY, flipZ are set correctly
#Make sure you have msopcksetup and mkspksetup setup correct (look at README.txt in process_sumfiles program)


#Set paths
SPICE='../spice_toolkit/cspice'
PROCESS_SUMFILES='../sumfiles-to-ck-spk-4.0.0'

#Match flip defined in make_sumfiles.txt
flipX='true'
flipY='true'
flipZ='false'

#msopcksetup and mkspksetup
msopcksetup=$PROCESS_SUMFILES'/msopcksetup_BENNU'
mkspksetup=$PROCESS_SUMFILES'/mkspksetup_BENNU'

#path to metakernal
kernels='kernels.txt'

#sumfile_list for mapcam and polycam
sumfilelist_mapcam='sumfilelist_mapcam.txt'
sumfilelist_polycam='sumfilelist_polycam.txt'



if [[ $# -lt 5 ]]; then
  echo 'Purpose: To build spks and sumfiles from sumfiles - read program comments for detail'
  echo -e "Usage: \t$0  <mapcamck> <mapcamspk> <polycamck> <polycamspk> <make_sumfiles.in> "
  echo "Example:"
  echo -e "  \t$0 ors_s_20171120_20190310_mapcaspc_test_v1.bc ors_s_20171120_20190310_mapcaspc_test_v1.bsp ors_s_20181109_20190122_polycspc_test_v1.bc ors_s_20181109_20190122_polycspc_test_v1.bsp ../mksum/make_sumfiles.in.org " 
  exit 0
fi


mapcamck=$1
mapcamspk=$2
polycamck=$3
polycamspk=$4
make_sumfile=$5

# removing past spk and cks

if [ -e  $mapcamck ]
 then 
  rm $mapcamck
fi

if [ -e  $mapcamspk ]
 then 
  rm $mapcamspk
fi

if [ -e  $polycamck ]
 then 
  rm $polycamck
fi

if [ -e  $polycamspk ]
 then 
  rm $polycamspk
fi

##########
# MapCam #
##########

# Produce input data files for Spice mkspk and msopck

echo "$PROCESS_SUMFILES/process_sumfiles $kernels $sumfilelist_mapcam ORX_OCAMS_MAPCAM ORX_SPACECRAFT $flipX $flipY $flipZ"
$PROCESS_SUMFILES/process_sumfiles $kernels $sumfilelist_mapcam ORX_OCAMS_MAPCAM ORX_SPACECRAFT $flipX $flipY $flipZ  > process_sumfiles.log

# Run Spice ck generator (SPICE TOOLKIT need to be built)

echo "$SPICE/exe/msopck $msopcksetup msopckinputdata $mapcamck"
$SPICE/exe/msopck $msopcksetup msopckinputdata $mapcamck
mv msopckinputdata mapcam_msopckinputdata.txt

# Run Spice spk generator (SPICE TOOLKIT need to be built)

$SPICE/exe/mkspk -setup $mkspksetup -input mkspkinputdata -output $mapcamspk
mv mkspkinputdata mapcam_mkspkinputdata.txt 

# Prepare text file with images used

time=`echo "$mapcamspk" | cut -d'.' -f1`'.txt'
awk 'BEGIN{FS="/"}{print $NF}' process_sumfiles.log > mappictlist
awk 'BEGIN{FS=".SUM"}{print $1 $2}' mappictlist > mappictlist1
awk 'NR==FNR {a[$1]=$0;next}; $1 in a  {print a[$1],$0}'  $make_sumfile mappictlist1 | awk '{print $(NF-2)"_MCAM_L0b_V005.fits "$NF" "$(NF-1)".SUM"}' >$time

###########
# PolyCam #
###########

# Produce input data files for Spice mkspk and msopck

echo "$PROCESS_SUMFILES/process_sumfiles $kernels $sumfilelist_polycam ORX_OCAMS_POLYCAM ORX_SPACECRAFT $flipX $flipY $flipZ"
$PROCESS_SUMFILES/process_sumfiles $kernels $sumfilelist_polycam ORX_OCAMS_MAPCAM ORX_SPACECRAFT $flipX $flipY $flipZ  > process_sumfiles.log

# Run Spice ck generator (SPICE TOOLKIT need to be built)

echo "$SPICE/exe/msopck $msopcksetup msopckinputdata $polycamck"
$SPICE/exe/msopck $msopcksetup msopckinputdata $polycamck
mv msopckinputdata polycam_msopckinputdata.txt

# Run Spice spk generator (SPICE TOOLKIT need to be built)

$SPICE/exe/mkspk -setup $mkspksetup -input mkspkinputdata -output $polycamspk
mv mkspkinputdata polycam_mkspkinputdata.txt 

time=`echo "$polycamspk" | cut -d'.' -f1`'.txt'
awk 'BEGIN{FS="/"}{print $NF}' process_sumfiles.log > polypictlist
awk 'BEGIN{FS=".SUM"}{print $1 $2}' polypictlist > polypictlist1
awk 'NR==FNR {a[$1]=$0;next}; $1 in a  {print a[$1],$0}'  $make_sumfile polypictlist1 | awk '{print $(NF-2)"_MCAM_L0b_V005.fits "$NF" "$(NF-1)".SUM"}' >$time
