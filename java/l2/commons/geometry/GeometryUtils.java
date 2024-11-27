package l2.commons.geometry;

public class GeometryUtils
{
	private GeometryUtils()
	{
	}
	
	public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d)
	{
		return checkIfLinesIntersects(a, b, c, d, null);
	}
	
	public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r)
	{
		if(a.x == b.x && a.y == b.y || c.x == d.x && c.y == d.y)
		{
			return false;
		}
		double Bx = b.x - a.x;
		double By = b.y - a.y;
		double Cx = c.x - a.x;
		double Cy = c.y - a.y;
		double Dx = d.x - a.x;
		double Dy = d.y - a.y;
		double distAB = Math.sqrt(Bx * Bx + By * By);
		double theCos = Bx / distAB;
		double theSin = By / distAB;
		double newX = Cx * theCos + Cy * theSin;
		Cy = (int) (Cy * theCos - Cx * theSin);
		Cx = newX;
		newX = Dx * theCos + Dy * theSin;
		Dy = (int) (Dy * theCos - Dx * theSin);
		Dx = newX;
		if(Cy == Dy)
		{
			return false;
		}
		double ABpos = Dx + (Cx - Dx) * Dy / (Dy - Cy);
		if(r != null)
		{
			r.x = (int) ((double) a.x + ABpos * theCos);
			r.y = (int) ((double) a.y + ABpos * theSin);
		}
		return true;
	}
	
	public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d)
	{
		return checkIfLineSegementsIntersects(a, b, c, d, null);
	}
	
	public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r)
	{
		if(a.x == b.x && a.y == b.y || c.x == d.x && c.y == d.y)
		{
			return false;
		}
		if(a.x == c.x && a.y == c.y || b.x == c.x && b.y == c.y || a.x == d.x && a.y == d.y || b.x == d.x && b.y == d.y)
		{
			return false;
		}
		double Bx = b.x - a.x;
		double By = b.y - a.y;
		double Cx = c.x - a.x;
		double Cy = c.y - a.y;
		double Dx = d.x - a.x;
		double Dy = d.y - a.y;
		double distAB = Math.sqrt(Bx * Bx + By * By);
		double theCos = Bx / distAB;
		double theSin = By / distAB;
		double newX = Cx * theCos + Cy * theSin;
		Cy = (int) (Cy * theCos - Cx * theSin);
		Cx = newX;
		newX = Dx * theCos + Dy * theSin;
		Dy = (int) (Dy * theCos - Dx * theSin);
		Dx = newX;
		if(Cy < 0.0 && Dy < 0.0 || Cy >= 0.0 && Dy >= 0.0)
		{
			return false;
		}
		double ABpos = Dx + (Cx - Dx) * Dy / (Dy - Cy);
		if(ABpos < 0.0 || ABpos > distAB)
		{
			return false;
		}
		if(r != null)
		{
			r.x = (int) ((double) a.x + ABpos * theCos);
			r.y = (int) ((double) a.y + ABpos * theSin);
		}
		return true;
	}
}