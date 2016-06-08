#!/usr/bin/python

f1 = open('sumfilelist.txt', 'r')
f2 = open('fitfilelist.txt', 'r')

lines1 = f1.readlines();
lines2 = f2.readlines();

for line1 in lines1:
    found = 0
    for line2 in lines2:
        s1 = line1[8:12]
        s2 = line2[8:12]
        if s1 == s2:
            found = 1
            break
    if found == 0:
        print line1,
