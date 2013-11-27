#!/usr/bin/env python

# This script tiles Eros with maplets, searches for lidar tracks within each maplet
# and optimizes all tracks to the asteroid.


import os
import sys
import subprocess
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


def runMapmakerAtLonLat(lon, lat):
    folder = os.environ["HOME"] + "/.neartool/cache/2/GASKELL/EROS/mapmaker"
    os.environ["PATH"] = folder + "/EXECUTABLES:" + os.environ["PATH"]
    if "darwin" in sys.platform:
        os.environ["DYLD_LIBRARY_PATH"] = folder + "/EXECUTABLES:"
        command = "MAPMAKERO.macosx"
    else: # assume Linux
        os.environ["DISPLAY"] = ":20"
        os.system("Xvfb :20 &") # needed to run in headless mode
        os.environ["LD_LIBRARY_PATH"] = os.environ["JAVA_HOME"]+"/jre/lib/amd64:"+os.environ["JAVA_HOME"]+"/jre/lib/amd64/xawt"
        os.environ["LD_LIBRARY_PATH"] = folder + "/EXECUTABLES:" + os.environ["LD_LIBRARY_PATH"]
        command = "MAPMAKERO.linux64"
    name="lat"+str(lat)+"_lon"+str(lon)

    p = subprocess.Popen(command, shell=True, stdin=subprocess.PIPE, env=os.environ, cwd=folder)
    arguments = name+"\n513 5.0\nL\n" + str(lat) + "," + str(lon) + "\nn\nn\nn\nn\nn\nn\n"
    print arguments
    p.communicate(arguments)
    p.wait()
    print name

    cubeFile = folder+"/OUTPUT/"+name+".cub"
    lblFile = folder+"/OUTPUT/"+name+".lbl"

    # mv these files into a separate folder
    shutil.rmtree(name, ignore_errors=True)
    os.mkdir(name)
    newCubeFile = name + "/" + name + ".cub"
    newLblFile = name + "/" + name + ".lbl"
    os.system("mv " + cubeFile + " " + newCubeFile)
    os.system("mv " + lblFile  + " " + newLblFile)
    cubeFile = newCubeFile
    lblFile = newLblFile

    # extract out the boundary of the maplet into a separate VTK so we can view the coverage of the maplets in the SBMT
    boundaryFile = name+"/"+name+"-boundary.vtk"
    command = "./convert-mapmaker-cube -vtk -boundary " + cubeFile + " " + boundaryFile
    print command
    os.system(command)

    # convert the cube file to VTK format since this format is required by the lidar-opt script
    cubeFileVtk = name+"/"+name+".vtk"
    command = "./convert-mapmaker-cube -vtk " + cubeFile + " " + cubeFileVtk
    print command
    os.system(command)

    # convert the cube file to VTK format but decimate it so we can combine all the cubes together
    decimatedFileVtk = name+"/"+name+"-decimated.vtk"
    command = "./convert-mapmaker-cube -vtk -decimate " + cubeFile + " " + decimatedFileVtk
    print command
    os.system(command)

    # get all tracks inside cube
    tracksDir = name + "/tracks"
    os.mkdir(tracksDir)
    command = "./search-lidar " + cubeFile + " 1900-01-01 2100-01-01 100 10 " + tracksDir
    print command
    os.system(command)

    # Optimize these tracks
    trackFiles = glob.glob(tracksDir + "/*.txt")
    command = "./lidar-opt.py " + cubeFileVtk + " " + " ".join(trackFiles) + " > /dev/null 2>&1"
    print command
    os.system(command)

    # tar up the folder
    command = "tar czf " + name + ".tar.gz " + name
    print command
    os.system(command)


def runMapmakerAtAllLonLat(lonLat, numJobs):
    Parallel(n_jobs=numJobs)(delayed(runMapmakerAtLonLat)(ll[0], ll[1]) for ll in lonLat)



erosModelPds = os.environ["HOME"] + "/.neartool/cache/2/EROS/ver512q.tab"
verts = loadVerticesFromPDSFile(erosModelPds)
verts = findUniformlySpacedPoints(verts, 3.0);
printPointsAsTrackFile(verts)

lonLat = vertsToLatLon(verts)

print "number of maplets to do: " + str(len(lonLat))

runMapmakerAtAllLonLat(lonLat, 8)
