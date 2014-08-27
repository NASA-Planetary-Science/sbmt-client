#!/bin/sh

gfortran -O2 -fbounds-check shape2plates.f -o shape2plates
gfortran -O2 -fbounds-check dumber.f -o dumber
