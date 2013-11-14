#!/usr/bin/env python

import glob
import os


files = glob.glob('*.py')
newExtension = ".lbl"

for f in files:
    name = os.path.splitext(f)
    command = "mv " + f + " " + name[0] + newExtension
    print command
    os.system(command)
