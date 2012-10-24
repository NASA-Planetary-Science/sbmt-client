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
function computeSphereTruePotential,density,r,x,y,z

  volume = (4.0/3.0) * !dpi * r * r * r
  mass = density * volume
  G = 6.67384e-11 * 1.0e-9

  true_potential = -1.0e6 * 1.0e12 * G * mass / sqrt(x*x + y*y + z*z)

  return, true_potential

end

;-----------------------------------------------------------------------
function computeSphereTrueAcceleration,density,r,x,y,z

  volume = (4.0/3.0) * !dpi * r * r * r
  mass = density * volume
  G = 6.67384e-11 * 1.0e-9

  true_acc = 1.0e3 * 1.0e12 * G * mass / (x*x + y*y + z*z)

  return, true_acc

end

;-----------------------------------------------------------------------
function computeSpherePotentialError, potentialfile, centersfile, percent_error=percent_error

  r = 1.0
  density = 2.5
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

  r = 1.0
  density = 2.5
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

  density = 2.5
  a = 2.0
  c = 1.0
  true_potential = 1.0e9 * oblate_spheroid_potential(density, a, c, centers[0,*], centers[1,*], centers[2,*])

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

  density = 2.5
  a = 2.0
  c = 1.0
  true_acceleration = 1.0e6 * oblate_spheroid_acceleration(density, a, c, centers[0,*], centers[1,*], centers[2,*])

  if keyword_set(percent_error) then begin
     errors = (double(accelerations) - true_acceleration) / true_acceleration
  endif else begin
     errors = double(accelerations) - true_acceleration
  endelse

  errors = abs(errors)

  return, mean(errors, /double)

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
                 3.93283985308008, $
                 4.123096341494543, $
                 4.172264134573937, $
                 4.184649009908455, $
                 4.187754873578247, $
                 4.188531564762127 $
                 ]

ellipsoid_volumes = [ $
                    15.61096186263374, $
                    16.458620456230864, $
                    16.680336076134594, $
                    16.736413599511888, $
                    16.750470641171884, $
                    16.753986665728796 $
                    ]

sphere_true_mass = 2.5 * (4.0/3.0) * !dpi * 1.0 * 1.0 * 1.0
ellipsoid_true_mass = 2.5 * (4.0/3.0) * !dpi * 2.0 * 2.0 * 1.0

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
   runCommand, './gravity --save-plate-centers --cheng --centers -d ' + sphere_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   runCommand, './elevation-slope ' + platemodelfile + ' ' + spheres[i] + '-potential.txt ' + spheres[i] + '-acceleration.txt'
   times_sphere_cheng[i] = getTimeFromOutput(outfile)
   error_potential_sphere_cheng[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-centers.txt')
   error_acceleration_sphere_cheng[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-centers.txt')
   error_percent_potential_sphere_cheng[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-centers.txt', /per)
   error_percent_acceleration_sphere_cheng[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-centers.txt', /per)
   potential_sphere_cheng[i] = computeFileMean(spheres[i] + '-potential.txt')
   acceleration_sphere_cheng[i] = computeFileMean(spheres[i] + '-acceleration-magnitude.txt')
   elevation_sphere_cheng[i] = computeFileMean(spheres[i] + '-elevation.txt')

   ; run Werner method
   runCommand, './gravity --save-plate-centers --werner --centers -d ' + sphere_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   runCommand, './elevation-slope ' + platemodelfile + ' ' + spheres[i] + '-potential.txt ' + spheres[i] + '-acceleration.txt'
   times_sphere_werner[i] = getTimeFromOutput(outfile)
   error_potential_sphere_werner[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-centers.txt')
   error_acceleration_sphere_werner[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-centers.txt')
   error_percent_potential_sphere_werner[i] = computeSpherePotentialError(spheres[i] + '-potential.txt', spheres[i] + '-centers.txt', /per)
   error_percent_acceleration_sphere_werner[i] = computeSphereAccelerationError(spheres[i] + '-acceleration-magnitude.txt', spheres[i] + '-centers.txt', /per)
   potential_sphere_werner[i] = computeFileMean(spheres[i] + '-potential.txt')
   acceleration_sphere_werner[i] = computeFileMean(spheres[i] + '-acceleration-magnitude.txt')
   elevation_sphere_werner[i] = computeFileMean(spheres[i] + '-elevation.txt')

; Do Spheroid
   ; run Cheng method
   platemodelfile = data_dir + ellipsoids[i]
   runCommand, './gravity --save-plate-centers --cheng --centers -d ' + ellipsoid_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   runCommand, './elevation-slope ' + platemodelfile + ' ' + ellipsoids[i] + '-potential.txt ' + ellipsoids[i] + '-acceleration.txt'
   times_ellipsoid_cheng[i] = getTimeFromOutput(outfile)
   error_potential_ellipsoid_cheng[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-centers.txt')
   error_acceleration_ellipsoid_cheng[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-centers.txt')
   error_percent_potential_ellipsoid_cheng[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-centers.txt', /per)
   error_percent_acceleration_ellipsoid_cheng[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-centers.txt', /per)

   ; run Werner method
   runCommand, './gravity --save-plate-centers --werner --centers -d ' + ellipsoid_densities[i] + ' ' + platemodelfile + ' > ' + outfile
   runCommand, './elevation-slope ' + platemodelfile + ' ' + ellipsoids[i] + '-potential.txt ' + ellipsoids[i] + '-acceleration.txt'
   times_ellipsoid_werner[i] = getTimeFromOutput(outfile)
   error_potential_ellipsoid_werner[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-centers.txt')
   error_acceleration_ellipsoid_werner[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-centers.txt')
   error_percent_potential_ellipsoid_werner[i] = computeEllipsoidPotentialError(ellipsoids[i] + '-potential.txt', ellipsoids[i] + '-centers.txt', /per)
   error_percent_acceleration_ellipsoid_werner[i] = computeEllipsoidAccelerationError(ellipsoids[i] + '-acceleration-magnitude.txt', ellipsoids[i] + '-centers.txt', /per)

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

r = 1.0
density = 2.5
print, "potential sphere true    = ", computeSphereTruePotential(density,r,r,0.0,0.0)
print, "acceleration sphere true     = ", computeSphereTrueAcceleration(density,r,r,0.0,0.0)

end
