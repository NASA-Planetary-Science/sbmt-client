#!/bin/bash

# make all of the trees for the different phases
#prelim     X -> Dec 2018
#detailed   March 2019 - June 9 2019
#Orb B      June 10 2019 - Sept 10 2019
#Recon      Sept 11 2019 -> Y


./generate_ola_hypertree.sh preliminary 2018-10-01T00:00:00 2018-12-31T00:00:00
./generate_ola_hypertree.sh detailed 2019-03-01T00:00:00 2019-06-09T00:00:00
./generate_ola_hypertree.sh orbB 2019-06-10T00:00:00 2019-09-10T00:00:00
./generate_ola_hypertree.sh reconnaissance 2019-09-10T00:00:00 2020-01-01T00:00:00
./generate_ola_hypertree.sh default 2018-10-01T00:00:00 2020-01-01T00:00:00

