package org.yajasi.JungleJepps;

import com.sun.media.sound.InvalidFormatException;

public class Longitude {
	
	private double decimal;
	
	public static void main(String[] args){
		
		
	}
	
	public Longitude(double decimal){
		this.decimal = decimal;
	}
	
	public static Longitude parse(String value) throws InvalidFormatException{
		double decimal;
		try {
			decimal = Double.valueOf(value);
			
		} catch(NumberFormatException e1){
			decimal = toDecimal(value);
		}
		
		return new Longitude(decimal);
	}
	
	private static double toDecimal(String dms) throws InvalidFormatException{
		char region;
		
		if(dms == null)
			throw new NullPointerException();
		
		dms = dms.trim();
		region = Character.toUpperCase( dms.charAt(0) );
		
		
		String dmsMod = dms.substring(2, dms.length());
		
		String[] dmsBreakdown = dmsMod.split(":");
		double degs = 0, mins = 0, secs = 0;
		
		switch(dmsBreakdown.length)
		{
			case 2: secs = Double.valueOf( dmsBreakdown[2] );
			case 1: mins = Double.valueOf( dmsBreakdown[1] );
			case 0: degs = Double.valueOf( dmsBreakdown[0] );
				break;
			default: throw new InvalidFormatException("Found more than 2 ':' in the DMS input string");
		}
		/*
		if(region == 'E')
			
		else if(region == 'W')
			
		else
			throw new InvalidFormatException("Longitude format did not begin with a 'E' or 'W'");
		*/
		return toDecimal(degs, mins, secs);
		
	}
	
	private static String toDms(double decimal){
		throw new UnsupportedOperationException();
	}
	
	private static double toDecimal(double degs, double minutes, double seconds){
		return degs + minutes / 60 + seconds / 3600;
	}
	
}
