#! /usr/bin/perl

# Licensed under Apache 2.0
# Copyright 2011, National Research Council of Canada

#############################################################################
#              National Research Council -- IIT - Fredericton
#
# PROJECT:      SAVOIR
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To scan a given directory and set the group sticky bit
#   	    	on all found directories - including the given directory.
#   	    	Setting the sticky bit will preserve group names on all new
#   	    	files created within the directory. This is required for
#   	    	software installed as system software but various members of
#   	    	a group need to create files - such as config files - within
#   	    	the install directory of the software. Preserving the group
#   	    	name on new files allows access using group permissions.
#
#				This script also sets the group to the given group and sets
#				the group permissions to be read and write.
# CMD LINE ARG: directory to scan
#				group value for the directory
# OUTPUT:       none
# DATE CREATED: May 11 2010
# LAST MOD: 	$Date:$
# LAST MOD BY:	$Author:$
# USAGE:    	For internal NRC use and distribution with SAVOIR only
# COMMENTS: 	
#
#############################################################################

# ****************************** Main Script ********************************

# Declare variables
my($dir);   	# Directory to scan
my($grp);   	# Group to set for the directory

# Use any pragmas
use strict;
use subs qw(&SearchDir);

# Include any external modules
use Cwd;

# Make sure we are root
if ($< != 0)
{
    die "You must be user root to run this script\n";
}

# The 1 is the last index into the ARGV array
if ($#ARGV != 1)
{
    die "Usage: setGroupAccess <dir to scan - absolute path> <group>\n";
}
else
{
    $dir = $ARGV[0];
	$grp = $ARGV[1];
}


# Check if the dir is an absolute path
if (!($dir =~ /^\//))
{
	die "The directory must be specified as an absolute path\n";
}

# Scan the files in the directory
print STDERR "Searching $dir...\n";
SearchDir($dir);

# Set the group for the directory and its contents
system("chgrp", "-R", $grp, $dir) && die "Failed to set group $grp for $dir $!\n";

# Set the group permissions for the directory and its contents
system("chmod", "-R", "g+rw", $dir) && die "Set group perms failed for $dir $!\n";

# Exit the script
exit(0);

# ************************* Internal Procedures *****************************

#############################################################################
#
# PROC NAME:    SearchDir
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To recursively search the given directory and set the group
#   	    	sticky bit on any directories
# INPUT VARS:   $dir
# RETURN VARS:  none
# DATE CREATED: May 11 2010
#
#############################################################################

sub SearchDir
{
	# Declare local variables
	my($dir) = shift;	    # Directory to search
	my(@files);     	    # List of files in directory
	my($file);	    	    # Individual file in directory

	# Set the group sticky bit for the given directory
	system("chmod", "g+s", $dir) && die "Failed to set sticky bit on $dir $!\n";

	# Open the directory and read in the files - skip . and ..
	opendir(DIR, "$dir") || die "Could not open directory $dir: $!\n";
	@files = grep(!/^\.\.?$/, readdir(DIR));
	closedir(DIR);

	# Change to the directory
	chdir($dir) || die "Could not change to directory $dir: $!\n";

	# Go through the files
	foreach $file (@files)
	{
		# Check if it is a directory but make sure it is not a link
		if (!-l $file && -d $file)
		{
			system("chmod", "g+s", $file) && die "Failed to set sticky bit for
				$file $!\n";
			SearchDir($file);
			chdir("..") || die "Could not change to directory .. from
				$file: $!\n";
		}
	}
}

__END__
