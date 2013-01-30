#!/usr/bin/env python

# This program can be used to auto-generate code for solving for the
# roots of the quadratic equation needed when computing the potential
# for an obslate spheroid. The sympy packages are required.

from sympy import *

x, y, z, a, c, u = symbols('x y z a c u')
q = (x*x+y*y)/(a*a+u) + z*z/(c*c+u) - 1
print solve(q, u)
