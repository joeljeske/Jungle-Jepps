package org.yajasi.JungleJepps.jjtp;

import org.yajasi.JungleJepps.Runway;
import org.yajasi.JungleJepps.db.DatabaseConnection;

public class Client implements DatabaseConnection{
	

	public static void main(String[] args){
	
	}

	@Override
	public String[] getAllRunwayIds() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runway getRunway(String runwayId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean updateRunway(Runway runway) {
		throw new UnsupportedOperationException();
	}

	
}
