#!/usr/bin/python

import os
import subprocess

# Change to folder containing this script
abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)


# Compute total number on lines in provided list of files
def num_lines_in_input(inputs):
    num_lines = 0
    for path in inputs:
        f = open(path)
        num_lines += sum(1 for line in f)
        f.close()
    print "total number of lines " + str(num_lines)
    return num_lines


# Combine output into single file and also create spk input file
def combine_output():
    N = []
    met  = []
    utc  = []
    rang = []
    xsc = []
    ysc = []
    zsc = []
    xtarget = []
    ytarget = []
    ztarget = []
    for i in range(0, NUM_PROCS):
        output_file = OUTPUT + str(i)
        f = open(output_file, 'r')
        lines = f.readlines()
        j = -1 # line number
        for line in lines:
            j += 1
            fields = line.split()

            if i == 0:
                N.append(0)
                met.append(fields[1])
                utc.append(fields[2])
                rang.append(fields[3])
                xsc.append(0.0)
                ysc.append(0.0)
                zsc.append(0.0)
                xtarget.append(0.0)
                ytarget.append(0.0)
                ztarget.append(0.0)

            N[j] += int(fields[0])
            xsc[j] += float(fields[4])
            ysc[j] += float(fields[5])
            zsc[j] += float(fields[6])
            xtarget[j] += float(fields[7])
            ytarget[j] += float(fields[8])
            ztarget[j] += float(fields[9])

        f.close()

    f = open(OUTPUT + 'combines.txt', 'w')
    f2 = open(OUTPUT + 'spk_input.txt', 'w')
    numLines = len(N)
    for i in range(0, numLines):
        xsc[i] /= N[i]
        ysc[i] /= N[i]
        zsc[i] /= N[i]
        xtarget[i] /= N[i]
        ytarget[i] /= N[i]
        ztarget[i] /= N[i]

        output_line = '%s %s %s %.16e %.16e %.16e %.16e %.16e %.16e\n' % (met[i], utc[i], rang[i], xsc[i], ysc[i], zsc[i], xtarget[i], ytarget[i], ztarget[i])
        f.write(output_line)
        output_line = '%s UTC;%.16e;%.16e;%.16e;%.16e;%.16e;%.16e\n' % (utc[i].replace('T', ' '), xsc[i], ysc[i], zsc[i], 0.0, 0.0, 0.0)
        f2.write(output_line)

    f.close()
    f2.close()


def split_final_output_into_months(input_file, output_prefix):

    f = open(input_file, 'r')
    lines = f.readlines();

    f1 = open(output_prefix + '20050911_20050930.tab', 'w')
    f2 = open(output_prefix + '20051001_20051031.tab', 'w')
    f3 = open(output_prefix + '20051101_20051118.tab', 'w')

    for line in lines:
        fields = line.split()
        if fields[1].startswith("2005-09"):
            f1.write(line)
        if fields[1].startswith("2005-10"):
            f2.write(line)
        if fields[1].startswith("2005-11"):
            f3.write(line)

    f.close()
    f1.close()
    f2.close()
    f3.close()


def split_final_output_into_days():
    f = open(OUTPUT + 'combines.txt', 'r')
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
            f1 = open(OUTPUT + day + '.tab', 'w')
        f1.write(line)

    f.close()
    f1.close()


def run_lidar_min():
    number_points = num_lines_in_input(INPUT_FILES)
    #number_points = 16000000
    SIZE=number_points / NUM_PROCS
    subproc = []
    for i in range(0, NUM_PROCS):
        startId = str(i*SIZE)
        stopId = str((i+1)*SIZE)
        if i == NUM_PROCS-1:
            stopId = str(number_points)
        ii = str(i)
        command = './lidar-min-icp '+BODY+' --single-track-mode=no '+DSKFILE+' '+startId+' '+stopId+' '+KERNEL+' '+OUTPUT+ii+' '+" ".join(INPUT_FILES)+' > '+LOG+ii+' 2>&1'
        #command = './lidar-min-fit '+BODY+' '+DSKFILE+' '+startId+' '+stopId+' '+KERNEL+' '+OUTPUT+ii+' '+" ".join(INPUT_FILES)+' > '+LOG+ii+' 2>&1'
        print command
        p = subprocess.Popen(command, shell=True)
        subproc.append(p)

    for i in range(0, NUM_PROCS):
        subproc[i].wait()


def run_mkspk():
    # Create an SPK file with the spacecraft positions
    os.system("rm -f " + NEW_SPK)
    command = '/project/nearsdc/software/spice/alpha_dsk_c/exe/mkspk -setup ./pos_to_spk.setup '
    command += '-input ' + OUTPUT + 'spk_input.txt '
    command += '-output ' + NEW_SPK
    print command
    os.system(command)
    print "finished mkspk"


def run_lidar_save():
    # Then run the lidar-save-min-icp to generate the final deliverable as a single file
    command = './lidar-save-min-icp ' + KERNEL_NEW_SPK + ' ' + OUTPUT + 'final_single_file ' + OUTPUT + 'combines.txt '
    print command
    os.system(command)

    # Then break up the file into months
    split_final_output_into_months(OUTPUT + 'final_single_file', OUTPUT)


def do_all():
    run_lidar_min()

    combine_output()

#    if BODY == 'ITOKAWA':
#        run_mkspk()
#        run_lidar_save()

    split_final_output_into_days()


###########################################################################

BODY='ITOKAWA'
#DSKFILE='/project/nearsdc/data/ITOKAWA/quad512q.bds'
DSKFILE='/project/nearsdc/data/ITOKAWA/ver512q.vtk'
KERNEL='/project/nearsdc/spice-kernels/hayabusa/kernels.txt'
KERNEL_NEW_SPK='/project/nearsdc/spice-kernels/hayabusa/kernels-newspk.txt'
NEW_SPK='/project/nearsdc/spice-kernels/hayabusa/spk/hay_osbj_050911_051118_v2.bsp'


NUM_PROCS = 1
OUTPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_uf_'
INPUT1='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20050911_20050930.tab'
INPUT2='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20051001_20051031.tab'
INPUT3='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_uf2_20051101_20051118.tab'
INPUT_FILES = [INPUT1, INPUT2, INPUT3]
LOG='/tmp/icp-out-uf_'

do_all()

###########################################################################

NUM_PROCS = 1
OUTPUT='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/cdr_optimized_test_'
INPUT1='/project/nearsdc/data/ITOKAWA/LIDAR/cdr/per-day/cdr_uf2_2005-10-31.tab'
INPUT_FILES = [INPUT1]
LOG='/tmp/icp-out-test_'

#do_all()

###########################################################################

BODY='EROS'
#DSKFILE='/project/nearsdc/data/EROS/quad512q.bds'
DSKFILE='/project/nearsdc/data/EROS/ver512q.vtk'
#KERNEL='/project/nearsdc/spice-kernels/near/kernels.txt'

NUM_PROCS = 1
OUTPUT='/project/nearsdc/data/NLR/nlr_optimized_test_'
#INPUT1='/project/nearsdc/data/NLR/L00118NG-d.TAB'
#INPUT1='/project/nearsdc/data/NLR/L00174NG-b.TAB'
#INPUT1='/home/eli/projects/near/nearsdc/data/NLR/l00059nd.tab'
INPUT1='/project/nearsdc/data/NLR/all.tab'
INPUT_FILES = [INPUT1]
LOG='/tmp/icp-out-test_'

#do_all()
