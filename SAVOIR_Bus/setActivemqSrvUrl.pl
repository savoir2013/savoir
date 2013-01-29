#! /usr/bin/perl

# Licensed under Apache 2.0
# Copyright 2011, National Research Council of Canada
# Property of Lakehead University


#############################################################################
#              National Research Council -- IIT - Fredericton
#
# PROJECT:      SAVOIR
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To set the mule server bus config file
#				mule/savoir-esb/conf/savoir-bus-config.xml. This should
#				eventually be replaced with an Ant task that can edit the
#				XML file properly. For now, we use this script since it was
#				quicker to write.
# CMD LINE ARG: savoir bus config file name
# OUTPUT:       none
# DATE CREATED: Sep 07 2011
# LAST MOD: 	$Date:$
# LAST MOD BY:	$Author: hickeyj $
# USAGE:    	For internal NRC use only
# COMMENTS: 	none
#
#############################################################################

# ****************************** Main Script ********************************

# Declare variables
my($configFile);	# File name of source config file
my(@xml);			# Contents of the XML file
my($line);  		# Single line from one of the files
my($url);			# URL of the mule server

# Use any pragmas
use strict;
use English;

# Check the command line for arguments
# The $#ARGV syntax returns the value of the last index
if ($#ARGV == 0)
{
    $configFile = $ARGV[0];
}
else
{
    die "Usage: setActivemqSrvUrl.pl <savoir bus config file>\n";
}

# Open the build.properties file
open(PROP, "build.properties") || die "Failed to open build.properties: $!\n";

# Start reading the properties file
while ($line = <PROP>)
{ 
	# Check if this is the mule server URL property
    if ($line =~ /^savoir\.activemq\.url=/)
    {
		# Save the url
		$url = $POSTMATCH;
		chomp($url);
		
		# Get rid of any back slashes
		$url =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$url =~ s/\/*$//;
		
		# Stop reading the file once we have the data
		last;
    }
}

# Close the property file
close(PROP);

# Read in the XML file
open(XML, "conf/$configFile") || die "Failed to open XML: $!\n";
@xml = <XML>;
close(XML);

# Open the XML file for output
open(OUT, ">dist/savoir-bus-config.xml") || die "Failed to open XML: $!\n";

# Go through the file and replace the URL
foreach $line (@xml)
{
	if ($line =~ /0\.0\.0\.0:61616/)
	{
		$line =~ s/tcp:\/\/0\.0\.0\.0/$url/;
		$line =~ s/\\//g;
	}
	
	print OUT $line;
}

# Close the XML file
close(OUT);

# Exit the script
exit (0);

# ************************* Internal Procedures *****************************

# none

__END__
