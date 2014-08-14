#!/bin/sh
rm -rf output/*
nanoc
sed -i "" "s/VERSIONXXXXXX/${TODAYSDATE}/g" output/index.html output/installation.html
