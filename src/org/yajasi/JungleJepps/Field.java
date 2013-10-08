package org.yajasi.JungleJepps;

public class Field {
	
	private Class<?> type;
	private String name;
	private Object value; 
	private boolean isModified;
	private boolean isReadOnly;
	
	public Field(String name, Class<?> type, Object value, boolean isReadOnly){
		this.name = name;
		this.type = type;
		this.value = value;
		this.isReadOnly = isReadOnly;
		this.isModified = false;
	}
	
	public boolean isType(Class<?> type){
		return this.type.equals( type );
	}
	
	public String getName(){
		return name;
	}
	
	public Object getValue(){
		return value;
	}
	
	public void setValue(Object value){
		if( this.isReadOnly )
			throw new UnsupportedOperationException("This field is readonly. Field: " + this.name);
		
		this.value = value;
		this.isModified = true;
	}
	
	public boolean isModified(){
		return isModified;
	}
	
	public void clearIsModified(){
		this.isModified = false;
	}
	
	public boolean isReadOnly(){
		return isReadOnly;
	}

}
