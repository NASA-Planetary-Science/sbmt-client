#!/bin/sh

# Run this script from within this folder to compile the 2 programs

g++ -O3 gravity.cpp gravity-cheng.cpp gravity-werner.cpp platemodel.cpp -I. -o gravity
g++ -O3 elevation-slope.cpp platemodel.cpp -I. -o elevation-slope
