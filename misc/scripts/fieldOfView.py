#!/usr/bin/python

import math

def computeFOV(focalLengthInPixels, sensorSize):

    fov = 2.0 * math.atan(sensorSize/(2.0*focalLengthInPixels))

    print "FOV radians: " + str(fov)

    fov = fov * 180.0 / math.pi
    print "FOV degrees: " + str(fov)

    return fov



#pixelSize = 13.5
#sensorSize_ = numPixels * pixelSize
#computeFOV(717320, sensorSize_)
#computeFOV(700000, sensorSize_)
#computeFOV(135540, sensorSize_)
#computeFOV(140000, sensorSize_)

# MEX HRSC/SRC
print "\nMEX HRSC/SRC"
numPixels = 1024
flmm = 988.6
pxmm = 1000.0/9
computeFOV(flmm*pxmm, numPixels)

# Lutetia OSIRIS NAC
numPixels = 2048.0
print "\nLutetia OSIRIS NAC"
flmm = 0.7173200000e03
pxmm = 74.07410
computeFOV(flmm*pxmm, numPixels)

# Lutetia OSIRIS WAC
print "\nLutetia OSIRIS WAC"
flmm = 0.1355400000e03
pxmm = 74.07410
computeFOV(flmm*pxmm, numPixels)

# Phobos Viking
print "\nPhobos Viking"
flmm = 0.4750000000e03
pxmm = 80.0
numPixels = 1204.
computeFOV(flmm*pxmm, numPixels)
numPixels = 1056.
computeFOV(flmm*pxmm, numPixels)

# Phobos2 Filter 2
print "\nPhobos2 Filter 2"
flmm = 0.1000000000e03
pxmm = 55.55600
numPixels = 510.
computeFOV(flmm*pxmm, numPixels)
pxmm = 41.66700
numPixels = 298.
computeFOV(flmm*pxmm, numPixels)

# Phobos2 Filter 1 and 3
print "\nPhobos2 Filter 1 and 3"
flmm = 0.1850000000e02
pxmm = 55.55600
numPixels = 510.
computeFOV(flmm*pxmm, numPixels)
pxmm = 41.66700
numPixels = 298.
computeFOV(flmm*pxmm, numPixels)
