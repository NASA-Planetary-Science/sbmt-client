#!/usr/bin/python

import os


#os.system("cat cdr_uf_20050911_20050930.tab cdr_uf_20051001_20051031.tab cdr_uf_20051101_20051118.tab | sort -k 1,1n | uniq -w 34 > /tmp/cdr_all.tab")
#os.system("cat cdr_uf2_20050911_20050930.tab cdr_uf2_20051001_20051031.tab cdr_uf2_20051101_20051118.tab | sort -k 1,1n | uniq -w 34 > /tmp/cdr_all.tab")
#os.system("cat cdr_f_20050911_20050930.tab cdr_f_20051001_20051031.tab cdr_f_20051101_20051118.tab | sort -k 1,1n | uniq -w 34 > /tmp/cdr_all.tab")
os.system("cat cdr_optimized2_20050911_20050930.tab cdr_optimized2_20051001_20051031.tab cdr_optimized2_20051101_20051118.tab | sort -k 1,1n | uniq -w 34 > /tmp/cdr_all.tab")

f = open('/tmp/cdr_all.tab', 'r')
lines = f.readlines();

daysset = set([])
f1 = None

for line in lines:
    fields = line.split()
    day = fields[1][0:10]
    if day not in daysset:
        daysset.add(day)
        print day
        if f1 != None:
            f1.close()
#        f1 = open('cdr_uf_' + day + '.tab', 'w')
#        f1 = open('cdr_uf2_' + day + '.tab', 'w')
#        f1 = open('cdr_f_' + day + '.tab', 'w')
        f1 = open('cdr_optimized2_' + day + '.tab', 'w')
    f1.write(line)

f.close()
f1.close()

os.system("rm -f /tmp/cdr_all.tab")
