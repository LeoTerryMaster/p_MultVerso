package l2.gameserver.utils;

import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;

public class PositionUtils
{
	private static final int MAX_ANGLE = 360;
	private static final double FRONT_MAX_ANGLE = 100.0;
	private static final double BACK_MAX_ANGLE = 40.0;
	
	public static TargetDirection getDirectionTo(Creature target, Creature attacker)
	{
		if(target == null || attacker == null)
		{
			return TargetDirection.NONE;
		}
		if(isBehind(target, attacker))
		{
			return TargetDirection.BEHIND;
		}
		if(isInFrontOf(target, attacker))
		{
			return TargetDirection.FRONT;
		}
		return TargetDirection.SIDE;
	}
	
	public static boolean isInFrontOf(Creature target, Creature attacker)
	{
		if(target == null)
		{
			return false;
		}
		double angleTarget = calculateAngleFrom(target, attacker);
		double angleChar = convertHeadingToDegree(target.getHeading());
		double angleDiff = angleChar - angleTarget;
		if(angleDiff <= -260.0)
		{
			angleDiff += 360.0;
		}
		if(angleDiff >= 260.0)
		{
			angleDiff -= 360.0;
		}
		return Math.abs(angleDiff) <= 100.0;
	}
	
	public static boolean isBehind(Creature target, Creature attacker)
	{
		if(target == null)
		{
			return false;
		}
		double angleChar = calculateAngleFrom(attacker, target);
		double angleTarget = convertHeadingToDegree(target.getHeading());
		double angleDiff = angleChar - angleTarget;
		if(angleDiff <= -320.0)
		{
			angleDiff += 360.0;
		}
		if(angleDiff >= 320.0)
		{
			angleDiff -= 360.0;
		}
		return Math.abs(angleDiff) <= 40.0;
	}
	
	public static boolean isFacing(Creature attacker, GameObject target, int maxAngle)
	{
		if(target == null)
		{
			return false;
		}
		double maxAngleDiff = maxAngle / 2;
		double angleTarget = calculateAngleFrom(attacker, target);
		double angleChar = convertHeadingToDegree(attacker.getHeading());
		double angleDiff = angleChar - angleTarget;
		if(angleDiff <= -360.0 + maxAngleDiff)
		{
			angleDiff += 360.0;
		}
		if(angleDiff >= 360.0 - maxAngleDiff)
		{
			angleDiff -= 360.0;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public static boolean isFacing(Location attackerLoc, Location targetLoc, int maxAngle)
	{
		if(attackerLoc == null)
		{
			return false;
		}
		double maxAngleDiff = maxAngle / 2;
		double angleTarget = calculateAngleFrom(attackerLoc.getX(), attackerLoc.getY(), targetLoc.getX(), targetLoc.getY());
		double angleChar = convertHeadingToDegree(targetLoc.getH());
		double angleDiff = angleChar - angleTarget;
		if(angleDiff <= -360.0 + maxAngleDiff)
		{
			angleDiff += 360.0;
		}
		if(angleDiff >= 360.0 - maxAngleDiff)
		{
			angleDiff -= 360.0;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public static int calculateHeadingFrom(GameObject obj1, GameObject obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if(angleTarget < 0.0)
		{
			angleTarget = 360.0 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	public static double calculateAngleFrom(GameObject obj1, GameObject obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if(angleTarget < 0.0)
		{
			angleTarget = 360.0 + angleTarget;
		}
		return angleTarget;
	}
	
	public static boolean checkIfInRange(int range, int x1, int y1, int x2, int y2)
	{
		return checkIfInRange(range, x1, y1, 0, x2, y2, 0, false);
	}
	
	public static boolean checkIfInRange(int range, int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		long dx = x1 - x2;
		long dy = y1 - y2;
		if(includeZAxis)
		{
			long dz = z1 - z2;
			return dx * dx + dy * dy + dz * dz <= (long) (range * range);
		}
		return dx * dx + dy * dy <= (long) (range * range);
	}
	
	public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if(obj1 == null || obj2 == null)
		{
			return false;
		}
		return checkIfInRange(range, obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}
	
	public static double convertHeadingToDegree(int heading)
	{
		return (double) heading / 182.044444444;
	}
	
	public static double convertHeadingToRadian(int heading)
	{
		return Math.toRadians(convertHeadingToDegree(heading) - 90.0);
	}
	
	public static int convertDegreeToClientHeading(double degree)
	{
		if(degree < 0.0)
		{
			degree = 360.0 + degree;
		}
		return (int) (degree * 182.044444444);
	}
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}
	
	public static double calculateDistance(Location locA, Location locB, boolean includeZAxis)
	{
		return calculateDistance(locA.getX(), locA.getY(), locA.getZ(), locB.getX(), locB.getY(), locB.getZ(), includeZAxis);
	}
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		long dx = x1 - x2;
		long dy = y1 - y2;
		if(includeZAxis)
		{
			long dz = z1 - z2;
			return Math.sqrt(dx * dx + dy * dy + dz * dz);
		}
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public static double calculateDistance(GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if(obj1 == null || obj2 == null)
		{
			return 2.147483647E9;
		}
		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}
	
	public static double getDistance(GameObject a1, GameObject a2)
	{
		return getDistance(a1.getX(), a2.getY(), a2.getX(), a2.getY());
	}
	
	public static double getDistance(Location loc1, Location loc2)
	{
		return getDistance(loc1.getX(), loc1.getY(), loc2.getX(), loc2.getY());
	}
	
	public static double getDistance(int x1, int y1, int x2, int y2)
	{
		return Math.hypot(x1 - x2, y1 - y2);
	}
	
	public static int getHeadingTo(GameObject actor, GameObject target)
	{
		if(actor == null || target == null || target == actor)
		{
			return -1;
		}
		return getHeadingTo(actor.getLoc(), target.getLoc());
	}
	
	public static int getHeadingTo(Location actor, Location target)
	{
		if(actor == null || target == null || target.equals(actor))
		{
			return -1;
		}
		int dy = target.y - actor.y;
		int dx = target.x - actor.x;
		int heading = target.h - (int) (Math.atan2(-dy, -dx) * 10430.378350470453 + 32768.0);
		if(heading < 0)
		{
			heading = heading + 1 + Integer.MAX_VALUE & 65535;
		}
		else if(heading > 65535)
		{
			heading &= 65535;
		}
		return heading;
	}
	
	public enum TargetDirection
	{
		NONE,
		FRONT,
		SIDE,
		BEHIND;
	}
}