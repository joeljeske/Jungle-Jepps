package org.yajasi.JungleJepps;
/*
 * Add highlight fields as on db pdf
 */
public enum Field {
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
	  LENGTH,
	  WIDTH_TEXT,
	  TDZ_SLOPE,
	  IAS_ADJUSTMENT,
	  PRECIPITATION_ON_SCREEN,
	  RUNWAY_A,
	  A_TAKEOFF_RESTRICTION,
	  A_TAKEOFF_NOTE,
	  A_LANDING_RESTRICTION,
	  A_LANDING_NOTE,
	  RUNWAY_B,
	  B_TAKEOFF_RESTRICTION,
	  B_TAKEOFF_NOTE,
	  B_LANDING_RESTRICTION,
	  B_LANDING_NOTE,
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
	  
	public String toString(){
		return super.toString().toLowerCase();
	}

}