#!/usr/bin/python

f1 = open('sumfilelist.txt', 'r')
f2 = open('fitfilelist.txt', 'r')

lines1 = f1.readlines();
lines2 = f2.readlines();

for line1 in lines1:
    for line2 in lines2:
        s1 = line1[8:12]
        s2 = line2[8:12]
        if s1 == s2:
            f3 = open('sumfiles/' + line1.rstrip("\n"), 'r')
            lines3 = f3.readlines();
            print line2.rstrip("\n") + " " + lines3[1],
            break
