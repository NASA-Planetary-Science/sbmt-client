#!/usr/bin/perl -w

#
# Create a metakernel file for DAWN data.  Get list of files in appropriate 
# spice directories and print out the metakernel file with some header
# information. This script was created by Dave Bazell for DAWN and 
# modified for SBMT.
# See http://naif.jpl.nasa.gov/pub/naif/DAWN/kernels/spk/aareadme.txt
# and http://naif.jpl.nasa.gov/pub/naif/DAWN/kernels/ck/aareadme.txt
# for DAWN file naming conventions.

use strict;
my @months   = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);
my @dow      = qw(Sun Mon Tue Wed Thu Fri Sat);

my @spkDirs = </project/sbmtpipeline/rawdata/dawn/spice/spk/reconstructed/2015/>;
my @ckDirs = </project/sbmtpipeline/rawdata/dawn/spice/ck/Spacecraft/2015/>;
#my @spk2016Dirs = </project/sbmtpipeline/rawdata/dawn/spice/spk/reconstructed/2016/>; not yet available
my @ck2016Dirs = </project/sbmtpipeline/rawdata/dawn/spice/ck/Spacecraft/2016/>;
my @ckDirsNaif = </project/sbmtpipeline/rawdata/dawn/NAIF/kernels/ck/>;
my @spkDirsNaif = </project/sbmtpipeline/rawdata/dawn/NAIF/kernels/spk/>;

my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime;

my $date     = $months[0] . " " .  sprintf("%04d", $year + 1900);

my @spk_ql2016_list = ();
my @spk_rec2016_list = ();
my @spk_ql_list = ();
my @spk_rec_list = ();
my @ck_list = ();
my @ck2016_list = ();
my @ck_list_naif = ();
my @spk_list_naif = ();

# Get data from PROJECT spice directory
my@tmp_list = ();
foreach my $dir (@spkDirs) {
    opendir(DH, $dir) or die "Cannot open $dir: $!\n";
    @tmp_list  = sort {$a cmp $b } grep { /dawn_ql.*\.bsp\z/ } readdir DH;
    push(@spk_ql_list, @tmp_list);
    closedir(DH);
}
@tmp_list = ();
foreach my $dir (@spkDirs) {
    opendir(DH, $dir) or die "Cannot open $dir: $!\n";
    @tmp_list = sort {$a cmp $b } grep { /dawn_rec.*\.bsp\z/ } readdir DH;
    push(@spk_rec_list, @tmp_list);
    closedir(DH);
}
#@tmp_list = ();
#foreach my $dir (@spk2016Dirs) {
#    opendir(DH, $dir) or die "Cannot open $dir: $!\n";
#    @tmp_list  = sort {$a cmp $b } grep { /dawn_ql.*\.bsp\z/ } readdir DH;
#    push(@spk_ql2016_list, @tmp_list);
#    closedir(DH);
#}
#@tmp_list = ();
#foreach my $dir (@spk2016Dirs) {
#    opendir(DH, $dir) or die "Cannot open $dir: $!\n";
#    @tmp_list = sort {$a cmp $b } grep { /dawn_rec.*\.bsp\z/ } readdir DH;
#    push(@spk_rec2016_list, @tmp_list);
#    closedir(DH);
#}
@tmp_list = ();
foreach my $dir (@ckDirs) {
   opendir(DH, $dir) or die "Cannot open $dir: $!\n";
   @tmp_list = sort { $a cmp $b } grep { /dawn_sc_\d{6}_\d{6}\.bc\z/ } readdir(DH);
   push(@ck_list, @tmp_list); 
   closedir(DH);
}
@tmp_list = ();
foreach my $dir (@ck2016Dirs) {
   opendir(DH, $dir) or die "Cannot open $dir: $!\n";
   @tmp_list = sort { $a cmp $b } grep { /dawn_sc_\d{6}_\d{6}\.bc\z/ } readdir(DH);
   push(@ck2016_list, @tmp_list); 
   closedir(DH);
}

# Get data from NAIF:  Only get 2015,2016 data
@tmp_list = ();
foreach my $dir (@ckDirsNaif) {
   opendir(DH, $dir) or die "Cannot open $dir: $!\n";
   @tmp_list = sort { $a cmp $b } grep { /(dawn_sc_15\d{4}_\d{6}\.bc\z)|(dawn_sc_16\d{4}_\d{6}\.bc\z)|(dawn_fc_v\d+\.bc\z)/ } readdir(DH);
   push(@ck_list_naif, @tmp_list); 
   closedir(DH);
}
@tmp_list = ();
foreach my $dir (@spkDirsNaif) {
   opendir(DH, $dir) or die "Cannot open $dir: $!\n";
   @tmp_list = sort { $a cmp $b } grep { /dawn_ql_15.*\.bsp\z/ } readdir(DH);
   push(@spk_list_naif, @tmp_list); 
   @tmp_list = 0;
   @tmp_list = sort { $a cmp $b } grep { /dawn_rec_15.*\.bsp\z/ } readdir(DH);
   push(@spk_list_naif, @tmp_list);
   @tmp_list = 0;
   @tmp_list = sort { $a cmp $b } grep { /dawn_ql_16.*\.bsp\z/ } readdir(DH);
   push(@spk_list_naif, @tmp_list); 
   @tmp_list = 0;
   @tmp_list = sort { $a cmp $b } grep { /dawn_rec_16.*\.bsp\z/ } readdir(DH);
   push(@spk_list_naif, @tmp_list);
   closedir(DH);
}

# Output file
open MK, ">/project/sbmtpipeline/processed/dawn/ceres/kernels_ceres.mk" or die "Error in mkmetakernel.pl: cannot open output file kernels_ceres.mk:  $!\n";

# Print header info
print MK "\\begintext\n";
print MK "===============================================================================\n";
print MK "\n";
print MK "This is a SPICE metakernel for the Small Body Mapping Tool (SBMT) created from\n";
print MK "spice files downloaded from the DAWN science center website,\n";
print MK "http://dscws.igpp.ucla.edu/:/data/DSDb/data/\n";
print MK "\n";
print MK "Version and Date\n";
print MK "===============================================================================\n";
print MK "\n";
print MK "   Version 1.0 -- $date -- SBMT JHU/APL\n";
print MK "===============================================================================\n";
print MK "\n";

# Data section
print MK "\\begindata\n";
print MK "\n";
print MK "PATH_VALUES = ( '/project/sbmtpipeline/rawdata/dawn/spice/spk/reconstructed/2015',
                '/project/sbmtpipeline/rawdata/dawn/spice/spk/reconstructed/2016',
                '/project/sbmtpipeline/rawdata/dawn/spice/ck/Spacecraft/2015',
                '/project/sbmtpipeline/rawdata/dawn/spice/ck/Spacecraft/2016',
                '/project/sbmtpipeline/rawdata/dawn/spice/fk',
                '/project/sbmtpipeline/rawdata/dawn/spice/ik',
                '/project/sbmtpipeline/rawdata/dawn/spice/lsk',
                '/project/sbmtpipeline/rawdata/dawn/spice/pck',
                '/project/sbmtpipeline/rawdata/dawn/spice/sclk',
                '/project/sbmtpipeline/rawdata/dawn/NAIF/kernels/ck',
                '/project/sbmtpipeline/rawdata/dawn/NAIF/kernels/spk'
              )\n";
print MK "\n";
print MK "PATH_SYMBOLS = ( 'SPK',
                 'SPK2016',
                 'CK',
                 'CK2016',
                 'FK',
                 'IK',
                 'LSK',
                 'PCK',
                 'SCLK',
                 'CKNAIF',
                 'SPKNAIF'
                )\n";
print MK "\n";
# Print the fixed kernel names
print MK "KERNELS_TO_LOAD = (
                   '\$LSK/naif0011.tls',
                   '\$PCK/pck00010.tpc',
                   '\$PCK/dawn_ceres_v04.tpc',
                   '/project/sbmtpipeline/rawdata/dawn/spice/spk/reconstructed/de421.bsp',
                   '/project/sbmtpipeline/rawdata/dawn/spice/spk/reconstructed/sb_ceres_140724.bsp',
                   '\$SCLK/DAWN_203_SCLKSCET.00060.tsc',
                   '\$IK/dawn_fc_v10.ti',
                   '\$FK/dawn_v13.tf',
                   '\$FK/dawn_ceres_v00.tf',\n";

# Print NAIF spk  files 
foreach my $spk (@spk_list_naif) {
    print MK "                   '\$SPKNAIF/$spk',\n";
}

# Print spk quicklook files (should come first)
foreach my $spk (@spk_ql_list) {
    print MK "                   '\$SPK/$spk',\n";
}
#foreach my $spk (@spk_ql2016_list) {
#    print MK "                   '\$SPK2016/$spk',\n";
#}

# Print spk reconstructed files (should come after quicklook)
foreach my $spk (@spk_rec_list) {
    print MK "                   '\$SPK/$spk',\n";
}
#foreach my $spk (@spk_rec2016_list) {
#    print MK "                   '\$SPK2016/$spk',\n";
#}

# Print NAIF c-kernel files
foreach my $ck (@ck_list_naif) {
    print MK "                   '\$CKNAIF/$ck',\n";
}

# Print c-kernel files
foreach my $ck (@ck_list) {
    print MK "                   '\$CK/$ck',\n";
}
foreach my $ck (@ck2016_list) {
    print MK "                   '\$CK2016/$ck',\n";
}

# Print trailer info
print MK "                   )\n";
print MK "\n";
print MK "\\begintext\n";

close MK;
