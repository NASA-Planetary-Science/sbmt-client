function pow, u, v
  return, u^v
end

function oblate_spheroid_acceleration,rho,a,c,x,y,z
;Computes acceleration magnitude on surface if spheroid
;Inputs: rho=density of oblate spheroid
;        a = x-y plane semi-major axis
;        c= z semi-major axes
;        array of x,y and z  numbers on surface
;The GiNaC script oblate_spheroid_acceleration.ginsh was used to
;generate C code which was adapted into this file.
G=6.67384d-11 ;m^3 kg^-1 s^-2

Const=G*!dpi*rho*a*a*c

acc_x = -2.0000000000000000d+00*Const*( 1.0d/( (a*a)-(c*c))*c*x/(a*a)-pow( (a*a)-(c*c),-(3.0d/2.0d))*x*asin(pow(( (a*a)-(c*c))/(a*a),(1.0d/2.0d))))
acc_y = 2.0000000000000000d+00*( pow( (a*a)-(c*c),-(3.0d/2.0d))*asin(pow(( (a*a)-(c*c))/(a*a),(1.0d/2.0d)))*y-1.0d/( (a*a)-(c*c))*c/(a*a)*y)*Const
acc_z = Const*( 4.0000000000000000d+00*1.0d/( (a*a)-(c*c))/c*z+(-4.0d)*pow( (a*a)-(c*c),-(3.0d/2.0d))*asin(pow(( (a*a)-(c*c))/(a*a),(1.0d/2.0d)))*z)

acc = (acc_x*acc_x + acc_y*acc_y + acc_z*acc_z)^0.5d

return, acc

end
