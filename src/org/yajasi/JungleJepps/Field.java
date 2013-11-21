/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: Field.java
// Class: org.yajasi.JungleJepps.Field
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
// Objective: This file represents a Java ENUM. This enumerated type
// is a list of every possible field that pertaining to a Runway, Aircraft,
// and Runway-Aircraft relationship. This ENUM is used to generate Strings
// pertaining to Fields and Field-relevant information (e.g. column override
// names, field labels, etc...). To generate the string, use the toString()
// instance method. There is a standard naming convention used here: 
// All capital letters, spaces represented as '_'. The majority of these
// fields are named directly as stated in the SRS V1.0.1 Section 3.4.8.3.
// The "_HL" append represents a boolean typed value if stating if a field
// should be highlighted on the PDF Runway char.
//
/////////////////////////////////////////////////////////////////////////

package org.yajasi.JungleJepps;

public enum Field implements ValueByEnum.JungleJeppsEnum {
	  RUNWAY_IDENTIFIER, 
	  RUNWAY_NAME,
	  AIRCRAFT_IDENTIFIER,
	  LONGITUDE,
	  LATITUDE,
	  INSPECTION_NA,
	  INSPECTION_DATE,
	  INSPECTOR_NAME,
	  INSPECTION_DUE,
	  CLASSIFICATION,
	  FREQUENCY_1,
	  FREQUENCY_2,
	  LANGUAGE_GREET,
	  ELEVATION,
      ELEVATION_HL,
	  LENGTH,
      LENGTH_HL,
	  WIDTH_TEXT,
      WIDTH_TEXT_HL,
	  TDZ_SLOPE,
      TDZ_SLOPE_HL,
	  IAS_ADJUSTMENT,
      IAS_ADJUSTMENT_HL,
	  PRECIPITATION_ON_SCREEN,
      PRECIPITATION_ON_SCREEN_HL,
	  RUNWAY_A,
      RUNWAY_A_HL,
	  A_TAKEOFF_RESTRICTION,
      A_TAKEOFF_RESTRICTION_HL,
	  A_TAKEOFF_NOTE,
      A_TAKEOFF_NOTE_HL, 
	  A_LANDING_RESTRICTION,
      A_LANDING_RESTRICTION_HL,
	  A_LANDING_NOTE,
      A_LANDING_NOTE_HL,
	  RUNWAY_B,
      RUNWAY_B_HL,
	  B_TAKEOFF_RESTRICTION,
      B_TAKEOFF_RESTRICTION_HL,
	  B_TAKEOFF_NOTE,
      B_TAKEOFF_NOTE_HL,
	  B_LANDING_RESTRICTION,
      B_LANDING_RESTRICTION_HL,
	  B_LANDING_NOTE,
      B_LANDING_NOTE_HL,
	  P1_TEXT_1,
	  P1_TEXT_2,
	  P1_TEXT_3,
	  P1_TEXT_4,
	  P1_TEXT_5,
	  P1_TEXT_6,
	  P1_TEXT_7,
	  P2_TEXT_1,
	  P2_TEXT_2,
	  P2_TEXT_3,
	  P2_TEXT_4,
	  P2_TEXT_5,
	  P2_TEXT_6,
	  P2_TEXT_7,
	  PDF_PATH,
	  IMAGE_PATH;
	  
	/**
	 * This method converts the literal value of the ENUM to 
	 * a String. The string is returned in lower case and should be 
	 * used whenever a persistent or portable field identifier is 
	 * needed.
	 * @return Literal value of the ENUM in lower case
	 */
	public String toString(){
		return super.toString().toLowerCase();
	}

}
