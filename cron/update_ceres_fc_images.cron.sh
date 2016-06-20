#!/bin/bash
SBMTROOT=/project/sbmtpipeline/sbmt ; export SBMTROOT
theDate=`date +"%Y%m%d-%H%M%S-%Z"`
theHost=`hostname`
shortHost=`echo $theHost | sed 's/.jhuapl.edu//'`
#logFile="$SBMTROOT"/cron/logs/update_ceres_fc_images.cron."$theDate"."$shortHost".log
logFile="$SBMTROOT"/cron/logs/install_ceres_fc_images.cron."$theDate"."$shortHost".log
mkdir -p "$SBMTROOT"/cron/logs
#echo "log file is $logFile"
#echo "host file is $theHost"
#echo "shortHost  $shortHost"
cd $SBMTROOT
# update_ceres_fc_images modifies the beta tables.
#/usr/bin/make update_ceres_fc_images > $logFile 2>&1 &
# install_ceres_fc_images modifies the main tables.
/usr/bin/make install_ceres_fc_images > $logFile 2>&1 &
