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
	  
	public String toString(){
		return super.toString().toLowerCase();
	}

}
