function oblate_spheroid_potential,rho,a,c,x,y,z
;Computes potential on surface if spheroid
;Inputs: rho=density of oblate spheroid
;        a = x-y plane semi-major axis
;        c= z semi-major axes
;        array of x,y and z  numbers on surface
;Equation for Oblate spheroid comes from Wolfram
G=6.67384d-11 ;m^3 kg^-1 s^-2

Const=G*!dpi*rho*a*a*c


; The following 2 lines were auto-generated
root1 = -a^2.0d/2.0d - c^2.0d/2.0d + x^2.0d/2.0d + y^2.0d/2.0d + z^2.0d/2.0d - sqrt(a^4.0d - 2.0d*a^2.0d*c^2.0d - 2.0d*a^2.0d*x^2.0d - 2.0d*a^2.0d*y^2.0d + 2.0d*a^2.0d*z^2.0d + c^4.0d + 2.0d*c^2.0d*x^2.0d + 2.0d*c^2.0d*y^2.0d - 2.0d*c^2.0d*z^2.0d + x^4.0d + 2.0d*x^2.0d*y^2.0d + 2.0d*x^2.0d*z^2.0d + y^4.0d + 2.0d*y^2.0d*z^2.0d + z^4.0d)/2.0d
root2 = -a^2.0d/2.0d - c^2.0d/2.0d + x^2.0d/2.0d + y^2.0d/2.0d + z^2.0d/2.0d + sqrt(a^4.0d - 2.0d*a^2.0d*c^2.0d - 2.0d*a^2.0d*x^2.0d - 2.0d*a^2.0d*y^2.0d + 2.0d*a^2.0d*z^2.0d + c^4.0d + 2.0d*c^2.0d*x^2.0d + 2.0d*c^2.0d*y^2.0d - 2.0d*c^2.0d*z^2.0d + x^4.0d + 2.0d*x^2.0d*y^2.0d + 2.0d*x^2.0d*z^2.0d + y^4.0d + 2.0d*y^2.0d*z^2.0d + z^4.0d)/2.0d

; Choose the positive root
k = transpose(max([root1, root2], DIMENSION=1))

; test root finding
;    left = (x*x+y*y)/(a*a+k) + z*z/(c*c+k)
;    ; This should equal 1
;    print, "(x*x+y*y)/(a*a+k) + z*z/(c*c+k) = ", left
;    print,"root1",root1
;    print,"root2",root2
;    print,"k=",k


TermA1=1-(x*x+y*y-2.0d*z*z)/(2.0d*(a*a-c*c))
TermA2=(2.0d*asin(sqrt((a*a-c*c)/(a*a+k))))/sqrt(a*a-c*c)
TermA=TermA1*TermA2
;print,TermA(0),x(0),y(0),z(0)

TermB1=(sqrt(c*c+k)/(a*a-c*c))*((x*x+y*y)/(a*a+k))
TermB=TermB1
;print,TermB(0)

TermC1=2.0d*z*z/sqrt(c*c+k)
TermC2=(a*a-c*c)
TermC=-1.0d*TermC1/TermC2
;print,TermC(0)


U=-1.0d*const*(TermA+TermB+TermC)
return,U
end
