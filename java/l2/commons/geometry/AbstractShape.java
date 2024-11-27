package l2.commons.geometry;

public abstract class AbstractShape implements Shape
{
	protected final Point3D max = new Point3D();
	protected final Point3D min = new Point3D();
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		return min.z <= z && max.z >= z && isInside(x, y);
	}
	
	@Override
	public int getXmax()
	{
		return max.x;
	}
	
	@Override
	public int getXmin()
	{
		return min.x;
	}
	
	@Override
	public int getYmax()
	{
		return max.y;
	}
	
	@Override
	public int getYmin()
	{
		return min.y;
	}
	
	@Override
	public int getZmax()
	{
		return max.z;
	}
	
	public AbstractShape setZmax(int z)
	{
		max.z = z;
		return this;
	}
	
	@Override
	public int getZmin()
	{
		return min.z;
	}
	
	public AbstractShape setZmin(int z)
	{
		min.z = z;
		return this;
	}
}