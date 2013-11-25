#!/usr/bin/env python

# This script tiles Eros with maplets, searches for lidar tracks within each maplet
# and optimizes all tracks to the asteroid.


import os
import sys
import subprocess
from euclid import Vector3
import math
import spice
from joblib import Parallel, delayed
import shutil
import glob


def icosahedron():
    """Construct a 20-sided polyhedron"""
    X = 0.525731112119133606
    Z = 0.850650808352039932

    verts = [ \
             (-X, 0.0, Z),
             (X, 0.0, Z),
             (-X, 0.0, -Z),
             (X, 0.0, -Z),
             (0.0, Z, X),
             (0.0, Z, -X),
             (0.0, -Z, X),
             (0.0, -Z, -X),
             (Z, X, 0.0),
             (-Z, X, 0.0),
             (Z, -X, 0.0),
             (-Z, -X, 0.0) ]
    faces = [ \
             (0, 4, 1),
             (0, 9, 4),
             (9, 5, 4),
             (4, 5, 8),
             (4, 8, 1),
             (8, 10, 1),
             (8, 3, 10),
             (5, 3, 8),
             (5, 2, 3),
             (2, 7, 3),
             (7, 10, 3),
             (7, 6, 10),
             (7, 11, 6),
             (11, 0, 6),
             (0, 1, 6),
             (6, 1, 10),
             (9, 0, 11),
             (9, 11, 2),
             (9, 2, 5),
             (7, 2, 11) ]
    return verts, faces


def subdivide(verts, faces):
    """Subdivide each triangle into four triangles, pushing verts to the unit sphere"""
    triangles = len(faces)
    for faceIndex in xrange(triangles):

        # Create three new verts at the midpoints of each edge:
        face = faces[faceIndex]
        a,b,c = (Vector3(*verts[vertIndex]) for vertIndex in face)
        verts.append((a + b).normalized()[:])
        verts.append((b + c).normalized()[:])
        verts.append((a + c).normalized()[:])

        # Split the current triangle into four smaller triangles:
        i = len(verts) - 3
        j, k = i+1, i+2
        faces.append((i, j, k))
        faces.append((face[0], i, k))
        faces.append((i, face[1], j))
        faces[faceIndex] = (k, j, face[2])

    return verts, faces


def scaleIcosahedron(verts, sx, sy, sz):
    for i in range(len(verts)):
        v = verts[i]
        v = (v[0]*sx, v[1]*sy, v[2]*sz)
        verts[i] = v


def printToOBJ(verts, faces):
    """Print out mesh in OBJ format"""
    for v in verts:
        print "v " + str(v[0]) + " " + str(v[1]) + " " + str(v[2])
    for f in faces:
        print "f " + str(f[0]+1) + " " + str(f[1]+1) + " " + str(f[2]+1)


def subdividedIcosahedron(numSubdivisions):
    verts, faces = icosahedron()
    for x in xrange(numSubdivisions):
        verts, faces = subdivide(verts, faces)
    return verts, faces


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
    # Note the list contains duplicates so remove them
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
    p = subprocess.Popen(command, shell=True, stdin=subprocess.PIPE, env=os.environ, cwd=folder)
    name="lat"+str(lat)+"_lon"+str(lon)
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


def runMapmakerAtAllLonLat(lonLat, numJobs):
    Parallel(n_jobs=numJobs)(delayed(runMapmakerAtLonLat)(ll[0], ll[1]) for ll in lonLat)


(verts, faces) = subdividedIcosahedron(2)

# scale points to fit on ellipsoidal shape similar to Eros
#scaleIcosahedron(verts, 16.32921, 8.44172, 5.979465)
scaleIcosahedron(verts, 15.0, 8.44172, 5.979465)

#printToOBJ(verts, faces)

lonLat = vertsToLatLon(verts)

print "number of maplets to do: " + str(len(lonLat))

runMapmakerAtAllLonLat(lonLat, 8)
