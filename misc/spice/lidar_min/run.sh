#!/bin/sh

export DISPLAY=":20"
Xvfb $DISPLAY &

cd `dirname $0`

#NUMBER_POINTS=1114386
NUMBER_POINTS=1248998
#NUMBER_POINTS=1636893

SIZE=`expr $NUMBER_POINTS / 8`
./lidar-min-icp 0 $SIZE > ~/icp-out-run5/icp-out1 2>&1 &

START=`expr 1 \* $SIZE`
STOP=`expr 2 \* $SIZE`
./lidar-min-icp $START $STOP > ~/icp-out-run5/icp-out2 2>&1 &

START=`expr 2 \* $SIZE`
STOP=`expr 3 \* $SIZE`
./lidar-min-icp $START $STOP > ~/icp-out-run5/icp-out3 2>&1 &

START=`expr 3 \* $SIZE`
STOP=`expr 4 \* $SIZE`
./lidar-min-icp $START $STOP > ~/icp-out-run5/icp-out4 2>&1 &

START=`expr 4 \* $SIZE`
STOP=`expr 5 \* $SIZE`
./lidar-min-icp $START $STOP > ~/icp-out-run5/icp-out5 2>&1 &

START=`expr 5 \* $SIZE`
STOP=`expr 6 \* $SIZE`
./lidar-min-icp $START $STOP > ~/icp-out-run5/icp-out6 2>&1 &

START=`expr 6 \* $SIZE`
STOP=`expr 7 \* $SIZE`
./lidar-min-icp $START $STOP > ~/icp-out-run5/icp-out7 2>&1 &

START=`expr 7 \* $SIZE`
./lidar-min-icp $START $NUMBER_POINTS > ~/icp-out-run5/icp-out8 2>&1 &

wait
