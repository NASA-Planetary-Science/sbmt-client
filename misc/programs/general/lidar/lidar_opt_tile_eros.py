#!/usr/bin/env python

# This script tiles Eros with maplets, searches for lidar tracks within each maplet
# and optimizes all tracks to the asteroid.


import os
import sys
import math
import spice
from joblib import Parallel, delayed
import shutil
import glob


def loadVerticesFromPDSFile(filename):
    """Loads vertices from specified file and returns them as a list"""
    fin = open(filename,'r')
    firstLine = fin.readline().split()
    numPoints = firstLine[0]
    verts = []
    for i in range(int(numPoints)):
        line = fin.readline()
        p = line.split()
        verts.append( (float(p[1]),float(p[2]),float(p[3])) )
    fin.close()
    return verts

def distance2BetweenPoints(p1, p2):
    return ((p2[0] - p1[0]) ** 2 +
            (p2[1] - p1[1]) ** 2 +
            (p2[2] - p1[2]) ** 2)

def isVertexNearOtherVertices(otherVertices, v, minDistance):
    """Tests whether v is within minDistance of otherVertices. If it is True
    is return, otherwise False is returned."""
    for vert in otherVertices:
        if distance2BetweenPoints(vert, v) <= (minDistance*minDistance):
            return True
    return False


def findUniformlySpacedPoints(verts, meanDistanceBetweenPoints):
    """Given a list of vertices, returns a subset of these that are all
    meanDistanceBetweenPoints apart from each other"""
    spacedPoints = []
    for v in verts:
        if isVertexNearOtherVertices(spacedPoints, v, meanDistanceBetweenPoints) == False:
            print "found new point " + str(v)
            spacedPoints.append(v)
    return spacedPoints

def printPointsAsTrackFile(verts):
    for v in verts:
        print "2000-01-01T00:00:00 " + str(v[0]) + " " + str(v[1]) + " " + str(v[2]) + " 0.0 0.0 0.0"


def vertsToLatLon(verts):
    lonLat = []
    for v in verts:
        rll = spice.reclat(v)
        lon = rll[1]*180.0/math.pi
        lat = rll[2]*180.0/math.pi
        # Change to west longitude
        if lon < 0.0:
            lon = lon + 360.0
        lon = 360.0 - lon
        if lon >= 360.0:
            lon = lon - 360.0
        ll = (lon, lat)
        lonLat.append(ll)
    # Note the list may contain duplicates so remove them
    return list(set(lonLat))


def runMapmakerAtLonLat(lon, lat, mapmakerFolder):
    name="lat"+str(lat)+"_lon"+str(lon)
    outputFolder = mapmakerFolder+"/OUTPUT/"

    command = "run_java_program.sh edu.jhuapl.near.server.RunMapmaker " + mapmakerFolder + " " + name + " 513 5.0 " + str(lat) + " " + str(lon) + " " + outputFolder
    print command
    os.system(command)

    # mv the maplet file into a separate folder
    mapletFile = mapmakerFolder+"/OUTPUT/"+name+".FIT"
    shutil.rmtree(name, ignore_errors=True)
    os.mkdir(name)
    newMapletFile = name + "/" + name + ".FIT"
    os.system("mv " + mapletFile + " " + newMapletFile)
    mapletFile = newMapletFile

    # extract out the boundary of the maplet into a separate VTK so we can view the coverage of the maplets in the SBMT
    boundaryFile = name+"/"+name+"-boundary.vtk"
    command = "run_java_program.sh edu.jhuapl.near.server.ConvertMaplet -vtk -boundary " + mapletFile + " " + boundaryFile
    print command
    os.system(command)

    # convert the maplet file to VTK format since this format is required by the lidar-opt script
    mapletFileVtk = name+"/"+name+".vtk"
    command = "run_java_program.sh edu.jhuapl.near.server.ConvertMaplet -vtk " + mapletFile + " " + mapletFileVtk
    print command
    os.system(command)

    # convert the maplet file to VTK format but decimate it so we can combine all the maplets together
    decimatedFileVtk = name+"/"+name+"-decimated.vtk"
    command = "run_java_program.sh edu.jhuapl.near.server.ConvertMaplet -vtk -decimate " + mapletFile + " " + decimatedFileVtk
    print command
    os.system(command)

    # get all tracks inside the maplet
    tracksDir = name + "/tracks"
    os.mkdir(tracksDir)
    command = "run_java_program.sh edu.jhuapl.near.server.SearchLidarDataInsideMaplet " + mapletFile + " 1900-01-01 2100-01-01 100 10 1 " + tracksDir
    print command
    os.system(command)

    # Optimize these tracks
    trackFiles = glob.glob(tracksDir + "/*.txt")
    command = "lidar-opt.py " + mapletFileVtk + " " + " ".join(trackFiles) + " > /dev/null 2>&1"
    print command
    os.system(command)

    # tar up the folder
    command = "tar czf " + name + ".tar.gz " + name
    print command
    os.system(command)


def runMapmakerAtAllLonLat(lonLat, numJobs, mapmakerFolder):
    Parallel(n_jobs=numJobs)(delayed(runMapmakerAtLonLat)(ll[0], ll[1], mapmakerFolder) for ll in lonLat)


def runMain():
    mapmakerFolder = os.environ["HOME"] + "/.neartool/cache/2/GASKELL/EROS/mapmaker"
    os.environ["PATH"] = os.environ["SBMT_ROOT"] + "/misc/programs/general/lidar:" + os.environ["PATH"]
    os.environ["PATH"] = os.environ["SBMT_ROOT"] + "/misc/programs/general/build:" + os.environ["PATH"]
    os.environ["PATH"] = os.environ["SBMT_ROOT"] + "/misc/scripts:" + os.environ["PATH"]
    if "linux" in sys.platform:
        os.environ["LD_LIBRARY_PATH"] = os.environ["JAVA_HOME"]+"/jre/lib/amd64:"+os.environ["JAVA_HOME"]+"/jre/lib/amd64/xawt"


    erosModelPds = os.environ["HOME"] + "/.neartool/cache/2/EROS/ver512q.tab"
    verts = loadVerticesFromPDSFile(erosModelPds)
    verts = findUniformlySpacedPoints(verts, 3.0);
    printPointsAsTrackFile(verts)

    lonLat = vertsToLatLon(verts)

    # Note we need to manually add a point to fully cover the asteroid since
    # the above scheme leaves some wholes
    lonLat.append((360.0-187.304, 2.061))

    print "number of maplets to do: " + str(len(lonLat))

    runMapmakerAtAllLonLat(lonLat, 8, mapmakerFolder)


if __name__ == '__main__':
    runMain()
