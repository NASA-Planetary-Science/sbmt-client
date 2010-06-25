#!/usr/bin/perl

# This program splits a given file into a specified number of
# files of approximately equal length.
# 
# Usage: ./split.pl <filename> <# files to split into>

use POSIX qw(ceil floor);

$input_file       = $ARGV[0];
$num_output_files = $ARGV[1];

# Count the number of the lines in the given file
$num_input_lines = `wc -l $input_file | awk '{print \$1}'`;
chomp($num_input_lines);

# Compute the number of lines that should go in each output file
$num_output_lines = ceil($num_input_lines / $num_output_files);

# Run the split command using numeric suffixes
system("split -d -l $num_output_lines $input_file $input_file.");



#print $num_input_lines . "\n";
#print $num_output_files . "\n";
#print $num_output_lines . "\n";
