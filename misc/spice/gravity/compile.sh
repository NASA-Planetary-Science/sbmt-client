#!/bin/sh

cd `dirname $0`

g++ -O3 *.cpp -I. -o gravity
