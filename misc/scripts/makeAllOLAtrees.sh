#!/bin/bash

# make all of the trees for the different phases
#prelim     Dec 1 2018 -> Jan 1 2019
#detailed   Jan 1 2019 - June 10 2019
#Orb B      June 10 2019 - Sept 11 2019
#Recon      Sept 11 2019 -> Jan 1 2021

DIR=`dirname "$0"`

$DIR/generate_ola_hypertree.sh preliminary 2018-12-01T00:00:00.000 2019-01-01T00:00:00.000
$DIR/generate_ola_hypertree.sh detailed 2019-01-01T00:00:00.000 2019-06-10T00:00:00.000
$DIR/generate_ola_hypertree.sh orbB 2019-06-10T00:00:00.000 2019-09-11T00:00:00.000
$DIR/generate_ola_hypertree.sh recon 2019-09-11T00:00:00.000 2021-01-01T00:00:00.000
$DIR/generate_ola_hypertree.sh default 2018-12-01T00:00:00.000 2021-01-01T00:00:00.000

