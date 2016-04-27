#!/usr/bin/perl -w

my @ckDirsNaif = </project/sbmtpipeline/rawdata/dawn/NAIF/kernels/ck/>;
my @ck_list_naif = ();

my@tmp_list = ();
foreach my $dir (@ckDirsNaif) {
    opendir(DH, $dir) or die "Cannot open $dir: $!\n";

#    @tmp_list = sort { $a cmp $b } grep { /dawn_sc_15\d{4}_\d{6}\.bc\z/ } readdir(DH);
#    push(@ck_list_naif, @tmp_list); 

    @tmp_list = 0;

    @tmp_list = sort { $a cmp $b } grep { /(dawn_sc_15\d{4}_\d{6}\.bc\z)|(dawn_sc_16\d{4}_\d{6}\.bc\z)|(dawn_fc_v\d+\.bc\z)/ } readdir(DH);
#    @tmp_list = grep { /dawn_fc_v\d+\.bc\z/ } readdir(DH);
    push(@ck_list_naif, @tmp_list); 

    print "@tmp_list\n\n";

    closedir(DH);
}


print "@ck_list_naif\n";
