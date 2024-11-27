package l2.commons.geometry;

public interface Shape
{
	boolean isInside(int x, int y);
	
	boolean isInside(int x, int y, int z);
	
	int getXmax();
	
	int getXmin();
	
	int getYmax();
	
	int getYmin();
	
	int getZmax();
	
	int getZmin();
}