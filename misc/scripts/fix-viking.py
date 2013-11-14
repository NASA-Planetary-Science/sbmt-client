#!/usr/bin/env python

import glob
import os


files = glob.glob('VO*.FIT')
#newExtension = ".lbl"

for f in files:
    name = os.path.splitext(f)
    newName = "f" +
    command = "mv " + f + " " + name[0] + ne
    print command
    os.system(command)
