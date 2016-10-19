KPL/MK

Eros Text Meta (TM) Kernel for SBMT
==============================================================================

This file contains a list of SPICE kernels to load using the furnish routine
for body Eros. SPICE priority is from bottom up.

JHU/APL

\begindata

PATH_VALUES = ('/project/sbmtpipeline/rawdata/near/kernels/NEAR_A_SPICE_6_EROS_ORBIT_V1_0/data')

PATH_SYMBOLS = ('PATH')
                
KERNELS_TO_LOAD += (
        '$PATH/lsk/naif0007.tls',
		'$PATH/pck/pck00007.tpc',
		'$PATH/pck/erosatt_1999304_2001151.bpc',
		'$PATH/spk/near_cruise_nav_v1.bsp',
		'$PATH/spk/near_erosorbit_nav_v1.bsp',
		'$PATH/spk/near_erosorbit_rs_v1.bsp',
		'$PATH/spk/near_erosorbit_nlr_v1.bsp',
		'$PATH/spk/near_eroslanded_nav_v1.bsp',
		'$PATH/spk/erosephem_1999004_2002181.bsp',
		'$PATH/spk/eros80.bsp',
		'$PATH/spk/math9749.bsp',
		'$PATH/spk/de403s.bsp',
		'$PATH/spk/stations.bsp'
                    )
                    
\begintext