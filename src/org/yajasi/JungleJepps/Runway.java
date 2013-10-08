package org.yajasi.JungleJepps;

import java.util.Map;

public class Runway {
	
	// All the fields have string identifiers. Will be used in database
	public static final String RUNWAY_ID = "id";
	public static final String RUNWAY_NAME = "name";

	private Map<String, Field> fields; 
	
	public Runway(){
		
	}
	
	public boolean isModified(){
		for(Field f : fields.values())
			if( f.isModified() )
				return true;
		
		return false;
	}
	
	public Field getField(String field_name){
		return fields.get( field_name );
	}
	
	
}
