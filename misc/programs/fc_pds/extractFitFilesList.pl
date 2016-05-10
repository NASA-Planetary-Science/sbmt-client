#!/usr/bin/perl
# Extract FIT Files List
# This script parses the output from the cp -uv command and outputs
# the list of all the *.FIT files (with their full path) that were updated 
# by the cp -uv command.
# Input file lines have this format:
#    `/path/to/src/file' -> `/path/to/destination/file' 
# Output file lines have this format:
#    /path/to/destination/file
my $inputFile  = shift(@ARGV);
my $outputFile = shift(@ARGV);
open (INPUTFILE, "<",$inputFile)  or die "Cannot open $inputFile, got error: $!\n";
open (OUTPUTFILE,">",$outputFile) or die "Cannot open $outputFile, got error: $!\n";
my @newFilesList = <INPUTFILE>;
foreach my $line ( @newFilesList ) {
   my $str = $line;
   if( index($str, ".FIT") != -1 ) {
      $str =~ s/([^\`]*\`){2}//;
      $str = substr($str,0,length($str)-2);
      print OUTPUTFILE $str."\n";
   }
}
