KPL/MK

OCAMS Meta (FURNSH) Kernel for SBMT
==============================================================================

This file contains a list of SPICE kernels to be loaded into the SPICE system
using the FURNSH subroutine. SPICE priority order is from bottom up.

This kernel was produced by Lillian Nguyen, JHU/APL, for the Small Body Mapping
Tool (SBMT), and is not intended for distribution.


\begindata
KERNELS_TO_LOAD += ('/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase03_AP_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase04_PS_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase05_OA_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase06_DS_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase07_OB_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase08_RN_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase09_RE_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase10_SA_01s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/OREX_DRMrevC_Phase11_QO_60s.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/sb-101955-76.bsp'
                    '/project/osiris/ola/spice/Kernels/SPK/de421.bsp'
                    )
KERNELS_TO_LOAD += ('/project/osiris/ola/spice/Kernels/CK/Phase03_AP_NominalProfile.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_4.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_5.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_6.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_7.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_8.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_9.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_ShapeModel_9_Forced4x4.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase03_AP_PhaseFunction_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_NominalProfile.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_OLA_Nominal_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_OLA_Nominal_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_OLA_Nominal_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_OLA_Nominal_4.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_PolyCam_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_PolyCam_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_PolyCam_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_PolyCam_4.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_PolyCam_5.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_PolyCam_6.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_MapCamOLA_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase04_PS_MapCamOLA_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase05_OA_NominalProfile.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_NominalProfile.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Baseball_Diamond_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Baseball_Diamond_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Baseball_Diamond_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Baseball_Diamond_4.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Equatorial_Stations_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Equatorial_Stations_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Equatorial_Stations_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Equatorial_Stations_4.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Equatorial_Stations_5.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Equatorial_Stations_6.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Equatorial_Stations_7.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Plume_Search_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase06_DS_Plume_Search_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase07_OB_NominalProfile.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase07_OB_CSS_Mapping_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase07_OB_CSS_Mapping_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase07_OB_CSS_Mapping_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase08_RN_NominalProfile.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase08_RN_Primary_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase08_RN_Primary_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase08_RN_Primary_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase08_RN_Secondary_1.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase08_RN_Secondary_2.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase08_RN_Secondary_3.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase10_SA_NominalProfile.bc'
                    '/project/osiris/ola/spice/Kernels/CK/Phase10_SA_TAGsequence.bc'
                    )
KERNELS_TO_LOAD += ('/project/osiris/ola/spice/Kernels/LSK/naif0011.tls'
                    )
KERNELS_TO_LOAD += ('/project/osiris/ola/spice/Kernels/PCK/pck00010.tpc'
                    '/project/osiris/ola/spice/Kernels/PCK/bennu_v10.tpc'
                    )
KERNELS_TO_LOAD += ('/project/osiris/ola/spice/Kernels/SCLK/ORX_SCLKSCET.00000.example.tsc'
                    )
KERNELS_TO_LOAD += ('/project/osiris/ola/spice/Kernels/FK/orx_v03.tf'
                    )
KERNELS_TO_LOAD += ('/project/osiris/ola/spice/Kernels/IK/orex_ocams_v02.ti'
                    )
\begintext
