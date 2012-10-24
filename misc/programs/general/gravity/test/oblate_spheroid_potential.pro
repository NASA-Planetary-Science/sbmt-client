function oblate_spheroid_potential,rho,a,c,x,y,z
;Computes potential on surface if spheroid
;Inputs: rho=density of oblate spheroid
;        a = x-y plane semi-major axis
;        c= z semi-major axes
;        array of x,y and z  numbers on surface
;Equation for Oblate spheroid comes from Wolfram
G=6.67384e-11 ;m^3 kg^-1 s^-2


Const=G*!dpi*rho*a*a*c

TermA1=1-(x*x+y*y-2*z*z)/(2*(a*a-c*c))
TermA2=(2*asin(sqrt((a*a-c*c)/(a*a))))/sqrt(a*a-c*c)
TermA=TermA1*TermA2
print,TermA(0),x(0),y(0),z(0)

TermB1=(c/(a*a-c*c))*((x*x+y*y)/(a*a))
TermB=TermB1
print,TermB(0)


TermC1=2*z*z/c
TermC2=a*a-c*c
TermC=-1.*TermC1/TermC2
print,TermC(0)


U=-1.*const*(TermA+TermB+TermC)
return,U
end
