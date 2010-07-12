#!/bin/bash

DIR=/disks/dg007/near/kahneg1/code/code

ssh thedummy   $DIR/nohup.sh $DIR/generate_msi_footprints_multires.sh 00 01 02 03
ssh drdeath    $DIR/nohup.sh $DIR/generate_msi_footprints_multires.sh 04 05 06 07
ssh copperhead $DIR/nohup.sh $DIR/generate_msi_footprints_multires.sh 08 09 10 11
ssh joechill   $DIR/nohup.sh $DIR/generate_msi_footprints_multires.sh 12 13 14 15
