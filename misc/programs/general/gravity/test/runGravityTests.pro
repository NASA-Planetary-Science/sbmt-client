;-----------------------------------------------------------------------
pro runCommand, command

   print, command
   spawn, command

end

;-----------------------------------------------------------------------
function getTimeFromOutput, filename

; The time taken to run the gravity can be found by parsing the output
; file. The second to last word on the last line contains the number
; of seconds
lines = getfile(filename)
last = n_elements(lines)-1
tokens = strsplit(lines(last), /extract)
second_to_last = n_elements(tokens)-2
return, tokens[second_to_last]

end

;-----------------------------------------------------------------------
function getReferencePotentialFromOutput, filename

; The reference potential can be found by parsing the output
; file. The last word on the last line contains the reference
; potenital
lines = getfile(filename)
last = n_elements(lines)-1
tokens = strsplit(lines(last), /extract)
last = n_elements(tokens)-1
return, tokens[last]

end

;-----------------------------------------------------------------------
function computeSphereTruePotential,density,r,x,y,z

  volume = (4.0d/3.0d) * !dpi * r * r * r
  mass = density * volume
  G = 6.67384d-11 * 1.0d-9

  true_potential = -1.0d6 * 1.0d12 * G * mass / sqrt(x*x + y*y + z*z)

  return, true_potential

end

;-----------------------------------------------------------------------
function computeSphereTrueAcceleration,density,r,x,y,z

  volume = (4.0d/3.0d) * !dpi * r * r * r
  mass = density * volume
  G = 6.67384d-11 * 1.0d-9

  true_acc = 1.0d3 * 1.0d12 * G * mass / (x*x + y*y + z*z)

  return, true_acc

end

;-----------------------------------------------------------------------
function computeSpherePotentialError, potentialfile, centersfile, percent_error=percent_error

  r = 1.0d
  density = 2.5d
  potentials = getfile(potentialfile)
  lines = getfile(centersfile)
  numLines = n_elements(lines)
  centers = dblarr(3, numLines)
  for i = 0, numLines-1, 1 do begin
     tokens = strsplit(lines[i], /extract)
     centers[0,i] = double(tokens[0])
     centers[1,i] = double(tokens[1])
     centers[2,i] = double(tokens[2])
  end

  true_potential = computeSphereTruePotential(density, r, centers[0,*], centers[1,*], centers[2,*])

  if keyword_set(percent_error) then begin
     errors = (double(potentials) - true_potential) / true_potential
  endif else begin
     errors = double(potentials) - true_potential
  endelse

  errors = abs(errors)

  return, mean(errors, /double)

end

;-----------------------------------------------------------------------
function computeSphereAccelerationError, accelerationfile, centersfile, percent_error=percent_error

  r = 1.0d
  density = 2.5d
  accelerations = getfile(accelerationfile)
  lines = getfile(centersfile)
  numLines = n_elements(lines)
  centers = dblarr(3, numLines)
  for i = 0, numLines-1, 1 do begin
     tokens = strsplit(lines[i], /extract)
     centers[0,i] = double(tokens[0])
     centers[1,i] = double(tokens[1])
     centers[2,i] = double(tokens[2])
  end

  true_acceleration = computeSphereTrueAcceleration(density, r, centers[0,*], centers[1,*], centers[2,*])

  if keyword_set(percent_error) then begin
     errors = (double(accelerations) - true_acceleration) / true_acceleration
  endif else begin
     errors = double(accelerations) - true_acceleration
  endelse

  errors = abs(errors)

  return, mean(errors, /double)

end

;-----------------------------------------------------------------------
function computeFileMean, datafile

  data = getfile(datafile)
  data = double(data)
  return, mean(data, /double)

end

;-----------------------------------------------------------------------
function computeEllipsoidPotentialError, potentialfile, centersfile, percent_error=percent_error

  potentials = getfile(potentialfile)
  lines = getfile(centersfile)
  numLines = n_elements(lines)
  centers = dblarr(3, numLines)
  for i = 0, numLines-1, 1 do begin
     tokens = strsplit(lines[i], /extract)
     centers[0,i] = double(tokens[0])
     centers[1,i] = double(tokens[1])
     centers[2,i] = double(tokens[2])
  end

  density = 2.5d
  a = 2.0d
  c = 1.0d
  true_potential = 1.0d9 * oblate_spheroid_potential(density, a, c, centers[0,*], centers[1,*], centers[2,*])

  if keyword_set(percent_error) then begin
     errors = (double(potentials) - true_potential) / true_potential
  endif else begin
     errors = double(potentials) - true_potential
  endelse

  errors = abs(errors)

  return, mean(errors, /double)

end

;-----------------------------------------------------------------------
function computeEllipsoidAccelerationError, accelerationfile, centersfile, percent_error=percent_error

  accelerations = getfile(accelerationfile)
  lines = getfile(centersfile)
  numLines = n_elements(lines)
  centers = dblarr(3, numLines)
  for i = 0, numLines-1, 1 do begin
     tokens = strsplit(lines[i], /extract)
     centers[0,i] = double(tokens[0])
     centers[1,i] = double(tokens[1])
     centers[2,i] = double(tokens[2])
  end

  density = 2.5d
  a = 2.0d
  c = 1.0d
  true_acceleration = 1.0d6 * oblate_spheroid_acceleration(density, a, c, centers[0,*], centers[1,*], centers[2,*])

  if keyword_set(percent_error) then begin
     errors = (double(accelerations) - true_acceleration) / true_acceleration
  endif else begin
     errors = double(accelerations) - true_acceleration
  endelse

  errors = abs(errors)

  return, mean(errors, /double)

end

;-----------------------------------------------------------------------
pro saveAndPlotAltitudeError, points, errorsCheng, errorsWerner, filenamePrefix

  radii = sqrt(points[0,*]*points[0,*] + points[1,*]*points[1,*] + points[2,*]*points[2,*])

  openw,lun1,filenamePrefix+'.txt',/get_lun
  ii=0L
  n = size(points)
  n = n[2]
  while ii lt n do begin
      printf,lun1,radii[ii],errorsCheng[ii],errorsWerner[ii],format='(2(e15.6,x),e15.6)'
      ii=ii+1
  endwhile
  close,lun1

  set_plot,'ps'
  device,filename=filenamePrefix+'.ps'
  plot,radii,errorsCheng,xtitle='radius',ytitle='Fractional Deviation',title='Error as Function of Altitude'
  oplot,radii,errorsWerner,linestyle=2
  plots, [0.55, 0.75], [0.88, 0.88], /normal
  xyouts, 0.8, 0.877, 'Cheng', /normal
  plots, [0.55, 0.75], [0.84, 0.84], linestyle=2, /normal
  xyouts, 0.8, 0.837, 'Werner', /normal
  device, /close
  set_plot,'x'
  runCommand, 'ps2pdf '+filenamePrefix+'.ps'

end

;-----------------------------------------------------------------------
pro runAltitudeTest

fname='/tmp/inputpoints.txt'
openw,1,fname
size = 3000
points = dblarr(3, size)
dx = 0.001d
dir = [.2d, .45d, .23d]
mag = sqrt(dir[0]*dir[0] + dir[1]*dir[1] + dir[2]*dir[2])
dir = dir / mag
for i = 0, size-1, 1 do begin
   x = 0.0d
   z = 0.0d
   y = 1.0d + dx * i
   points[0,i] = x
   points[1,i] = y
   points[2,i] = z

   points[*,i] = dir * (1.0d + dx * i)
   x = points[0,i]
   y = points[1,i]
   z = points[2,i]
   printf,1,x,y,z,format='(F16.10,1X,F16.10,1X,F16.10)'
end
close,1

outfile = '/tmp/output'
a = 2.0d
c = 1.0d
r = 1.0d
densityTrue = 2.5d

;platemodelfile = '/project/nearsdc/data/test/SPHERE_v6534.PLT'
;potentialfile = 'SPHERE_v6534.PLT-potential.txt'
;density = '2.5024740395599068'
platemodelfile = '/project/nearsdc/data/test/SPHERE_V99846.PLT'
potentialfile = 'SPHERE_V99846.PLT-potential.txt'
density = '2.5001543739257208'

runCommand, './gravity --cheng --file ' + fname + ' -d ' + density + ' ' + platemodelfile + ' > ' + outfile
potentials = getfile(potentialfile)
true_potential = computeSphereTruePotential(densityTrue, r, points[0,*], points[1,*], points[2,*])
errorsCheng = (double(potentials) - true_potential) / true_potential
errorsCheng = abs(errorsCheng)

runCommand, './gravity --werner --file ' + fname + ' -d ' + density + ' ' + platemodelfile + ' > ' + outfile
potentials = getfile(potentialfile)
true_potential = computeSphereTruePotential(densityTrue, r, points[0,*], points[1,*], points[2,*])
errorsWerner = (double(potentials) - true_potential) / true_potential
errorsWerner = abs(errorsWerner)


saveAndPlotAltitudeError, points, errorsCheng, errorsWerner, "sphere-altitude-error"



;platemodelfile = '/project/nearsdc/data/test/ELLIPSOID_v6534.PLT'
;potentialfile = 'ELLIPSOID_v6534.PLT-potential.txt'
;density = '2.5028003639373222'
platemodelfile = '/project/nearsdc/data/test/ELLIPSOID_v99846.PLT'
potentialfile = 'ELLIPSOID_v99846.PLT-potential.txt'
density = '2.5001752050780794'

runCommand, './gravity --cheng --file ' + fname + ' -d ' + density + ' ' + platemodelfile + ' > ' + outfile
potentials = getfile(potentialfile)
true_potential = 1.0d9 * oblate_spheroid_potential(densityTrue, a, c, points[0,*], points[1,*], points[2,*])
errorsCheng = (double(potentials) - true_potential) / true_potential
errorsCheng = abs(errorsCheng)

runCommand, './gravity --werner --file ' + fname + ' -d ' + density + ' ' + platemodelfile + ' > ' + outfile
potentials = getfile(potentialfile)
true_potential = 1.0d9 * oblate_spheroid_potential(densityTrue, a, c, points[0,*], points[1,*], points[2,*])
errorsWerner = (double(potentials) - true_potential) / true_potential
errorsWerner = abs(errorsWerner)

saveAndPlotAltitudeError, points, errorsCheng, errorsWerner, "ellipsoid-altitude-error"



end


;-----------------------------------------------------------------------
pro runGravityTests

outfile = '/tmp/output'

data_dir = '/project/nearsdc/data/test/'

spheres = [ $
          'SPHERE_v150.PLT', $
          'SPHERE_v486.PLT', $
          'SPHERE_v1734.PLT', $
          'SPHERE_v6534.PLT', $
          'SPHERE_v25350.PLT', $
          'SPHERE_V99846.PLT' $
          ]

ellipsoids = [ $
             'ELLIPSOID_v150.PLT', $
             'ELLIPSOID_v486.PLT', $
             'ELLIPSOID_v1730.PLT', $
             'ELLIPSOID_v6534.PLT', $
             'ELLIPSOID_v25350.PLT', $
             'ELLIPSOID_v99846.PLT' $
             ]

sphere_volumes = [ $
                 3.93283985308008d, $
                 4.123096341494543d, $
                 4.172264134573937d, $
                 4.184649009908455d, $
                 4.187754873578247d, $
                 4.188531564762127d $
                 ]

ellipsoid_volumes = [ $
                    15.61096186263374d, $
                    16.458620456230864d, $
                    16.680336076134594d, $
                    16.736413599511888d, $
                    16.750470641171884d, $
                    16.753986665728796d $
                    ]


sphere_true_mass = 2.5d * (4.0d/3.0d) * !dpi * 1.0d * 1.0d * 1.0d
ellipsoid_true_mass = 2.5d * (4.0d/3.0d) * !dpi * 2.0d * 2.0d * 1.0d

sphere_densities = string(sphere_true_mass / sphere_volumes, format='(D0.16)')
ellipsoid_densities = string(ellipsoid_true_mass / ellipsoid_volumes, format='(D0.16)')


num_res = 6
times_sphere_cheng = dblarr(num_res)
times_sphere_werner = dblarr(num_res)
times_ellipsoid_cheng = dblarr(num_res)
times_ellipsoid_werner = dblarr(num_res)

error_potential_sphere_cheng = dblarr(num_res)
error_potential_sphere_werner = dblarr(num_res)
error_potential_ellipsoid_cheng = dblarr(num_res)
error_potential_ellipsoid_werner = dblarr(num_res)

error_acceleration_sphere_cheng = dblarr(num_res)
error_acceleration_sphere_werner = dblarr(num_res)
error_acceleration_ellipsoid_cheng = dblarr(num_res)
error_acceleration_ellipsoid_werner = dblarr(num_res)

error_percent_potential_sphere_cheng = dblarr(num_res)
error_percent_potential_sphere_werner = dblarr(num_res)
error_percent_potential_ellipsoid_cheng = dblarr(num_res)
error_percent_potential_ellipsoid_werner = dblarr(num_res)

error_percent_acceleration_sphere_cheng = dblarr(num_res)
error_percent_acceleration_sphere_werner = dblarr(num_res)
error_percent_acceleration_ellipsoid_cheng = dblarr(num_res)
error_percent_acceleration_ellipsoid_werner = dblarr(num_res)

elevation_sphere_cheng = dblarr(num_res)
elevation_sphere_werner = dblarr(num_res)
potential_sphere_cheng = dblarr(num_res)
potential_sphere_werner = dblarr(num_res)
acceleration_sphere_cheng = dblarr(num_res)
acceleration_sphere_werner = dblarr(num_res)

;Run the Cheng and Werner methods on the sphere and spheroid
for i = 0, num_res-1, 1 do begin

; Do Sphere
   ; run Cheng method
   platemodelfile = data_dir + spheres[i]
   ; First compute reference potential which is needed for computing elevation
   runCommand, './gravity --cheng --centers -d ' + sphere_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   runCommand, './elevation-slope ' + platemodelfile + ' ' + spheres[i] + '-potential.txt ' + spheres[i] + '-acceleration.txt' + ' > ' + outfile
   refPotential = getReferencePotentialFromOutput(outfile)
   runCommand, './normals-centers ' + platemodelfile + ' 1.0 1.0'
   runCommand, './gravity --cheng --file ' + spheres[i] + '-shifted-centers.txt' + ' -d ' + sphere_densities[i] + ' --ref-potential ' + refPotential + ' ' + platemodelfile + ' > ' + outfile
   times_sphere_cheng[i] = getTimeFromOutput(outfile)
   error_potential_sphere_cheng[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-shifted-centers.txt')
   error_acceleration_sphere_cheng[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-shifted-centers.txt')
   error_percent_potential_sphere_cheng[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-shifted-centers.txt', /per)
   error_percent_acceleration_sphere_cheng[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-shifted-centers.txt', /per)
   potential_sphere_cheng[i] = computeFileMean(spheres[i] + '-potential.txt')
   acceleration_sphere_cheng[i] = computeFileMean(spheres[i] + '-acceleration-magnitude.txt')
   elevation_sphere_cheng[i] = computeFileMean(spheres[i] + '-elevation.txt')

   ; run Werner method
   ; First compute reference potential which is needed for computing elevation
   runCommand, './gravity --werner --centers -d ' + sphere_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   runCommand, './elevation-slope ' + platemodelfile + ' ' + spheres[i] + '-potential.txt ' + spheres[i] + '-acceleration.txt' + ' > ' + outfile
   refPotential = getReferencePotentialFromOutput(outfile)
   runCommand, './normals-centers ' + platemodelfile + ' 1.0 1.0'
   runCommand, './gravity --werner --file ' + spheres[i] + '-shifted-centers.txt' + ' -d ' + sphere_densities[i] + ' --ref-potential ' + refPotential + ' ' + platemodelfile + ' > ' + outfile
   times_sphere_werner[i] = getTimeFromOutput(outfile)
   error_potential_sphere_werner[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-shifted-centers.txt')
   error_acceleration_sphere_werner[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-shifted-centers.txt')
   error_percent_potential_sphere_werner[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-shifted-centers.txt', /per)
   error_percent_acceleration_sphere_werner[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-shifted-centers.txt', /per)
   potential_sphere_werner[i] = computeFileMean(spheres[i] + '-potential.txt')
   acceleration_sphere_werner[i] = computeFileMean(spheres[i] + '-acceleration-magnitude.txt')
   elevation_sphere_werner[i] = computeFileMean(spheres[i] + '-elevation.txt')

; Do Spheroid
   ; run Cheng method
   platemodelfile = data_dir + ellipsoids[i]
   runCommand, './normals-centers ' + platemodelfile + ' 2.0 1.0'
   runCommand, './gravity --cheng --file ' + ellipsoids[i] + '-shifted-centers.txt' + ' -d ' + ellipsoid_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   times_ellipsoid_cheng[i] = getTimeFromOutput(outfile)
   error_potential_ellipsoid_cheng[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-shifted-centers.txt')
   error_acceleration_ellipsoid_cheng[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-shifted-centers.txt')
   error_percent_potential_ellipsoid_cheng[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-shifted-centers.txt', /per)
   error_percent_acceleration_ellipsoid_cheng[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-shifted-centers.txt', /per)

   ; run Werner method
   runCommand, './normals-centers ' + platemodelfile + ' 2.0 1.0'
   runCommand, './gravity --werner --file ' + ellipsoids[i] + '-shifted-centers.txt' + ' -d ' + ellipsoid_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   times_ellipsoid_werner[i] = getTimeFromOutput(outfile)
   error_potential_ellipsoid_werner[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-shifted-centers.txt')
   error_acceleration_ellipsoid_werner[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-shifted-centers.txt')
   error_percent_potential_ellipsoid_werner[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-shifted-centers.txt', /per)
   error_percent_acceleration_ellipsoid_werner[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-shifted-centers.txt', /per)

endfor


print, "density sphere    = ", sphere_densities
print, "density ellipsoid = ", ellipsoid_densities
print, "volume sphere     = ", sphere_volumes
print, "volume ellipsoid  = ", ellipsoid_volumes

print, "times sphere cheng     = ", times_sphere_cheng
print, "times sphere werner    = ", times_sphere_werner
print, "times ellipsoid cheng  = ", times_ellipsoid_cheng
print, "times ellipsoid werner = ", times_ellipsoid_werner

print, "error potential sphere cheng     = ", error_potential_sphere_cheng
print, "error potential sphere werner    = ", error_potential_sphere_werner
print, "error potential ellipsoid cheng  = ", error_potential_ellipsoid_cheng
print, "error potential ellipsoid werner = ", error_potential_ellipsoid_werner

print, "error acceleration sphere cheng     = ", error_acceleration_sphere_cheng
print, "error acceleration sphere werner    = ", error_acceleration_sphere_werner
print, "error acceleration ellipsoid cheng  = ", error_acceleration_ellipsoid_cheng
print, "error acceleration ellipsoid werner = ", error_acceleration_ellipsoid_werner

print, "error percent potential sphere cheng     = ", error_percent_potential_sphere_cheng
print, "error percent potential sphere werner    = ", error_percent_potential_sphere_werner
print, "error percent potential ellipsoid cheng  = ", error_percent_potential_ellipsoid_cheng
print, "error percent potential ellipsoid werner = ", error_percent_potential_ellipsoid_werner

print, "error percent acceleration sphere cheng     = ", error_percent_acceleration_sphere_cheng
print, "error percent acceleration sphere werner    = ", error_percent_acceleration_sphere_werner
print, "error percent acceleration ellipsoid cheng  = ", error_percent_acceleration_ellipsoid_cheng
print, "error percent acceleration ellipsoid werner = ", error_percent_acceleration_ellipsoid_werner

print, "potential sphere cheng     = ", potential_sphere_cheng
print, "potential sphere werner    = ", potential_sphere_werner
print, "acceleration sphere cheng     = ", acceleration_sphere_cheng
print, "acceleration sphere werner    = ", acceleration_sphere_werner
print, "elevation sphere cheng     = ", elevation_sphere_cheng
print, "elevation sphere werner    = ", elevation_sphere_werner

r = 1.0d
density = 2.5d
print, "potential sphere true    = ", computeSphereTruePotential(density,r,r,0.0d,0.0d)
print, "acceleration sphere true     = ", computeSphereTrueAcceleration(density,r,r,0.0d,0.0d)



runAltitudeTest

end
