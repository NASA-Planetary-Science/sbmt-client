#!/bin/bash

# make all of the trees for the different phases
#prelim     X -> Dec 2018
#detailed   March 2019 - June 9 2019
#Orb B      June 10 2019 - Sept 10 2019
#Recon      Sept 11 2019 -> Y


./generate_ola_hypertree.sh preliminary 2000-01-01T00:00:00.000 2019-01-01T00:00:00.000
./generate_ola_hypertree.sh detailed 2019-03-01T00:00:00.000 2019-06-10T00:00:00.000
./generate_ola_hypertree.sh orbB 2019-06-10T00:00:00.000 2019-09-11T00:00:00.000
./generate_ola_hypertree.sh recon 2019-09-11T00:00:00.000 2050-01-01T00:00:00.000
./generate_ola_hypertree.sh default 2000-01-01T00:00:00.000 2050-01-01T00:00:00.000

