#!/bin/sh
rm -rf output/*
nanoc
sed -i "" "s/VERSIONXXXXXX/${TODAYSDATE}/g" output/index.html output/installation.html
scp -r output/* hurlbut:/usr/apache/htdocs-sbmt/internal
