#!/bin/sh

cd `dirname $0`

SIZE=`expr 1114386 / 8`
./lidar-min-icp 0 $SIZE > /tmp/icp-out1 &

START=`expr 1 \* $SIZE`
STOP=`expr 2 \* $SIZE`
./lidar-min-icp $START $STOP > /tmp/icp-out2 &

START=`expr 2 \* $SIZE`
STOP=`expr 3 \* $SIZE`
./lidar-min-icp $START $STOP > /tmp/icp-out3 &

START=`expr 3 \* $SIZE`
STOP=`expr 4 \* $SIZE`
./lidar-min-icp $START $STOP > /tmp/icp-out4 &

START=`expr 4 \* $SIZE`
STOP=`expr 5 \* $SIZE`
./lidar-min-icp $START $STOP > /tmp/icp-out5 &

START=`expr 5 \* $SIZE`
STOP=`expr 6 \* $SIZE`
./lidar-min-icp $START $STOP > /tmp/icp-out6 &

START=`expr 6 \* $SIZE`
STOP=`expr 7 \* $SIZE`
./lidar-min-icp $START $STOP > /tmp/icp-out7 &

START=`expr 7 \* $SIZE`
./lidar-min-icp $START 1114386 > /tmp/icp-out8 &

wait
