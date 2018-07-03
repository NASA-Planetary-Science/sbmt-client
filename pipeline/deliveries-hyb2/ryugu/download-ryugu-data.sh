#!/bin/bash
SOURCEROOT=sbmt@hyb2sbmt.u-aizu.ac.jp:/mnt/TD1
TARGETDIR=/project/sbmtpipeline/deliveries-hyb2/ryugu
TARGETROOT="$TARGETDIR"/latest
theDate=`date +"%Y%m%d-%H%M%S-%Z"`
theHost=`hostname`
shortHost=`echo $theHost | sed 's/.jhuapl.edu//'`
logFile=/project/sbmtpipeline/deliveries-hyb2/ryugu/logs/download-ryugu-data."$theDate"."$shortHost".log
mkdir -p "$TARGETDIR"/logs
# echo "target dir is $TARGETDIR"
# echo "source root is $SOURCEROOT"
# echo "target root is $TARGETROOT"
# echo "log file is $logFile"
# echo "host file is $theHost"
# echo "shortHost  $shortHost"

# download all ryugu data from the Aizu server
echo `date` ": starting ryugu cronjob..."
echo `date` >>$logFile
echo "Starting to download latest Ryugu data from sbmt@hyb2sbmt.u-aizu.ac.jp:/mnt/TD1..." >>$logFile
echo "ReadMe file:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/00README.md $TARGETROOT  >> $logFile 2>&1
echo "documents:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/documents $TARGETROOT  >> $logFile 2>&1
echo "lidar:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/lidar $TARGETROOT  >> $logFile 2>&1
echo "nirs3:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/nirs3 $TARGETROOT  >> $logFile 2>&1
echo "onc:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/onc $TARGETROOT  >> $logFile 2>&1
echo "shape:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/shape $TARGETROOT  >> $logFile 2>&1
echo "spice:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/spice $TARGETROOT  >> $logFile 2>&1
echo "tir:" >>$logFile
rsync -rltvhe ssh $SOURCEROOT/tir $TARGETROOT  >> $logFile 2>&1
echo `date` >>$logFile
echo "Finished downloading latest Ryugu data from sbmt@hyb2sbmt.u-aizu.ac.jp:/mnt/TD1!" >>$logFile
echo `date` ": finished ryugu cronjob..."
