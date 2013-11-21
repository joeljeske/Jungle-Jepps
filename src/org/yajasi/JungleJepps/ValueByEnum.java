package org.yajasi.JungleJepps;

public interface ValueByEnum {
	
	/**
	 * Must be able to get a string for a JungleJeppsEnum
	 * @param key
	 * @return
	 */
	String get(JungleJeppsEnum key);
	
	
	public interface JungleJeppsEnum {}
}




