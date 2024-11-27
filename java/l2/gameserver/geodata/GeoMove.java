package l2.gameserver.geodata;

import l2.gameserver.Config;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GeoMove
{
	private static List<Location> findPath(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, boolean isPlayable, int geoIndex)
	{
		if(Math.abs(fromZ - toZ) > 256)
		{
			return Collections.emptyList();
		}
		else
		{
			fromZ = GeoEngine.getHeight(fromX, fromY, fromZ, geoIndex);
			toZ = GeoEngine.getHeight(toX, toY, toZ, geoIndex);
			PathFind n = new PathFind(fromX, fromY, fromZ, toX, toY, toZ, isPlayable, geoIndex);
			if(n.getPath() != null && !n.getPath().isEmpty())
			{
				List<Location> targetRecorder = new ArrayList(n.getPath().size() + 2);
				targetRecorder.add(new Location(fromX, fromY, fromZ));
				Iterator var10 = n.getPath().iterator();
				
				while(var10.hasNext())
				{
					Location p = (Location) var10.next();
					targetRecorder.add(p.geo2world());
				}
				
				targetRecorder.add(new Location(toX, toY, toZ));
				if(Config.PATH_CLEAN)
				{
					pathClean(targetRecorder, geoIndex);
				}
				
				return targetRecorder;
			}
			else
			{
				return Collections.emptyList();
			}
		}
	}
	
	public static List<List<Location>> findMovePath(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, boolean isPlayable, int geoIndex)
	{
		return getNodePath(findPath(fromX, fromY, fromZ, toX, toY, toZ, isPlayable, geoIndex), geoIndex);
	}
	
	private static List<List<Location>> getNodePath(List<Location> path, int geoIndex)
	{
		int size = path.size();
		if(size <= 1)
		{
			return Collections.emptyList();
		}
		else
		{
			List<List<Location>> result = new ArrayList(size);
			
			for(int i = 1;i < size;++i)
			{
				Location p2 = path.get(i);
				Location p1 = path.get(i - 1);
				List<Location> moveList = GeoEngine.MoveList(p1.x, p1.y, p1.z, p2.x, p2.y, geoIndex, true);
				if(moveList == null)
				{
					return Collections.emptyList();
				}
				
				if(!moveList.isEmpty())
				{
					result.add(moveList);
				}
			}
			
			return result;
		}
	}
	
	public static List<Location> constructMoveList(Location begin, Location end)
	{
		begin = begin.world2geo();
		end = end.world2geo();
		int diff_x = end.x - begin.x;
		int diff_y = end.y - begin.y;
		int diff_z = end.z - begin.z;
		int dx = Math.abs(diff_x);
		int dy = Math.abs(diff_y);
		int dz = Math.abs(diff_z);
		float steps = (float) Math.max(Math.max(dx, dy), dz);
		if(steps == 0.0F)
		{
			return Collections.emptyList();
		}
		else
		{
			float step_x = (float) diff_x / steps;
			float step_y = (float) diff_y / steps;
			float step_z = (float) diff_z / steps;
			float next_x = (float) begin.x;
			float next_y = (float) begin.y;
			float next_z = (float) begin.z;
			List<Location> result = new ArrayList((int) steps + 1);
			result.add(new Location(begin.x, begin.y, begin.z));
			
			for(int i = 0;(float) i < steps;++i)
			{
				next_x += step_x;
				next_y += step_y;
				next_z += step_z;
				result.add(new Location((int) (next_x + 0.5F), (int) (next_y + 0.5F), (int) (next_z + 0.5F)));
			}
			
			return result;
		}
	}
	
	private static void pathClean(List<Location> path, int geoIndex)
	{
		int size = path.size();
		int current;
		Location one;
		Location two;
		if(size > 2)
		{
			for(current = 2;current < size;++current)
			{
				Location p3 = path.get(current);
				one = path.get(current - 1);
				two = path.get(current - 2);
				if(two.equals(one) || p3.equals(one) || IsPointInLine(two, one, p3))
				{
					path.remove(current - 1);
					--size;
					current = Math.max(2, current - 2);
				}
			}
		}
		
		for(current = 0;current < path.size() - 2;++current)
		{
			one = path.get(current);
			
			for(int sub = current + 2;sub < path.size();++sub)
			{
				two = path.get(sub);
				if(one.equals(two) || GeoEngine.canMoveWithCollision(one.x, one.y, one.z, two.x, two.y, two.z, geoIndex))
				{
					while(current + 1 < sub)
					{
						path.remove(current + 1);
						--sub;
					}
				}
			}
		}
	}
	
	private static boolean IsPointInLine(Location p1, Location p2, Location p3)
	{
		if((p1.x != p3.x || p3.x != p2.x) && (p1.y != p3.y || p3.y != p2.y))
		{
			return (p1.x - p2.x) * (p1.y - p2.y) == (p2.x - p3.x) * (p2.y - p3.y);
		}
		else
		{
			return true;
		}
	}
	
	public static List<Location> applyGeoIndent(List<Location> points, int geoIndent)
	{
		if(geoIndent <= 0)
		{
			return points;
		}
		else
		{
			long dx = (long) (points.get(points.size() - 1).getX() - points.get(0).getX());
			long dy = (long) (points.get(points.size() - 1).getY() - points.get(0).getY());
			long dz = (long) (points.get(points.size() - 1).getZ() - points.get(0).getZ());
			double distance = Math.sqrt((double) (dx * dx + dy * dy + dz * dz));
			if(distance <= (double) geoIndent)
			{
				Location point = points.get(0);
				points.clear();
				points.add(point);
				return points;
			}
			else
			{
				if(distance >= 1.0D)
				{
					double cut = (double) geoIndent / distance;
					int num = (int) ((double) points.size() * cut + 0.5D);
					
					for(int i = 1;i <= num && points.size() > 0;++i)
					{
						points.remove(points.size() - 1);
					}
				}
				
				return points;
			}
		}
	}
	
	public static List<Location> straightLineGeoPath(Location src, Location dst)
	{
		int diffX = dst.getX() - src.getX();
		int diffY = dst.getY() - src.getY();
		int diffZ = dst.getZ() - src.getZ();
		int dx = Math.abs(diffX);
		int dy = Math.abs(diffY);
		int dz = Math.abs(diffZ);
		float steps = (float) Math.max(Math.max(dx, dy), dz);
		if(steps == 0.0F)
		{
			return Collections.emptyList();
		}
		else
		{
			float stepX = (float) diffX / steps;
			float stepY = (float) diffY / steps;
			float stepZ = (float) diffZ / steps;
			float nextX = (float) src.getX();
			float nextY = (float) src.getY();
			float nextZ = (float) src.getZ();
			List<Location> straightGeoLine = new ArrayList((int) steps + 1);
			straightGeoLine.add(new Location(src.getX(), src.getY(), src.getZ()));
			
			for(int i = 0;(float) i < steps;++i)
			{
				nextX += stepX;
				nextY += stepY;
				nextZ += stepZ;
				straightGeoLine.add(new Location((int) (nextX + 0.5F), (int) (nextY + 0.5F), (int) (nextZ + 0.5F)));
			}
			
			return straightGeoLine;
		}
	}
	
	public static Location getIntersectPoint(Location actorLoc, Location targetLoc, int targetSpd, int timeMs)
	{
		if(timeMs != 0 && targetSpd != 0 && PositionUtils.isFacing(actorLoc, targetLoc, 90))
		{
			double angle = PositionUtils.convertHeadingToDegree(targetLoc.getHeading());
			double radian = Math.toRadians(angle - 90.0D);
			double range = (double) timeMs * ((double) targetSpd / 1000.0D);
			return new Location((int) ((double) targetLoc.getX() - range * Math.sin(radian)), (int) ((double) targetLoc.getY() + range * Math.cos(radian)), targetLoc.getZ());
		}
		else
		{
			return new Location(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ());
		}
	}
	
	public static List<Location> buildGeoLine(Location geoFrom, Location geoTo, int geoIndex, boolean air, boolean water, int waterZ, int indent)
	{
		if(geoFrom.equals(geoTo))
		{
			return Collections.emptyList();
		}
		else
		{
			Location worldFrom = geoFrom.clone().geo2world();
			Location worldTo = geoTo.clone().geo2world();
			Location worldToIndented;
			List geoPathLine;
			if(water)
			{
				worldToIndented = indent > 0 ? worldTo.clone().indent(worldFrom, indent, true) : worldTo;
				geoPathLine = straightLineGeoPath(geoFrom, worldToIndented.clone().world2geo());
				return !geoPathLine.isEmpty() ? geoPathLine : null;
			}
			else if(air)
			{
				worldToIndented = indent > 0 ? worldTo.clone().indent(worldFrom, indent, true) : worldTo;
				Location lastAvailableLoc = GeoEngine.moveCheckInAir(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), 16.0D, geoIndex);
				if(lastAvailableLoc != null && !lastAvailableLoc.equals(worldFrom))
				{
					List<Location> geoStraightLine = straightLineGeoPath(geoFrom, lastAvailableLoc.world2geo());
					return geoStraightLine.isEmpty() ? null : geoStraightLine;
				}
				else
				{
					return null;
				}
			}
			else
			{
				worldToIndented = indent > 0 ? worldTo.clone().indent(worldFrom, indent, false) : worldTo;
				geoPathLine = GeoEngine.MoveList(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), geoIndex, false);
				if(geoPathLine != null)
				{
					return geoPathLine.isEmpty() ? null : geoPathLine;
				}
				else
				{
					return null;
				}
			}
		}
	}
	
	public static boolean buildGeoPath(List<List<Location>> geoPathLines, Location geoFrom, Location geoTo, int geoIndex, int colRadius, int colHeight, int indent, boolean pathfind, boolean isPlayable, boolean air, boolean water, int waterZ, boolean straightLineIgnoreGeo)
	{
		geoPathLines.clear();
		if(geoFrom.equals(geoTo))
		{
			return true;
		}
		else
		{
			Location worldFrom = geoFrom.clone().geo2world();
			Location worldTo = geoTo.clone().geo2world();
			Location worldToIndented = indent > 0 ? worldTo.clone().indent(worldFrom, indent, !water && !air) : worldTo;
			List geoPathLine;
			if(!straightLineIgnoreGeo && Config.ALLOW_GEODATA)
			{
				List geoFoundPathLines;
				Location lastWorldLocInWater;
				if(air)
				{
					if(GeoEngine.canSeeCoord(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ() + colHeight + 64, worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), true, geoIndex))
					{
						geoPathLine = straightLineGeoPath(geoFrom, worldToIndented.world2geo());
						if(geoPathLine.isEmpty())
						{
							return false;
						}
						else
						{
							geoPathLines.add(geoPathLine);
							return true;
						}
					}
					else
					{
						lastWorldLocInWater = GeoEngine.moveCheckInAir(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), (double) colRadius, geoIndex);
						if(lastWorldLocInWater != null && !lastWorldLocInWater.equals(worldFrom))
						{
							geoFoundPathLines = straightLineGeoPath(geoFrom, lastWorldLocInWater.world2geo());
							if(geoFoundPathLines.isEmpty())
							{
								return false;
							}
							else
							{
								geoPathLines.add(geoFoundPathLines);
								return true;
							}
						}
						else
						{
							return false;
						}
					}
				}
				else if(water)
				{
					lastWorldLocInWater = GeoEngine.moveInWaterCheck(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), worldToIndented.getZ(), waterZ, geoIndex);
					geoFoundPathLines = straightLineGeoPath(geoFrom, lastWorldLocInWater.clone().world2geo());
					if(!geoFoundPathLines.isEmpty())
					{
						geoPathLines.add(geoFoundPathLines);
					}
					
					int dz = worldToIndented.getZ() - lastWorldLocInWater.getZ();
					if(!lastWorldLocInWater.clone().world2geo().equals(worldToIndented.clone().world2geo()))
					{
						if(pathfind)
						{
							geoFoundPathLines = findMovePath(lastWorldLocInWater.getX(), lastWorldLocInWater.getY(), lastWorldLocInWater.getZ(), worldTo.getX(), worldTo.getY(), worldTo.getZ(), isPlayable, geoIndex);
							if(!geoFoundPathLines.isEmpty())
							{
								if(indent > 0)
								{
									List<Location> lastGeoFoundPathLine = (List) geoFoundPathLines.remove(geoFoundPathLines.size() - 1);
									lastGeoFoundPathLine = applyGeoIndent(lastGeoFoundPathLine, indent >> 4);
									if(!lastGeoFoundPathLine.isEmpty())
									{
										geoFoundPathLines.add(lastGeoFoundPathLine);
									}
								}
								
								if(!geoFoundPathLines.isEmpty())
								{
									geoPathLines.addAll(geoFoundPathLines);
								}
							}
						}
						else
						{
							geoFoundPathLines = GeoEngine.MoveList(lastWorldLocInWater.getX(), lastWorldLocInWater.getY(), lastWorldLocInWater.getZ(), worldTo.getX(), worldTo.getY(), geoIndex, false);
							if(geoFoundPathLines != null && !geoFoundPathLines.isEmpty())
							{
								geoPathLines.add(geoFoundPathLines);
							}
						}
					}
					
					return !geoPathLines.isEmpty();
				}
				else
				{
					geoPathLine = GeoEngine.MoveList(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldTo.getX(), worldTo.getY(), geoIndex, true);
					if(geoPathLine != null)
					{
						if(geoPathLine.isEmpty())
						{
							return false;
						}
						else
						{
							geoPathLine = applyGeoIndent(geoPathLine, indent >> 4);
							if(geoPathLine.isEmpty())
							{
								return false;
							}
							else
							{
								geoPathLines.add(geoPathLine);
								return true;
							}
						}
					}
					else
					{
						if(pathfind)
						{
							geoFoundPathLines = findMovePath(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldTo.getX(), worldTo.getY(), worldTo.getZ(), isPlayable, geoIndex);
							if(!geoFoundPathLines.isEmpty())
							{
								if(indent > 0)
								{
									List<Location> lastGeoFoundPathLine = (List) geoFoundPathLines.remove(geoFoundPathLines.size() - 1);
									lastGeoFoundPathLine = applyGeoIndent(lastGeoFoundPathLine, indent >> 4);
									if(!lastGeoFoundPathLine.isEmpty())
									{
										geoFoundPathLines.add(lastGeoFoundPathLine);
									}
								}
								
								if(!geoFoundPathLines.isEmpty())
								{
									geoPathLines.addAll(geoFoundPathLines);
									return true;
								}
							}
						}
						
						geoPathLine = GeoEngine.MoveList(worldFrom.getX(), worldFrom.getY(), worldFrom.getZ(), worldToIndented.getX(), worldToIndented.getY(), geoIndex, false);
						if(geoPathLine != null)
						{
							if(geoPathLine.size() < 2)
							{
								return false;
							}
							else
							{
								geoPathLines.add(geoPathLine);
								return true;
							}
						}
						else
						{
							return false;
						}
					}
				}
			}
			else
			{
				geoPathLine = straightLineGeoPath(geoFrom, worldToIndented.world2geo());
				if(geoPathLine.isEmpty())
				{
					return false;
				}
				else
				{
					geoPathLines.add(geoPathLine);
					return true;
				}
			}
		}
	}
}