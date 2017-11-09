import sys
import re
import datetime
import os

# This function is a helper function for stitching together
# multiple time history CSVs.  If there are breaks in the time history,
# the timeHistory function in C will not be able to construct the CSV.
# however, if you construct multiple CSVs with multiple continuous intervals,
# you will be able to stitch them together using stitchTimeCSVs.py.  This
# function will help create those CSVs to be stitched together.  This
# function assumes that the data is broken at a specific interval. For
# example, we want an interval from Jan 20 to Jan 25 2017 but there is
# data missing every morning between 5am and 5:15am.
# Inputs:
#     beginDate = first date in the full interval
#     endDate = last date in the full interval
#     beginTime = beginning time of each continuous interval
#     endTime = end time of each continuous interval
#     numDays = number of days in continuous interval
#     bodyName = name of body as it would be input in timeHistory script
#     spacecraftName = name of spacecraft as it would be input in timeHistory script
#     metaKernel = file name of SPICE metakernel
# for the example described above, the inputs would be
#     CSVsForBrokenInterval.py 2017-01-20 2017-01-25 5:15:00.000 5:00:00.000 1 RYUGU HAYABUSA2 /project/sbmtpipeline/rawdata/ryugu/truth/spice/kernels/mk/hyb2_lss_truth.tm
def CSVsForBrokenInterval(beginDate, endDate, beginTime, endTime, numDays, bodyName, spacecraftName, metaKernel):
	base_command = '/project/sbmtpipeline/timeHistory/timeHistory ' + bodyName + ' ' + spacecraftName + ' ' + metaKernel + ' '


	# convert to datetime and find total length of interval
	startDateTime = datetime.datetime.strptime(beginDate, "%Y-%m-%d")
	# endDateTime   = datetime.datetime.strptime(endDate+" "+endTime, "%Y-%m-%d %H:%M:%S.%f")
	endDateTime   = datetime.datetime.strptime(endDate, "%Y-%m-%d")

	totalDays = (endDateTime - startDateTime).days;
	breakTime = (datetime.datetime.strptime(endDate+" "+beginTime, "%Y-%m-%d %H:%M:%S.%f") - datetime.datetime.strptime(endDate+" "+endTime, "%Y-%m-%d %H:%M:%S.%f"))


	startDateTime = datetime.datetime.strptime(beginDate+" "+beginTime, "%Y-%m-%d %H:%M:%S.%f")

	numDays = int(numDays)

	# make temp folder for csvs
	os.system('mkdir temp_CSVs')

	day = startDateTime;
	for iDay in range(0, 4):
		endDay = iDay + numDays
		currDateTime = startDateTime + datetime.timedelta(days=iDay)
		beginDateArg = currDateTime.strftime("%Y-%m-%dT%H:%M:%S.%f")
		endDateTimeA = startDateTime + datetime.timedelta(days=endDay)
		endDateTimeA = endDateTimeA - breakTime
		endDateArg   = endDateTimeA.strftime("%Y-%m-%dT%H:%M:%S.%f")
		command = base_command + beginDateArg + " " + endDateArg;

		# run time history for this interval
		os.system(command)

		# move *_timeHistory.csv to temp folder and rename with dates
		beginDateStr = str(currDateTime.day);
		endDateStr = str(endDateTimeA.day);
		csvName = spacecraftName + '_' + bodyName + '_timeHistory'
		mvCommand = 'mv ' + csvName + '.csv temp_CSVs/' + csvName + beginDateStr + '_' + endDateStr + '.csv '
		os.system(mvCommand)





if __name__ == "__main__":
	if len(sys.argv) != 9:
		print 'error! you need to provide the beginDate, endDate, beginTime, endTime, of the interval and the number of days in each continuous interval (see description in comments)'
		exit(0)
	beginDate = sys.argv[1]
	endDate = sys.argv[2]
	beginTime = sys.argv[3]
	endTime = sys.argv[4]
	numDays = sys.argv[5]
	bodyName = sys.argv[6]
	spacecraftName = sys.argv[7]
	metaKernel = sys.argv[8]


	CSVsForBrokenInterval(beginDate, endDate, beginTime, endTime, numDays, bodyName, spacecraftName, metaKernel)
