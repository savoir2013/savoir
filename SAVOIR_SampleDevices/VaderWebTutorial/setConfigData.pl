#! /usr/bin/perl

# Licensed under Apache 2.0
# Copyright 2011, National Research Council of Canada
# Property of Lakehead University


#############################################################################
#              National Research Council -- IIT - Fredericton
#
# PROJECT:      SAVOIR
# AUTHOR(S):    Justin Hickey
# PURPOSE:      To set properties inside the Vader web tutorial .xml file.
#				This should eventually be replaced with an Ant task that can
#				edit the XML file properly. For now, we use this script since
#				it was quicker to write.
# CMD LINE ARG: none
# OUTPUT:       none
# DATE CREATED: Nov 04 2011
# LAST MOD: 	$Date:$
# LAST MOD BY:	$Author: hickeyj $
# USAGE:    	For internal NRC use only
# COMMENTS: 	none
#
#############################################################################

# ****************************** Main Script ********************************

# Declare variables
my($xmlFile);		# File name of source xml file
my(@xml);			# Contents of the xml file
my($line);  		# Single line from one of the files
my($host);			# Host address of the web server
my($contactName);	# Contact name for the device
my($contactEmail);	# Contact email for the device

# Use any pragmas
use strict;
use English;

# Open the build.properties file
open(PROP, "build.properties") || die "Failed to open build.properties: $OS_ERROR\n";

# Start reading the properties file
while ($line = <PROP>)
{ 
	# Check if this is the contact name property
    if ($line =~ /^savoir\.vdrw\.deploy\.contact\.name=/)
    {
		# Save the property value
		$contactName = $POSTMATCH;
		chomp($contactName);
		
		# Get rid of any back slashes
		$contactName =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$contactName =~ s/\/*$//;
    }

	# Check if this is the contact email property
    if ($line =~ /^savoir\.vdrw\.deploy\.contact\.email=/)
    {
		# Save the property value
		$contactEmail = $POSTMATCH;
		chomp($contactEmail);
		
		# Get rid of any back slashes
		$contactEmail =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$contactEmail =~ s/\/*$//;
    }

	# Check if this is the Vader Web deploy host property
    if ($line =~ /^savoir\.vdrw\.deploy\.host=/)
    {
		# Save the property value
		$host = $POSTMATCH;
		chomp($host);
		
		# Get rid of any back slashes
		$host =~ s/\\//g;
		
		# Get rid of any trailing slashes
		$host =~ s/\/*$//;
    }
}

# Close the property file
close(PROP);

# Read in the xml file
open(XML, "resources/169_VaderWebTutorial_NRC.src.xml") ||
	die "Failed to open profile xml source file: $OS_ERROR\n";
@xml = <XML>;
close(XML);

# Open the xml file for output
open(OUT, ">resources/169_VaderWebTutorial_NRC.xml") ||
	die "Failed to open profile xml output file: $OS_ERROR\n";

# Go through the file and replace the properties
foreach $line (@xml)
{
	if ($line =~ /contactName="someone"/)
	{
		$line =~ s/someone/$contactName/;
		$line =~ s/\\//g;
	}
	
	if ($line =~ /contactEmail="somebody\@some.com"/)
	{
		$line =~ s/somebody\@some.com/$contactEmail/;
		$line =~ s/\\//g;
	}
	
	if ($line =~ /ipAddress="0\.0\.0\.0"/)
	{
		$line =~ s/0\.0\.0\.0/$host/;
		$line =~ s/\\//g;
	}
	
	if ($line =~ /http:\/\/0\.0\.0\.0/)
	{
		$line =~ s/0\.0\.0\.0/$host/g;
		$line =~ s/\\//g;
	}
	
	print OUT $line;
}

# Close the xml file
close(OUT);

# Exit the script
exit (0);

# ************************* Internal Procedures *****************************

# none

__END__
