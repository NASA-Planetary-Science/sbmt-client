#!/usr/bin/python

f1 = open('dawn_searchresults.csv', 'r')
f2 = open('fitfilelist.txt', 'r')

lines1 = f1.readlines();
lines2 = f2.readlines();

for line1 in lines1:
    found = 0
    vals = line1.split(',')
    s1 = vals[0]
    for line2 in lines2:
        s2 = line2.rstrip("\n")
        if s1 == s2:
            found = 1
            break
    if found == 0 and not s1.endswith(".fits"):
        print s1 + " " + vals[-1] + "",
