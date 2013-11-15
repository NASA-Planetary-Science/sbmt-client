#!/usr/bin/python

import math

# convert rotation rate from hours per revolution to radians per second
def computeRotationRate(hours):
    return 2.0 * math.pi / (hours * 3600.0)

# From spice pck
def computeRotationRateSpice(rate):
    return (rate / 86400.0) * (math.pi / 180.0)


print "tempel 1 ", computeRotationRateSpice(212.064)
print "geographos ", computeRotationRate(5.223)
print "ky26 ", computeRotationRate(0.1784)
print "bacchus ", computeRotationRate(0.6208*24.0)
print "toutatis ", computeRotationRate(176.0)
print "castalia ", computeRotationRate(4.095)
print "52760 ", computeRotationRate(14.98)
print "golevka ", computeRotationRate(6.026)
print "kleopatra ", computeRotationRate(5.385)
print "amalthea ", computeRotationRateSpice(722.6314560)
print "janus ", computeRotationRateSpice(518.2359876)
print "epimetheus ", computeRotationRateSpice(518.4907239)
print "prometheus ", computeRotationRateSpice(587.289000)
print "pandora ", computeRotationRateSpice(572.7891000)
print "larissa ", computeRotationRateSpice(649.0534470)
print "proteus ", computeRotationRateSpice(320.7654228)
print "halley ", computeRotationRate(52.8)
print "wild2 ", computeRotationRate(13.5)
