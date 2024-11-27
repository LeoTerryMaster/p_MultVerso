package l2.gameserver.geodata;

import l2.commons.geometry.Shape;

public interface GeoCollision
{
	Shape getShape();
	
	byte[][] getGeoAround();
	
	void setGeoAround(byte[][] geo);
	
	boolean isConcrete();
}