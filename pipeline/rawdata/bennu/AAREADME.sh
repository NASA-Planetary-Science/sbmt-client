#! /bin/sh
# James Peachey imported 20220316/OLAv21 on 2022-06-24.
#
# Started with the following by hand:
#
# cd /project/sbmtpipeline/rawdata/bennu
# mkdir redmine-2418
# cp redmine-2134/AAREADME.sh redmine-2418/
#
# That produced the initial version of this document/script.
# Edited this by hand to make it work for this delivery instead of
# the one processed for redmine-2134.
#
#-------------------------------------------------------------------------------
deliveredModelName="OLAv21"
deliveredVersion="20220316"
processingModelDirectory="ola-v21-spc"
processingVersion="redmine-2418"
processingModelName="OLA-v21"
#-------------------------------------------------------------------------------

if test `whoami` != sbmt; then
  echo "Run this script while logged into the sbmt account." >&2
  exit 1
fi

# Check for existence of code build area to restart following the git step.
if test -f /project/sbmtpipeline/rawdata/bennu/$processingVersion/.checkout-succeeded; then
  echo "Skipping checkout steps -- looks like they were completed already."
else

  # Initial steps.
  # Build a local copy of the tools.
  cd /project/sbmtpipeline/rawdata/bennu/$processingVersion

  # Make directory for the log files.
  if test ! -d logs; then
    mkdir -p logs
  fi

  git clone http://hardin:8080/scm/git/vtk/saavtk --branch saavtk1dev-redmine-2418 >> logs/git-clone-saavtk-log 2>&1
  if test $? -ne 0; then
    echo "Problem with git saavtk checkout" >&2
    exit 1
  fi
  git clone http://hardin:8080/scm/git/sbmt --branch sbmt1dev-redmine-2418 >> logs/git-clone-sbmt-log 2>&1
  if test $? -ne 0; then
    echo "Problem with git sbmt checkout" >&2
    exit 1
  fi
  touch /project/sbmtpipeline/rawdata/bennu/$processingVersion/.checkout-succeeded
fi

export SAAVTKROOT=/project/sbmtpipeline/rawdata/bennu/$processingVersion/saavtk
export SBMTROOT=/project/sbmtpipeline/rawdata/bennu/$processingVersion/sbmt
export JAVA_HOME=/project/nearsdc/software/java/jdk16/linux64

if test -f /project/sbmtpipeline/rawdata/bennu/$processingVersion/.build-succeeded; then
  echo "Skipping build steps -- looks like they were completed already."
else

  $SBMTROOT/misc/scripts/set-released-mission.sh TEST_APL_INTERNAL
  if test $? -ne 0; then
    echo "Problem setting released mission" >&2
    exit 1
  fi

  cd /project/sbmtpipeline/rawdata/bennu/$processingVersion/sbmt
  make clean > /dev/null 2>&1
  make release >> make-release.log 2>&1
  if test $? -ne 0; then
    echo "Problem with make release." >&2
    exit 1
  fi
  touch /project/sbmtpipeline/rawdata/bennu/$processingVersion/.build-succeeded
fi

# Check for existence of rawdata model area to restart following the build step.
if test -f /project/sbmtpipeline/rawdata/bennu/$processingVersion/.modelDelivery2rawdata-succeeded; then
  echo "Skipping copying delivery to rawdata -- looks like it was already done."
else

  # Delivered to rawdata.
  cd /project/sbmtpipeline/rawdata/bennu/$processingVersion
  $SBMTROOT/pipeline/rawdata/bennu/modelDelivery2rawdata-bennu.sh $deliveredModelName $deliveredVersion $processingModelDirectory $processingVersion
  if test $? -ne 0; then
    echo "Problem with delivery-to-rawdata step." >&2
    exit 1
  fi


#   # ************************** OREX IMAGING -> POLYCAM HACK **************************
#   if test -d $processingModelDirectory/imaging; then
#     echo "Moving imaging to polycam"
#     rm -rf $processingModelDirectory/polycam
#     mv $processingModelDirectory/imaging $processingModelDirectory/polycam
#   fi
#   # ************************** END OREX IMAGING -> POLYCAM HACK **************************
# 
#   # ************************** OREX SUMFILES -> sumfiles HACK **************************
#   if test -d $processingModelDirectory/polycam/SUMFILES; then
#     echo "Moving polycam/SUMFILES to polycam/sumfiles"
#     rm -rf $processingModelDirectory/polycam/sumfiles
#     mv $processingModelDirectory/polycam/SUMFILES $processingModelDirectory/polycam/sumfiles
#   fi
#   # ************************** END OREX SUMFILES -> sumfiles HACK **************************

  touch /project/sbmtpipeline/rawdata/bennu/$processingVersion/.modelDelivery2rawdata-succeeded
fi

if test -f /project/sbmtpipeline/rawdata/bennu/$processingVersion/.modelRawdata2processed-succeeded; then
  echo "Skipping copying rawdata to processed -- looks like it was already done."
else

  # Rawdata to processed.
  cd /project/sbmtpipeline/rawdata/bennu/$processingVersion
  $SBMTROOT/pipeline/rawdata/bennu/modelRawdata2processed-bennu.sh $processingModelDirectory $processingVersion $processingModelName
  if test $? -ne 0; then
    echo "Problem with rawdata-to-processed step." >&2
    exit 1
  fi

  echo "Looks OK -- check it over and then restart this script."
  touch /project/sbmtpipeline/rawdata/bennu/$processingVersion/.modelRawdata2processed-succeeded
  exit 0

fi

# if test x = y; then

# Deploy.
cd /project/sbmtpipeline/rawdata/bennu/$processingVersion
$SBMTROOT/pipeline/rawdata/bennu/modelProcessed2deployed-bennu.sh $processingModelDirectory $processingVersion
if test $? -ne 0; then
  echo "Problem with processed-to-deployed step." >&2
  exit 1
fi

# Used the following to infer correspondence of arbitrary model names to shape model resolutions.
# The file list is in order of increasing file size.
cd /project/sbmt2/sbmt/data/bodies/bennu/$processingModelDirectory-$processingVersion/shape
let res=0
for file in `ls -Sr *.obj.gz | grep -v shape`; do
  rm -f shape${res}.obj.gz
  ln -s $file shape${res}.obj.gz
  let res=res+1
done

# Next updated the link in the test "server" area:
cd /project/sbmt2/sbmt/data/servers/multi-mission/test/bennu
rm -f $processingModelDirectory; ln -s ../../../../bodies/bennu/$processingModelDirectory-$processingVersion $processingModelDirectory

# Next updated the link in the top level test area used by the database generator:
cd /project/sbmt2/sbmt/data/bodies/bennu
rm -f $processingModelDirectory; ln -s $processingModelDirectory-$processingVersion $processingModelDirectory

# fi

# Update database tables.
cd /project/sbmtpipeline/rawdata/bennu/$processingVersion

# if test x = y; then

$SBMTROOT/pipeline/rawdata/bennu/runDBGenerator-bennu.sh mapcam $processingModelName $processingModelDirectory $processingVersion GASKELL >> logs/runDBGenerator-bennu-mapcam-log 2>&1
if test $? -ne 0; then
  echo "Problem with creating MAPCAM database table." >&2
  exit 1
fi
mv logs/DatabaseGeneratorSql.log logs/DatabaseGeneratorSql-mapcam.log

$SBMTROOT/pipeline/rawdata/bennu/runDBGenerator-bennu.sh polycam $processingModelName $processingModelDirectory $processingVersion GASKELL >> logs/runDBGenerator-bennu-polycam-log 2>&1
if test $? -ne 0; then
  echo "Problem with creating POLYCAM database table." >&2
  exit 1
fi
mv logs/DatabaseGeneratorSql.log logs/DatabaseGeneratorSql-polycam.log

# fi

# Ready to test using a client launched with a test config.
