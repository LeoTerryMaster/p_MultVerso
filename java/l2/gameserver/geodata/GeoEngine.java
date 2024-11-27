package l2.gameserver.geodata;

import l2.commons.geometry.Shape;
import l2.commons.util.NaturalOrderComparator;
import l2.gameserver.Config;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.World;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoEngine
{
	public static final byte EAST = 1;
	public static final byte WEST = 2;
	public static final byte SOUTH = 4;
	public static final byte NORTH = 8;
	public static final byte NSWE_ALL = 15;
	public static final byte NSWE_NONE = 0;
	public static final byte BLOCKTYPE_FLAT = 0;
	public static final byte BLOCKTYPE_COMPLEX = 1;
	public static final byte BLOCKTYPE_MULTILEVEL = 2;
	public static final int BLOCKS_IN_MAP = 65536;
	private static final Logger _log = LoggerFactory.getLogger(GeoEngine.class);
	private static final ByteBuffer[][] rawgeo = new ByteBuffer[World.WORLD_SIZE_X][World.WORLD_SIZE_Y];
	private static final byte[][][][][] geodata = new byte[World.WORLD_SIZE_X][World.WORLD_SIZE_Y][1][][];
	public static int MAX_LAYERS = 1;
	
	public static short getType(int x, int y, int geoIndex)
	{
		return NgetType(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, geoIndex);
	}
	
	public static int getHeight(Location loc, int geoIndex)
	{
		return getHeight(loc.x, loc.y, loc.z, geoIndex);
	}
	
	public static int getHeight(int x, int y, int z, int geoIndex)
	{
		return NgetHeight(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, z, geoIndex);
	}
	
	public static boolean canMoveToCoord(int x, int y, int z, int tx, int ty, int tz, int geoIndex)
	{
		return canMove(x, y, z, tx, ty, tz, false, geoIndex) == 0;
	}
	
	public static byte getNSWE(int x, int y, int z, int geoIndex)
	{
		return NgetNSWE(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, z, geoIndex);
	}
	
	public static Location moveCheck(int x, int y, int z, int tx, int ty, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, false, false, false, geoIndex);
	}
	
	public static Location moveCheck(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, false, false, returnPrev, geoIndex);
	}
	
	public static Location moveCheckWithCollision(int x, int y, int z, int tx, int ty, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, true, false, false, geoIndex);
	}
	
	public static Location moveCheckWithCollision(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, true, false, returnPrev, geoIndex);
	}
	
	public static Location moveCheckBackward(int x, int y, int z, int tx, int ty, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, false, true, false, geoIndex);
	}
	
	public static Location moveCheckBackward(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, false, true, returnPrev, geoIndex);
	}
	
	public static Location moveCheckBackwardWithCollision(int x, int y, int z, int tx, int ty, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, true, true, false, geoIndex);
	}
	
	public static Location moveCheckBackwardWithCollision(int x, int y, int z, int tx, int ty, boolean returnPrev, int geoIndex)
	{
		return MoveCheck(x, y, z, tx, ty, true, true, returnPrev, geoIndex);
	}
	
	public static Location moveInWaterCheck(int x, int y, int z, int tx, int ty, int tz, int waterZ, int geoIndex)
	{
		return MoveInWaterCheck(x - World.MAP_MIN_X >> 4, y - World.MAP_MIN_Y >> 4, z, tx - World.MAP_MIN_X >> 4, ty - World.MAP_MIN_Y >> 4, tz, waterZ, geoIndex);
	}
	
	public static Location moveCheckForAI(Location loc1, Location loc2, int geoIndex)
	{
		return MoveCheckForAI(loc1.x - World.MAP_MIN_X >> 4, loc1.y - World.MAP_MIN_Y >> 4, loc1.z, loc2.x - World.MAP_MIN_X >> 4, loc2.y - World.MAP_MIN_Y >> 4, geoIndex);
	}
	
	public static Location moveCheckInAir(int x, int y, int z, int tx, int ty, int tz, double collision, int geoIndex)
	{
		int tgx = tx - World.MAP_MIN_X >> 4;
		int tgy = ty - World.MAP_MIN_Y >> 4;
		int nz = NgetHeight(tgx, tgy, tz, geoIndex);
		if(tz <= nz + 32)
		{
			tz = nz + 32;
		}
		int gy = y - World.MAP_MIN_Y >> 4;
		int gx = x - World.MAP_MIN_X >> 4;
		Location result;
		if((result = canSee(gx, gy, z, tgx, tgy, tz, true, geoIndex)).equals(gx, gy, z))
		{
			return null;
		}
		return result.geo2world();
	}
	
	public static boolean canSeeTarget(GameObject actor, GameObject target, boolean air)
	{
		if(target == null)
		{
			return false;
		}
		if(target instanceof GeoCollision || actor.equals(target))
		{
			return true;
		}
		return canSeeCoord(actor, target.getX(), target.getY(), target.getZ() + (int) target.getColHeight() + 64, air);
	}
	
	public static boolean canSeeCoord(GameObject actor, int tx, int ty, int tz, boolean air)
	{
		return actor != null && canSeeCoord(actor.getX(), actor.getY(), actor.getZ() + (int) actor.getColHeight() + 64, tx, ty, tz, air, actor.getGeoIndex());
	}
	
	public static boolean canSeeCoord(int x, int y, int z, int tx, int ty, int tz, boolean air, int geoIndex)
	{
		int mx = x - World.MAP_MIN_X >> 4;
		int my = y - World.MAP_MIN_Y >> 4;
		int tmx = tx - World.MAP_MIN_X >> 4;
		int tmy = ty - World.MAP_MIN_Y >> 4;
		return canSee(mx, my, z, tmx, tmy, tz, air, geoIndex).equals(tmx, tmy, tz) && canSee(tmx, tmy, tz, mx, my, z, air, geoIndex).equals(mx, my, z);
	}
	
	public static boolean canMoveWithCollision(int x, int y, int z, int tx, int ty, int tz, int geoIndex)
	{
		return canMove(x, y, z, tx, ty, tz, true, geoIndex) == 0;
	}
	
	public static boolean checkNSWE(byte NSWE, int x, int y, int tx, int ty)
	{
		if(NSWE == 15)
		{
			return true;
		}
		if(NSWE == 0)
		{
			return false;
		}
		if(tx > x ? (NSWE & 1) == 0 : tx < x && (NSWE & 2) == 0)
		{
			return false;
		}
		return ty > y ? (NSWE & 4) != 0 : ty >= y || (NSWE & 8) != 0;
	}
	
	public static String geoXYZ2Str(int _x, int _y, int _z)
	{
		return "(" + ((_x << 4) + World.MAP_MIN_X + 8) + " " + ((_y << 4) + World.MAP_MIN_Y + 8) + " " + _z + ")";
	}
	
	public static String NSWE2Str(byte nswe)
	{
		String result = "";
		if((nswe & 8) == 8)
		{
			result = result + "N";
		}
		if((nswe & 4) == 4)
		{
			result = result + "S";
		}
		if((nswe & 2) == 2)
		{
			result = result + "W";
		}
		if((nswe & 1) == 1)
		{
			result = result + "E";
		}
		return result.isEmpty() ? "X" : result;
	}
	
	private static boolean NLOS_WATER(int x, int y, int z, int next_x, int next_y, int next_z, int geoIndex)
	{
		short[] layers1 = new short[MAX_LAYERS + 1];
		short[] layers2 = new short[MAX_LAYERS + 1];
		NGetLayers(x, y, layers1, geoIndex);
		NGetLayers(next_x, next_y, layers2, geoIndex);
		if(layers1[0] == 0 || layers2[0] == 0)
		{
			return true;
		}
		int z2 = -32768;
		short h;
		for(int i = 1;i <= layers2[0];++i)
		{
			h = (short) ((short) (layers2[i] & 65520) >> 1);
			if(Math.abs(next_z - z2) <= Math.abs(next_z - h))
				continue;
			z2 = h;
		}
		if(next_z + 32 >= z2)
		{
			return true;
		}
		int z3 = -32768;
		for(int i = 1;i <= layers2[0];++i)
		{
			h = (short) ((short) (layers2[i] & 65520) >> 1);
			if(h >= z2 + Config.MIN_LAYER_HEIGHT || Math.abs(next_z - z3) <= Math.abs(next_z - h))
				continue;
			z3 = h;
		}
		if(z3 == -32768)
		{
			return false;
		}
		int z1 = -32768;
		byte NSWE1 = 15;
		for(int i = 1;i <= layers1[0];++i)
		{
			h = (short) ((short) (layers1[i] & 65520) >> 1);
			if(h >= z + Config.MIN_LAYER_HEIGHT || Math.abs(z - z1) <= Math.abs(z - h))
				continue;
			z1 = h;
			NSWE1 = (byte) (layers1[i] & 15);
		}
		return checkNSWE(NSWE1, x, y, next_x, next_y);
	}
	
	private static int FindNearestLowerLayer(short[] layers, int z)
	{
		short nearest_layer_h = -32768;
		int nearest_layer = Integer.MIN_VALUE;
		for(int i = 1;i <= layers[0];++i)
		{
			short h = (short) ((short) (layers[i] & 65520) >> 1);
			if(h >= z || nearest_layer_h >= h)
				continue;
			nearest_layer_h = h;
			nearest_layer = layers[i];
		}
		return nearest_layer;
	}
	
	private static short CheckNoOneLayerInRangeAndFindNearestLowerLayer(short[] layers, int z0, int z1)
	{
		int z_max;
		int z_min;
		if(z0 > z1)
		{
			z_min = z1;
			z_max = z0;
		}
		else
		{
			z_min = z0;
			z_max = z1;
		}
		short nearest_layer = -32768;
		short nearest_layer_h = -32768;
		for(int i = 1;i <= layers[0];++i)
		{
			short h = (short) ((short) (layers[i] & 65520) >> 1);
			if(z_min <= h && h <= z_max)
			{
				return -32768;
			}
			if(h >= z0 || nearest_layer_h >= h)
				continue;
			nearest_layer_h = h;
			nearest_layer = layers[i];
		}
		return nearest_layer;
	}
	
	public static boolean canSeeWallCheck(short layer, short nearest_lower_neighbor, byte directionNSWE, int curr_z, boolean air)
	{
		short nearest_lower_neighborh = (short) ((short) (nearest_lower_neighbor & 65520) >> 1);
		if(air)
		{
			return nearest_lower_neighborh < curr_z;
		}
		short layerh = (short) ((short) (layer & 65520) >> 1);
		int zdiff = nearest_lower_neighborh - layerh;
		return (layer & 15 & directionNSWE) != 0 || zdiff > -Config.MAX_Z_DIFF && zdiff != 0;
	}
	
	public static Location canSee(int _x, int _y, int _z, int _tx, int _ty, int _tz, boolean air, int geoIndex)
	{
		int diff_x = _tx - _x;
		int diff_y = _ty - _y;
		int dx = Math.abs(diff_x);
		int dy = Math.abs(diff_y);
		int curr_x = _x;
		int curr_y = _y;
		short[] curr_layers = new short[MAX_LAYERS + 1];
		NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
		Location result = new Location(_x, _y, _z, -1);
		int curr_z = _z;
		float steps = Math.max(dx, dy);
		int diff_z = _tz - _z;
		if(steps == 0.0f)
		{
			if(CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, curr_z, curr_z + diff_z) != -32768)
			{
				result.set(_tx, _ty, _tz, 1);
			}
			return result;
		}
		float step_x = (float) diff_x / steps;
		float step_y = (float) diff_y / steps;
		float step_z = (float) diff_z / steps;
		float half_step_z = step_z / 2.0f;
		float next_x = curr_x;
		float next_y = curr_y;
		float next_z = curr_z;
		short[] tmp_layers = new short[MAX_LAYERS + 1];
		int i = 0;
		while((float) i < steps)
		{
			if(curr_layers[0] == 0)
			{
				result.set(_tx, _ty, _tz, 0);
				return result;
			}
			int i_next_x = (int) ((next_x += step_x) + 0.5f);
			int i_next_y = (int) ((next_y += step_y) + 0.5f);
			int i_next_z = (int) ((next_z += step_z) + 0.5f);
			int middle_z = (int) ((float) curr_z + half_step_z);
			short src_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, curr_z, middle_z);
			if(src_nearest_lower_layer == -32768)
			{
				return result.setH(-10);
			}
			NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
			if(curr_layers[0] == 0)
			{
				result.set(_tx, _ty, _tz, 0);
				return result;
			}
			short dst_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, i_next_z, middle_z);
			if(dst_nearest_lower_layer == -32768)
			{
				return result.setH(-11);
			}
			if(curr_x == i_next_x)
			{
				if(!canSeeWallCheck(src_nearest_lower_layer, dst_nearest_lower_layer, i_next_y > curr_y ? SOUTH : NORTH, curr_z, air))
				{
					return result.setH(-20);
				}
			}
			else if(curr_y == i_next_y)
			{
				if(!canSeeWallCheck(src_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))
				{
					return result.setH(-21);
				}
			}
			else
			{
				NGetLayers(curr_x, i_next_y, tmp_layers, geoIndex);
				if(tmp_layers[0] == 0)
				{
					result.set(_tx, _ty, _tz, 0);
					return result;
				}
				short tmp_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(tmp_layers, i_next_z, middle_z);
				if(tmp_nearest_lower_layer == -32768)
				{
					return result.setH(-30);
				}
				if(!canSeeWallCheck(src_nearest_lower_layer, tmp_nearest_lower_layer, i_next_y > curr_y ? SOUTH : NORTH, curr_z, air) || !canSeeWallCheck(tmp_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))
				{
					NGetLayers(i_next_x, curr_y, tmp_layers, geoIndex);
					if(tmp_layers[0] == 0)
					{
						result.set(_tx, _ty, _tz, 0);
						return result;
					}
					tmp_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(tmp_layers, i_next_z, middle_z);
					if(tmp_nearest_lower_layer == -32768)
					{
						return result.setH(-31);
					}
					if(!canSeeWallCheck(src_nearest_lower_layer, tmp_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))
					{
						return result.setH(-32);
					}
					if(!canSeeWallCheck(tmp_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))
					{
						return result.setH(-33);
					}
				}
			}
			result.set(curr_x, curr_y, curr_z);
			curr_x = i_next_x;
			curr_y = i_next_y;
			curr_z = i_next_z;
			++i;
		}
		result.set(_tx, _ty, _tz, 255);
		return result;
	}
	
	private static Location MoveInWaterCheck(int x, int y, int z, int tx, int ty, int tz, int waterZ, int geoIndex)
	{
		int dx = tx - x;
		int dy = ty - y;
		int dz = tz - z;
		byte inc_x = sign(dx);
		byte inc_y = sign(dy);
		if((dx = Math.abs(dx)) + (dy = Math.abs(dy)) == 0)
		{
			return new Location(x, y, z).geo2world();
		}
		float inc_z_for_x = dx == 0 ? 0.0f : (float) (dz / dx);
		float inc_z_for_y = dy == 0 ? 0.0f : (float) (dz / dy);
		float next_x = x;
		float next_y = y;
		float next_z = z;
		if(dx >= dy)
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for(int i = 0;i < dx;++i)
			{
				int prev_x = x;
				int prev_y = y;
				int prev_z = z;
				x = (int) next_x;
				y = (int) next_y;
				z = (int) next_z;
				if(d > 0)
				{
					d += delta_B;
					next_x += (float) inc_x;
					next_z += inc_z_for_x;
					next_y += (float) inc_y;
					next_z += inc_z_for_y;
				}
				else
				{
					d += delta_A;
					next_x += (float) inc_x;
					next_z += inc_z_for_x;
				}
				if(next_z < (float) waterZ && NLOS_WATER(x, y, z, (int) next_x, (int) next_y, (int) next_z, geoIndex))
					continue;
				return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for(int i = 0;i < dy;++i)
			{
				int prev_x = x;
				int prev_y = y;
				int prev_z = z;
				x = (int) next_x;
				y = (int) next_y;
				z = (int) next_z;
				if(d > 0)
				{
					d += delta_B;
					next_x += (float) inc_x;
					next_z += inc_z_for_x;
					next_y += (float) inc_y;
					next_z += inc_z_for_y;
				}
				else
				{
					d += delta_A;
					next_y += (float) inc_y;
					next_z += inc_z_for_y;
				}
				if(next_z < (float) waterZ && NLOS_WATER(x, y, z, (int) next_x, (int) next_y, (int) next_z, geoIndex))
					continue;
				return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		return new Location((int) next_x, (int) next_y, (int) next_z).geo2world();
	}
	
	private static int canMove(int __x, int __y, int _z, int __tx, int __ty, int _tz, boolean withCollision, int geoIndex)
	{
		int _x = __x - World.MAP_MIN_X >> 4;
		int _y = __y - World.MAP_MIN_Y >> 4;
		int _tx = __tx - World.MAP_MIN_X >> 4;
		int _ty = __ty - World.MAP_MIN_Y >> 4;
		int diff_x = _tx - _x;
		int diff_y = _ty - _y;
		int diff_z = _tz - _z;
		int dx = Math.abs(diff_x);
		int dy = Math.abs(diff_y);
		int dz = Math.abs(diff_z);
		float steps = Math.max(dx, dy);
		if(steps == 0.0f)
		{
			return -5;
		}
		int curr_x = _x;
		int curr_y = _y;
		short[] curr_layers = new short[MAX_LAYERS + 1];
		NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
		if(curr_layers[0] == 0)
		{
			return 0;
		}
		float step_x = (float) diff_x / steps;
		float step_y = (float) diff_y / steps;
		float next_x = curr_x;
		float next_y = curr_y;
		short[] next_layers = new short[MAX_LAYERS + 1];
		short[] temp_layers = new short[MAX_LAYERS + 1];
		int i = 0;
		int curr_z = _z;
		while((float) i < steps)
		{
			int i_next_x = (int) ((next_x += step_x) + 0.5f);
			int i_next_y = (int) ((next_y += step_y) + 0.5f);
			NGetLayers(i_next_x, i_next_y, next_layers, geoIndex);
			curr_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, i_next_x, i_next_y, next_layers, temp_layers, withCollision, geoIndex);
			if(curr_z == Integer.MIN_VALUE)
			{
				return 1;
			}
			short[] curr_next_switcher = curr_layers;
			curr_layers = next_layers;
			next_layers = curr_next_switcher;
			curr_x = i_next_x;
			curr_y = i_next_y;
			++i;
		}
		diff_z = curr_z - _tz;
		dz = Math.abs(diff_z);
		if(Config.ALLOW_FALL_FROM_WALLS)
		{
			return diff_z < Config.MAX_Z_DIFF ? 0 : diff_z * 10000;
		}
		return dz > Config.MAX_Z_DIFF ? dz * 1000 : 0;
	}
	
	private static Location MoveCheck(int __x, int __y, int _z, int __tx, int __ty, boolean withCollision, boolean backwardMove, boolean returnPrev, int geoIndex)
	{
		int _x = __x - World.MAP_MIN_X >> 4;
		int _y = __y - World.MAP_MIN_Y >> 4;
		int _tx = __tx - World.MAP_MIN_X >> 4;
		int _ty = __ty - World.MAP_MIN_Y >> 4;
		int diff_x = _tx - _x;
		int diff_y = _ty - _y;
		int dx = Math.abs(diff_x);
		int dy = Math.abs(diff_y);
		float steps = Math.max(dx, dy);
		if(steps == 0.0f)
		{
			return new Location(__x, __y, _z);
		}
		float step_x = (float) diff_x / steps;
		float step_y = (float) diff_y / steps;
		int curr_x = _x;
		int curr_y = _y;
		int curr_z = _z;
		float next_x = curr_x;
		float next_y = curr_y;
		int i_next_z = curr_z;
		short[] next_layers = new short[MAX_LAYERS + 1];
		short[] temp_layers = new short[MAX_LAYERS + 1];
		short[] curr_layers = new short[MAX_LAYERS + 1];
		NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
		int prev_x = curr_x;
		int prev_y = curr_y;
		int prev_z = curr_z;
		int i = 0;
		while((float) i < steps)
		{
			int i_next_x = (int) ((next_x += step_x) + 0.5f);
			int i_next_y = (int) ((next_y += step_y) + 0.5f);
			NGetLayers(i_next_x, i_next_y, next_layers, geoIndex);
			i_next_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, i_next_x, i_next_y, next_layers, temp_layers, withCollision, geoIndex);
			if(i_next_z == Integer.MIN_VALUE || backwardMove && NcanMoveNext(i_next_x, i_next_y, i_next_z, next_layers, curr_x, curr_y, curr_layers, temp_layers, withCollision, geoIndex) == Integer.MIN_VALUE)
				break;
			short[] curr_next_switcher = curr_layers;
			curr_layers = next_layers;
			next_layers = curr_next_switcher;
			if(returnPrev)
			{
				prev_x = curr_x;
				prev_y = curr_y;
				prev_z = curr_z;
			}
			curr_x = i_next_x;
			curr_y = i_next_y;
			curr_z = i_next_z;
			++i;
		}
		if(returnPrev)
		{
			curr_x = prev_x;
			curr_y = prev_y;
			curr_z = prev_z;
		}
		return new Location(curr_x, curr_y, curr_z).geo2world();
	}
	
	public static List<Location> MoveList(int __x, int __y, int _z, int __tx, int __ty, int geoIndex, boolean onlyFullPath)
	{
		int _x = __x - World.MAP_MIN_X >> 4;
		int _y = __y - World.MAP_MIN_Y >> 4;
		int _tx = __tx - World.MAP_MIN_X >> 4;
		int _ty = __ty - World.MAP_MIN_Y >> 4;
		int diff_x = _tx - _x;
		int diff_y = _ty - _y;
		byte incx = sign(diff_x);
		byte incy = sign(diff_y);
		if(diff_x < 0)
		{
			diff_x = -diff_x;
		}
		if(diff_y < 0)
		{
			diff_y = -diff_y;
		}
		int el;
		int es;
		byte pdy;
		byte pdx;
		if(diff_x > diff_y)
		{
			pdx = incx;
			pdy = 0;
			es = diff_y;
			el = diff_x;
		}
		else
		{
			pdx = 0;
			pdy = incy;
			es = diff_x;
			el = diff_y;
		}
		int err = el / 2;
		int curr_x = _x;
		int curr_y = _y;
		int curr_z = _z;
		int next_x = curr_x;
		int next_y = curr_y;
		int next_z = curr_z;
		short[] next_layers = new short[MAX_LAYERS + 1];
		short[] temp_layers = new short[MAX_LAYERS + 1];
		short[] curr_layers = new short[MAX_LAYERS + 1];
		NGetLayers(curr_x, curr_y, curr_layers, geoIndex);
		if(curr_layers[0] == 0)
		{
			return null;
		}
		ArrayList<Location> result = new ArrayList<>(Math.min(1024, el + 1));
		result.add(new Location(curr_x, curr_y, curr_z));
		for(int i = 0;i < el;++i)
		{
			if((err -= es) < 0)
			{
				err += el;
				next_x += incx;
				next_y += incy;
			}
			else
			{
				next_x += pdx;
				next_y += pdy;
			}
			NGetLayers(next_x, next_y, next_layers, geoIndex);
			next_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, next_x, next_y, next_layers, temp_layers, false, geoIndex);
			if(next_z == Integer.MIN_VALUE)
			{
				if(!onlyFullPath)
					break;
				return null;
			}
			short[] t = curr_layers;
			curr_layers = next_layers;
			next_layers = t;
			curr_x = next_x;
			curr_y = next_y;
			curr_z = next_z;
			result.add(new Location(curr_x, curr_y, curr_z));
		}
		return result;
	}
	
	private static Location MoveCheckForAI(int x, int y, int z, int tx, int ty, int geoIndex)
	{
		int dx = tx - x;
		int dy = ty - y;
		byte inc_x = sign(dx);
		byte inc_y = sign(dy);
		if((dx = Math.abs(dx)) + (dy = Math.abs(dy)) < 2 || dx == 2 && dy == 0 || dx == 0 && dy == 2)
		{
			return new Location(x, y, z).geo2world();
		}
		int prev_x = x;
		int prev_y = y;
		int prev_z = z;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if(dx >= dy)
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for(int i = 0;i < dx;++i)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if(d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
				}
				next_z = NcanMoveNextForAI(x, y, z, next_x, next_y, geoIndex);
				if(next_z != 0)
					continue;
				return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for(int i = 0;i < dy;++i)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if(d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
				}
				next_z = NcanMoveNextForAI(x, y, z, next_x, next_y, geoIndex);
				if(next_z != 0)
					continue;
				return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		return new Location(next_x, next_y, next_z).geo2world();
	}
	
	private static boolean NcanMoveNextExCheck(int x, int y, int h, int nextx, int nexty, int hexth, short[] temp_layers, int geoIndex)
	{
		NGetLayers(x, y, temp_layers, geoIndex);
		if(temp_layers[0] == 0)
		{
			return true;
		}
		int temp_layer = FindNearestLowerLayer(temp_layers, h + Config.MIN_LAYER_HEIGHT);
		if(temp_layer == Integer.MIN_VALUE)
		{
			return false;
		}
		short temp_layer_h = (short) ((short) (temp_layer & 65520) >> 1);
		if(Math.abs(temp_layer_h - hexth) >= Config.MAX_Z_DIFF || Math.abs(temp_layer_h - h) >= Config.MAX_Z_DIFF)
		{
			return false;
		}
		return checkNSWE((byte) (temp_layer & 15), x, y, nextx, nexty);
	}
	
	public static int NcanMoveNext(int x, int y, int z, short[] layers, int next_x, int next_y, short[] next_layers, short[] temp_layers, boolean withCollision, int geoIndex)
	{
		if(layers[0] == 0 || next_layers[0] == 0)
		{
			return z;
		}
		int layer = FindNearestLowerLayer(layers, z + Config.MIN_LAYER_HEIGHT);
		if(layer == Integer.MIN_VALUE)
		{
			return Integer.MIN_VALUE;
		}
		byte layer_nswe = (byte) (layer & 15);
		if(!checkNSWE(layer_nswe, x, y, next_x, next_y))
		{
			return Integer.MIN_VALUE;
		}
		short layer_h = (short) ((short) (layer & 65520) >> 1);
		int next_layer = FindNearestLowerLayer(next_layers, layer_h + Config.MIN_LAYER_HEIGHT);
		if(next_layer == Integer.MIN_VALUE)
		{
			return Integer.MIN_VALUE;
		}
		short next_layer_h = (short) ((short) (next_layer & 65520) >> 1);
		if(x == next_x || y == next_y)
		{
			if(withCollision)
			{
				if(x == next_x)
				{
					NgetHeightAndNSWE(x - 1, y, layer_h, temp_layers, geoIndex);
					if(Math.abs(temp_layers[0] - layer_h) > 15 || !checkNSWE(layer_nswe, x - 1, y, x, y) || !checkNSWE((byte) temp_layers[1], x - 1, y, x - 1, next_y))
					{
						return Integer.MIN_VALUE;
					}
					NgetHeightAndNSWE(x + 1, y, layer_h, temp_layers, geoIndex);
					if(Math.abs(temp_layers[0] - layer_h) > 15 || !checkNSWE(layer_nswe, x + 1, y, x, y) || !checkNSWE((byte) temp_layers[1], x + 1, y, x + 1, next_y))
					{
						return Integer.MIN_VALUE;
					}
					return next_layer_h;
				}
				NgetHeightAndNSWE(x, y - 1, layer_h, temp_layers, geoIndex);
				if(Math.abs(temp_layers[0] - layer_h) >= Config.MAX_Z_DIFF || !checkNSWE(layer_nswe, x, y - 1, x, y) || !checkNSWE((byte) temp_layers[1], x, y - 1, next_x, y - 1))
				{
					return Integer.MIN_VALUE;
				}
				NgetHeightAndNSWE(x, y + 1, layer_h, temp_layers, geoIndex);
				if(Math.abs(temp_layers[0] - layer_h) >= Config.MAX_Z_DIFF || !checkNSWE(layer_nswe, x, y + 1, x, y) || !checkNSWE((byte) temp_layers[1], x, y + 1, next_x, y + 1))
				{
					return Integer.MIN_VALUE;
				}
			}
			return next_layer_h;
		}
		if(!NcanMoveNextExCheck(x, next_y, layer_h, next_x, next_y, next_layer_h, temp_layers, geoIndex))
		{
			return Integer.MIN_VALUE;
		}
		if(!NcanMoveNextExCheck(next_x, y, layer_h, next_x, next_y, next_layer_h, temp_layers, geoIndex))
		{
			return Integer.MIN_VALUE;
		}
		return next_layer_h;
	}
	
	public static int NcanMoveNextForAI(int x, int y, int z, int next_x, int next_y, int geoIndex)
	{
		short[] layers1 = new short[MAX_LAYERS + 1];
		short[] layers2 = new short[MAX_LAYERS + 1];
		NGetLayers(x, y, layers1, geoIndex);
		NGetLayers(next_x, next_y, layers2, geoIndex);
		if(layers1[0] == 0 || layers2[0] == 0)
		{
			return z == 0 ? 1 : z;
		}
		int z1 = -32768;
		byte NSWE1 = 15;
		short h;
		for(int i = 1;i <= layers1[0];++i)
		{
			h = (short) ((short) (layers1[i] & 65520) >> 1);
			if(Math.abs(z - z1) <= Math.abs(z - h))
				continue;
			z1 = h;
			NSWE1 = (byte) (layers1[i] & 15);
		}
		if(z1 == -32768)
		{
			return 0;
		}
		int z2 = -32768;
		byte NSWE2 = 15;
		for(int i = 1;i <= layers2[0];++i)
		{
			h = (short) ((short) (layers2[i] & 65520) >> 1);
			if(Math.abs(z - z2) <= Math.abs(z - h))
				continue;
			z2 = h;
			NSWE2 = (byte) (layers2[i] & 15);
		}
		if(z2 == -32768)
		{
			return 0;
		}
		if(z1 > z2 && z1 - z2 > Config.MAX_Z_DIFF)
		{
			return 0;
		}
		if(!checkNSWE(NSWE1, x, y, next_x, next_y) || !checkNSWE(NSWE2, next_x, next_y, x, y))
		{
			return 0;
		}
		return z2 == 0 ? 1 : z2;
	}
	
	public static void NGetLayers(int geoX, int geoY, short[] result, int geoIndex)
	{
		result[0] = 0;
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
		if(block == null)
			return;
		
		int index = 0;
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;
		
		int cellY;
		int cellX;
		switch(type)
		{
			case BLOCKTYPE_FLAT:
				short height = makeShort(block[index + 1], block[index]);
				height = (short) (height & 0x0fff0);
				result[0]++;
				result[1] = (short) ((short) (height << 1) | NSWE_ALL);
				return;
			case BLOCKTYPE_COMPLEX:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				index += (cellX << 3) + cellY << 1;
				height = makeShort(block[index + 1], block[index]);
				result[0]++;
				result[1] = height;
				return;
			case BLOCKTYPE_MULTILEVEL:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				int offset = (cellX << 3) + cellY;
				while(offset > 0)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
					offset--;
				}
				byte layer_count = block[index];
				index++;
				if(layer_count <= 0 || layer_count > MAX_LAYERS)
					return;
				result[0] = layer_count;
				while(layer_count > 0)
				{
					result[layer_count] = makeShort(block[index + 1], block[index]);
					layer_count--;
					index += 2;
				}
				return;
			default:
				_log.error("GeoEngine: Unknown block type");
				return;
		}
	}
	
	private static short NgetType(int geoX, int geoY, int geoIndex)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
		if(block == null)
		{
			return 0;
		}
		return block[0];
	}
	
	public static int NgetHeight(int geoX, int geoY, int z, int geoIndex)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
		if(block == null)
		{
			return z;
		}
		int index = 0;
		byte type = block[index];
		++index;
		switch(type)
		{
			case 0:
			{
				short height = makeShort(block[index + 1], block[index]);
				return (short) (height & 65520);
			}
			case 1:
			{
				int cellX = getCell(geoX);
				int cellY = getCell(geoY);
				short height = makeShort(block[index + 1], block[index += (cellX << 3) + cellY << 1]);
				return (short) ((short) (height & 65520) >> 1);
			}
			case 2:
			{
				int cellX = getCell(geoX);
				int cellY = getCell(geoY);
				for(int offset = (cellX << 3) + cellY;offset > 0;--offset)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
				}
				byte layers = block[index];
				++index;
				if(layers <= 0 || layers > MAX_LAYERS)
				{
					return (short) z;
				}
				int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;
				int z_nearest_lower = Integer.MIN_VALUE;
				int z_nearest = Integer.MIN_VALUE;
				while(layers > 0)
				{
					short height = (short) ((short) (makeShort(block[index + 1], block[index]) & 65520) >> 1);
					if(height < z_nearest_lower_limit)
					{
						z_nearest_lower = Math.max(z_nearest_lower, height);
					}
					else if(Math.abs(z - height) < Math.abs(z - z_nearest))
					{
						z_nearest = height;
					}
					layers = (byte) (layers - 1);
					index += 2;
				}
				return z_nearest_lower != Integer.MIN_VALUE ? z_nearest_lower : z_nearest;
			}
		}
		_log.error("GeoEngine: Unknown blockType");
		return z;
	}
	
	public static byte NgetNSWE(int geoX, int geoY, int z, int geoIndex)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
		if(block == null)
		{
			return 15;
		}
		int index = 0;
		byte type = block[index];
		++index;
		switch(type)
		{
			case 0:
			{
				return 15;
			}
			case 1:
			{
				int cellX = getCell(geoX);
				int cellY = getCell(geoY);
				short height = makeShort(block[index + 1], block[index += (cellX << 3) + cellY << 1]);
				return (byte) (height & 15);
			}
			case 2:
			{
				int cellX = getCell(geoX);
				int cellY = getCell(geoY);
				for(int offset = (cellX << 3) + cellY;offset > 0;--offset)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
				}
				byte layers = block[index];
				++index;
				if(layers <= 0 || layers > MAX_LAYERS)
				{
					return 15;
				}
				short tempz1 = -32768;
				int tempz2 = -32768;
				int index_nswe1 = 0;
				int index_nswe2 = 0;
				int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;
				while(layers > 0)
				{
					short height = (short) ((short) (makeShort(block[index + 1], block[index]) & 65520) >> 1);
					if(height < z_nearest_lower_limit)
					{
						if(height > tempz1)
						{
							tempz1 = height;
							index_nswe1 = index;
						}
					}
					else if(Math.abs(z - height) < Math.abs(z - tempz2))
					{
						tempz2 = height;
						index_nswe2 = index;
					}
					layers = (byte) (layers - 1);
					index += 2;
				}
				if(index_nswe1 > 0)
				{
					return (byte) (makeShort(block[index_nswe1 + 1], block[index_nswe1]) & 15);
				}
				if(index_nswe2 > 0)
				{
					return (byte) (makeShort(block[index_nswe2 + 1], block[index_nswe2]) & 15);
				}
				return 15;
			}
		}
		_log.error("GeoEngine: Unknown block type.");
		return 15;
	}
	
	public static void NgetHeightAndNSWE(int geoX, int geoY, short z, short[] result, int geoIndex)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
		
		if(block == null)
		{
			result[0] = z;
			result[1] = NSWE_ALL;
			return;
		}
		
		int index = 0;
		
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;
		
		short NSWE = NSWE_ALL;
		short height;
		int cellY;
		int cellX;
		switch(type)
		{
			case BLOCKTYPE_FLAT:
				height = makeShort(block[index + 1], block[index]);
				result[0] = (short) (height & 0x0fff0);
				result[1] = NSWE_ALL;
				return;
			case BLOCKTYPE_COMPLEX:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				index += (cellX << 3) + cellY << 1;
				height = makeShort(block[index + 1], block[index]);
				result[0] = (short) ((short) (height & 0x0fff0) >> 1); // height / 2
				result[1] = (short) (height & 0x0F);
				return;
			case BLOCKTYPE_MULTILEVEL:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				int offset = (cellX << 3) + cellY;
				while(offset > 0)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
					offset--;
				}
				byte layers = block[index];
				index++;
				if(layers <= 0 || layers > MAX_LAYERS)
				{
					result[0] = z;
					result[1] = NSWE_ALL;
					return;
				}
				
				short tempz1 = Short.MIN_VALUE;
				short tempz2 = Short.MIN_VALUE;
				int index_nswe1 = 0;
				int index_nswe2 = 0;
				int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;
				
				while(layers > 0)
				{
					height = (short) ((short) (makeShort(block[index + 1], block[index]) & 0x0fff0) >> 1); // height / 2
					
					if(height < z_nearest_lower_limit)
					{
						if(height > tempz1)
						{
							tempz1 = height;
							index_nswe1 = index;
						}
					}
					else if(Math.abs(z - height) < Math.abs(z - tempz2))
					{
						tempz2 = height;
						index_nswe2 = index;
					}
					
					layers--;
					index += 2;
				}
				
				if(index_nswe1 > 0)
				{
					NSWE = makeShort(block[index_nswe1 + 1], block[index_nswe1]);
					NSWE = (short) (NSWE & 0x0F);
				}
				else if(index_nswe2 > 0)
				{
					NSWE = makeShort(block[index_nswe2 + 1], block[index_nswe2]);
					NSWE = (short) (NSWE & 0x0F);
				}
				result[0] = tempz1 > Short.MIN_VALUE ? tempz1 : tempz2;
				result[1] = NSWE;
				return;
			default:
				_log.error("GeoEngine: Unknown block type.");
				result[0] = z;
				result[1] = NSWE_ALL;
				return;
		}
	}
	
	protected static short makeShort(byte b1, byte b0)
	{
		return (short) (b1 << 8 | b0 & 255);
	}
	
	protected static int getBlock(int geoPos)
	{
		return (geoPos >> 3) % 256;
	}
	
	protected static int getCell(int geoPos)
	{
		return geoPos % 8;
	}
	
	protected static int getBlockIndex(int blockX, int blockY)
	{
		return (blockX << 8) + blockY;
	}
	
	private static byte sign(int x)
	{
		if(x >= 0)
		{
			return 1;
		}
		return -1;
	}
	
	private static byte[] getGeoBlockFromGeoCoords(int geoX, int geoY, int geoIndex)
	{
		if(!Config.ALLOW_GEODATA)
		{
			return null;
		}
		int ix = geoX >> 11;
		int iy = geoY >> 11;
		if(ix < 0 || ix >= World.WORLD_SIZE_X || iy < 0 || iy >= World.WORLD_SIZE_Y)
		{
			return null;
		}
		byte[][][] region = geodata[ix][iy];
		int blockX = getBlock(geoX);
		int blockY = getBlock(geoY);
		int regIndex = 0;
		if((geoIndex & 251658240) == 251658240)
		{
			int x = (geoIndex & 16711680) >> 16;
			int y = (geoIndex & 65280) >> 8;
			if(ix == x && iy == y)
			{
				regIndex = geoIndex & 255;
			}
		}
		return region[regIndex][getBlockIndex(blockX, blockY)];
	}
	
	public static void load()
	{
		if(!Config.ALLOW_GEODATA)
		{
			return;
		}
		_log.info("GeoEngine: Loading Geodata...");
		File f = new File(Config.DATAPACK_ROOT, "geodata");
		if(!f.exists() || !f.isDirectory())
		{
			_log.info("GeoEngine: Files missing, loading aborted.");
			return;
		}
		Pattern p = Pattern.compile(Config.GEOFILES_PATTERN);
		ArrayList<File> geoFiles = new ArrayList<>();
		for(File q : f.listFiles())
		{
			String fn = q.getName();
			Matcher m = p.matcher(fn);
			if(q.isDirectory() || !m.matches())
				continue;
			geoFiles.add(q);
		}
		Collections.sort(geoFiles, NaturalOrderComparator.FILE_NAME_COMPARATOR);
		int counter = 0;
		for(File q : geoFiles)
		{
			String fn = q.getName();
			fn = fn.substring(0, 5);
			String[] xy = fn.split("_");
			byte rx = Byte.parseByte(xy[0]);
			byte ry = Byte.parseByte(xy[1]);
			LoadGeodataFile(q, rx, ry);
			LoadGeodata(rx, ry, 0);
			++counter;
		}
		_log.info("GeoEngine: Loaded " + counter + " map(s), max layers: " + MAX_LAYERS);
		if(Config.COMPACT_GEO)
		{
			compact();
		}
	}
	
	public static boolean LoadGeodataFile(File geoFile, byte rx, byte ry)
	{
		int ix = rx - Config.GEO_X_FIRST;
		int iy = ry - Config.GEO_Y_FIRST;
		String geoName = geoFile.getName();
		if(ix < 0 || iy < 0 || ix > (World.MAP_MAX_X >> 15) + Math.abs(World.MAP_MIN_X >> 15) || iy > (World.MAP_MAX_Y >> 15) + Math.abs(World.MAP_MIN_Y >> 15))
		{
			_log.info("GeoEngine: File " + geoName + " was not loaded!!! ");
			return false;
		}
		_log.info("GeoEngine: Loading: " + geoName);
		try
		{
			FileChannel inChannel = new RandomAccessFile(geoFile, "r").getChannel();
			ByteBuffer buf = null;
			int size = 0;
			int checkSum = 0;
			if(geoName.endsWith(".l2j"))
			{
				size = (int) inChannel.size();
				buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
				inChannel.read(buf);
			}
			else if(geoName.endsWith(".l2g"))
			{
				size = Math.max(4, (int) inChannel.size() - 4);
				buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
				buf.limit(4);
				inChannel.read(buf);
				buf.flip();
				checkSum = -2126429781 ^ buf.getInt();
				buf.clear();
				inChannel.read(buf);
				buf.rewind();
				byte key = (byte) (checkSum >> 24 & 255 ^ checkSum >> 16 & 255 ^ checkSum >> 8 & 255 ^ checkSum >> 0 & 255);
				while(buf.hasRemaining())
				{
					buf.put(buf.position(), (byte) (buf.get() ^ key));
					key = buf.get(buf.position() - 1);
					checkSum -= key;
				}
				buf.rewind();
				if(checkSum != 0)
				{
					throw new RuntimeException("Test failed!");
				}
			}
			rawgeo[ix][iy] = buf;
			if(size < 196608 || checkSum != 0)
			{
				throw new RuntimeException("Invalid geodata : " + geoName + "!");
			}
			return true;
		}
		catch(IOException e)
		{
			_log.error("", e);
			return false;
		}
	}
	
	public static void LoadGeodata(int rx, int ry, int regIndex)
	{
		int ix = rx - Config.GEO_X_FIRST;
		int iy = ry - Config.GEO_Y_FIRST;
		ByteBuffer geo = rawgeo[ix][iy];
		
		byte[][] blocks;
		
		synchronized(geodata)
		{
			blocks = geodata[ix][iy][regIndex];
			if(blocks == null)
			{
				blocks = new byte[65536][];
				geodata[ix][iy][regIndex] = blocks;
			}
		}
		byte floor = 0;
		int block = 0;
		int index = 0;
		block8:
		for(block = 0;block < 65536;++block)
		{
			byte type = geo.get(index);
			++index;
			byte[] geoBlock;
			switch(type)
			{
				case 0:
				{
					geoBlock = new byte[] {type, geo.get(index), geo.get(index + 1)};
					index += 2;
					blocks[block] = geoBlock;
					continue block8;
				}
				case 1:
				{
					geoBlock = new byte[129];
					geoBlock[0] = type;
					geo.position(index);
					geo.get(geoBlock, 1, 128);
					index += 128;
					blocks[block] = geoBlock;
					continue block8;
				}
				case 2:
				{
					int orgIndex = index;
					for(int b = 0;b < 64;++b)
					{
						byte layers = geo.get(index);
						MAX_LAYERS = Math.max(MAX_LAYERS, layers);
						index += (layers << 1) + 1;
						if(layers <= floor)
							continue;
						floor = layers;
					}
					int diff = index - orgIndex;
					geoBlock = new byte[diff + 1];
					geoBlock[0] = type;
					geo.position(orgIndex);
					geo.get(geoBlock, 1, diff);
					blocks[block] = geoBlock;
					continue block8;
				}
				default:
				{
					throw new RuntimeException("Invalid geodata: " + rx + "_" + ry + "!");
				}
			}
		}
	}
	
	public static int NextGeoIndex(int rx, int ry, int refId)
	{
		if(!Config.ALLOW_GEODATA)
		{
			return 0;
		}
		int ix = rx - Config.GEO_X_FIRST;
		int iy = ry - Config.GEO_Y_FIRST;
		int regIndex = -1;
		byte[][][][][] arrby = geodata;
		synchronized(arrby)
		{
			byte[][][] region = geodata[ix][iy];
			for(int i = 0;i < region.length;++i)
			{
				if(region[i] != null)
					continue;
				regIndex = i;
				break;
			}
			if(regIndex == -1)
			{
				regIndex = region.length;
				byte[][][] resizedRegion = new byte[regIndex + 1][][];
				for(int i = 0;i < region.length;++i)
				{
					resizedRegion[i] = region[i];
				}
				geodata[ix][iy] = resizedRegion;
			}
			LoadGeodata(rx, ry, regIndex);
		}
		return 251658240 | ix << 16 | iy << 8 | regIndex;
	}
	
	public static void FreeGeoIndex(int geoIndex)
	{
		if(!Config.ALLOW_GEODATA)
		{
			return;
		}
		if((geoIndex & 251658240) != 251658240)
		{
			return;
		}
		byte[][][][][] arrby = geodata;
		synchronized(arrby)
		{
			int regIndex = geoIndex & 255;
			int iy = (geoIndex & 65280) >> 8;
			int ix = (geoIndex & 16711680) >> 16;
			geodata[ix][iy][regIndex] = null;
		}
	}
	
	public static void removeGeoCollision(GeoCollision collision, int geoIndex)
	{
		Shape shape = collision.getShape();
		byte[][] around = collision.getGeoAround();
		if(around == null)
		{
			throw new RuntimeException("Attempt to remove unitialized collision: " + collision);
		}
		int minX = shape.getXmin() - World.MAP_MIN_X - 16 >> 4;
		int minY = shape.getYmin() - World.MAP_MIN_Y - 16 >> 4;
		int minZ = shape.getZmin();
		int maxZ = shape.getZmax();
		
		for(int gX = 0;gX < around.length;gX++)
			for(int gY = 0;gY < around[gX].length;gY++)
			{
				int geoX = minX + gX;
				int geoY = minY + gY;
				
				byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
				if(block == null)
					continue;
				
				int cellX = getCell(geoX);
				int cellY = getCell(geoY);
				
				int index = 0;
				byte blockType = block[index];
				index++;
				
				byte old_nswe;
				short height;
				switch(blockType)
				{
					case BLOCKTYPE_COMPLEX:
						index += (cellX << 3) + cellY << 1;
						
						// Gets the height of the cell
						height = makeShort(block[index + 1], block[index]);
						old_nswe = (byte) (height & 0x0F);
						height &= 0xfff0;
						height >>= 1;
						
						// Appropriate layer can not be found
						if(height < minZ || height > maxZ)
							break;
						
						// around
						height <<= 1;
						height &= 0xfff0;
						height |= old_nswe;
						if(collision.isConcrete())
							height |= around[gX][gY];
						else
							height &= ~around[gX][gY];
						
						// Record the height of the array
						block[index + 1] = (byte) (height >> 8);
						block[index] = (byte) (height & 0x00ff);
						break;
					case BLOCKTYPE_MULTILEVEL:
						// The last valid index for the door
						
						// What follows is a standard mechanism for height
						int offset = (cellX << 3) + cellY;
						while(offset > 0)
						{
							byte lc = block[index];
							index += (lc << 1) + 1;
							offset--;
						}
						byte layers = block[index];
						index++;
						if(layers <= 0 || layers > MAX_LAYERS)
							break;
						old_nswe = NSWE_ALL;
						short temph = Short.MIN_VALUE;
						int neededIndex = -1;
						while(layers > 0)
						{
							height = makeShort(block[index + 1], block[index]);
							byte tmp_nswe = (byte) (height & 0x0F);
							height &= 0xfff0;
							height >>= 1;
							int z_diff_last = Math.abs(minZ - temph);
							int z_diff_curr = Math.abs(maxZ - height);
							if(z_diff_last > z_diff_curr)
							{
								old_nswe = tmp_nswe;
								temph = height;
								neededIndex = index;
							}
							layers--;
							index += 2;
						}
						
						// appropriate layer can not be found					
						if(temph == Short.MIN_VALUE || temph < minZ || temph > maxZ)
							break;
						
						// around
						temph <<= 1;
						temph &= 0xfff0;
						temph |= old_nswe;
						if(collision.isConcrete())
							temph |= around[gX][gY];
						else
							temph &= ~around[gX][gY];
						
						// record high
						block[neededIndex + 1] = (byte) (temph >> 8);
						block[neededIndex] = (byte) (temph & 0x00ff);
						break;
				}
			}
	}
	
	public static void applyGeoCollision(GeoCollision collision, int geoIndex)
	{
		Shape shape = collision.getShape();
		if(shape.getXmax() == shape.getYmax() && shape.getXmax() == 0)
			throw new RuntimeException("Attempt to add incorrect collision: " + collision);
		
		boolean isFirstTime = false;
		
		// Size of conflict in the cells geodata
		int minX = shape.getXmin() - World.MAP_MIN_X - 16 >> 4;
		int maxX = shape.getXmax() - World.MAP_MIN_X + 16 >> 4;
		int minY = shape.getYmin() - World.MAP_MIN_Y - 16 >> 4;
		int maxY = shape.getYmax() - World.MAP_MIN_Y + 16 >> 4;
		int minZ = shape.getZmin();
		int maxZ = shape.getZmax();
		
		byte[][] around = collision.getGeoAround();
		if(around == null)
		{
			isFirstTime = true;
			
			// form the collision
			byte[][] cells = new byte[maxX - minX + 1][maxY - minY + 1];
			for(int gX = minX;gX <= maxX;gX++)
				for(int gY = minY;gY <= maxY;gY++)
				{
					int x = (gX << 4) + World.MAP_MIN_X;
					int y = (gY << 4) + World.MAP_MIN_Y;
					
					loop:
					for(int ax = x;ax < x + 16;ax++)
						for(int ay = y;ay < y + 16;ay++)
							if(shape.isInside(ax, ay))
							{
								cells[gX - minX][gY - minY] = 1;
								break loop;
							}
				}
			
			around = new byte[maxX - minX + 1][maxY - minY + 1];
			for(int gX = 0;gX < cells.length;gX++)
				for(int gY = 0;gY < cells[gX].length;gY++)
				{
					if(cells[gX][gY] == 1)
					{
						around[gX][gY] = NSWE_ALL;
						
						byte _nswe;
						if(gY > 0)
							if(cells[gX][gY - 1] == 0)
							{
								_nswe = around[gX][gY - 1];
								_nswe |= SOUTH;
								around[gX][gY - 1] = _nswe;
							}
						if(gY + 1 < cells[gX].length)
							if(cells[gX][gY + 1] == 0)
							{
								_nswe = around[gX][gY + 1];
								_nswe |= NORTH;
								around[gX][gY + 1] = _nswe;
							}
						if(gX > 0)
							if(cells[gX - 1][gY] == 0)
							{
								_nswe = around[gX - 1][gY];
								_nswe |= EAST;
								around[gX - 1][gY] = _nswe;
							}
						if(gX + 1 < cells.length)
							if(cells[gX + 1][gY] == 0)
							{
								_nswe = around[gX + 1][gY];
								_nswe |= WEST;
								around[gX + 1][gY] = _nswe;
							}
					}
				}
			
			collision.setGeoAround(around);
		}
		
		for(int gX = 0;gX < around.length;gX++)
			for(int gY = 0;gY < around[gX].length;gY++)
			{
				int geoX = minX + gX;
				int geoY = minY + gY;
				
				// Trying to copy a block of geodata, if already exists, it is not copied
				//TODO: if(first_time)
				//	copyBlock(ix, iy, blockIndex);
				
				byte[] block = getGeoBlockFromGeoCoords(geoX, geoY, geoIndex);
				if(block == null)
					continue;
				
				int cellX = getCell(geoX);
				int cellY = getCell(geoY);
				
				int index = 0;
				byte blockType = block[index];
				index++;
				
				byte close_nswe;
				byte old_nswe;
				short height;
				switch(blockType)
				{
					case BLOCKTYPE_COMPLEX:
						index += (cellX << 3) + cellY << 1;
						
						// Gets the height of the cell
						height = makeShort(block[index + 1], block[index]);
						old_nswe = (byte) (height & 0x0F);
						height &= 0xfff0;
						height >>= 1;
						
						// Appropriate layer can not be found
						if(height < minZ || height > maxZ)
							break;
						
						close_nswe = around[gX][gY];
						
						if(isFirstTime)
						{
							if(collision.isConcrete())
								close_nswe &= old_nswe;
							else
								close_nswe &= ~old_nswe;
							around[gX][gY] = close_nswe;
						}
						
						// around
						height <<= 1;
						height &= 0xfff0;
						height |= old_nswe;
						
						if(collision.isConcrete())
							height &= ~close_nswe;
						else
							height |= close_nswe;
						
						// Record the height of the array
						block[index + 1] = (byte) (height >> 8);
						block[index] = (byte) (height & 0x00ff);
						break;
					case BLOCKTYPE_MULTILEVEL:
						// The last valid index for the door
						
						// What follows is a standard mechanism for height
						int offset = (cellX << 3) + cellY;
						while(offset > 0)
						{
							byte lc = block[index];
							index += (lc << 1) + 1;
							offset--;
						}
						byte layers = block[index];
						index++;
						if(layers <= 0 || layers > MAX_LAYERS)
							break;
						old_nswe = NSWE_ALL;
						short temph = Short.MIN_VALUE;
						int neededIndex = -1;
						while(layers > 0)
						{
							height = makeShort(block[index + 1], block[index]);
							byte tmp_nswe = (byte) (height & 0x0F);
							height &= 0xfff0;
							height >>= 1;
							int z_diff_last = Math.abs(minZ - temph);
							int z_diff_curr = Math.abs(maxZ - height);
							if(z_diff_last > z_diff_curr)
							{
								old_nswe = tmp_nswe;
								temph = height;
								neededIndex = index;
							}
							layers--;
							index += 2;
						}
						
						// Appropriate layer can not be found
						if(temph == Short.MIN_VALUE || temph < minZ || temph > maxZ)
							break;
						
						close_nswe = around[gX][gY];
						
						if(isFirstTime)
						{
							if(collision.isConcrete())
								close_nswe &= old_nswe;
							else
								close_nswe &= ~old_nswe;
							around[gX][gY] = close_nswe;
						}
						
						// around
						temph <<= 1;
						temph &= 0xfff0;
						temph |= old_nswe;
						if(collision.isConcrete())
							temph &= ~close_nswe;
						else
							temph |= close_nswe;
						
						// record high
						block[neededIndex + 1] = (byte) (temph >> 8);
						block[neededIndex] = (byte) (temph & 0x00ff);
						break;
				}
			}
	}
	
	public static void compact()
	{
		long total = 0;
		long optimized = 0;
		for(int mapX = 0;mapX < World.WORLD_SIZE_X;++mapX)
		{
			for(int mapY = 0;mapY < World.WORLD_SIZE_Y;++mapY)
			{
				if(geodata[mapX][mapY] == null)
					continue;
				total += 65536;
				GeoOptimizer.BlockLink[] links = GeoOptimizer.loadBlockMatches("geodata/matches/" + (mapX + Config.GEO_X_FIRST) + "_" + (mapY + Config.GEO_Y_FIRST) + ".matches");
				if(links == null)
					continue;
				for(int i = 0;i < links.length;++i)
				{
					byte[][][] link_region = geodata[links[i].linkMapX][links[i].linkMapY];
					if(link_region == null)
						continue;
					link_region[links[i].linkBlockIndex][0] = geodata[mapX][mapY][links[i].blockIndex][0];
					++optimized;
				}
			}
		}
		_log.info(String.format("GeoEngine: - Compacted %d of %d blocks...", optimized, total));
	}
	
	public static boolean equalsData(byte[] a1, byte[] a2)
	{
		if(a1.length != a2.length)
		{
			return false;
		}
		for(int i = 0;i < a1.length;++i)
		{
			if(a1[i] == a2[i])
				continue;
			return false;
		}
		return true;
	}
	
	public static boolean compareGeoBlocks(int mapX1, int mapY1, int blockIndex1, int mapX2, int mapY2, int blockIndex2)
	{
		return equalsData(geodata[mapX1][mapY1][blockIndex1][0], geodata[mapX2][mapY2][blockIndex2][0]);
	}
	
	private static void initChecksums()
	{
		_log.info("GeoEngine: - Generating Checksums...");
		new File(Config.DATAPACK_ROOT, "geodata/checksum").mkdirs();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		GeoOptimizer.checkSums = new int[World.WORLD_SIZE_X][World.WORLD_SIZE_Y][];
		for(int mapX = 0;mapX < World.WORLD_SIZE_X;++mapX)
		{
			for(int mapY = 0;mapY < World.WORLD_SIZE_Y;++mapY)
			{
				if(geodata[mapX][mapY] == null)
					continue;
				executor.execute(new GeoOptimizer.CheckSumLoader(mapX, mapY, geodata[mapX][mapY]));
			}
		}
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}
		catch(InterruptedException e)
		{
			_log.error("", e);
		}
	}
	
	private static void initBlockMatches(int maxScanRegions)
	{
		_log.info("GeoEngine: Generating Block Matches...");
		new File(Config.DATAPACK_ROOT, "geodata/matches").mkdirs();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for(int mapX = 0;mapX < World.WORLD_SIZE_X;++mapX)
		{
			for(int mapY = 0;mapY < World.WORLD_SIZE_Y;++mapY)
			{
				if(geodata[mapX][mapY] == null || GeoOptimizer.checkSums == null || GeoOptimizer.checkSums[mapX][mapY] == null)
					continue;
				executor.execute(new GeoOptimizer.GeoBlocksMatchFinder(mapX, mapY, maxScanRegions));
			}
		}
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}
		catch(InterruptedException e)
		{
			_log.error("", e);
		}
	}
	
	public static void deleteChecksumFiles()
	{
		for(int mapX = 0;mapX < World.WORLD_SIZE_X;++mapX)
		{
			for(int mapY = 0;mapY < World.WORLD_SIZE_Y;++mapY)
			{
				if(geodata[mapX][mapY] == null)
					continue;
				new File(Config.DATAPACK_ROOT, "geodata/checksum/" + (mapX + Config.GEO_X_FIRST) + "_" + (mapY + Config.GEO_Y_FIRST) + ".crc").delete();
			}
		}
	}
	
	public static void genBlockMatches(int maxScanRegions)
	{
		initChecksums();
		initBlockMatches(maxScanRegions);
	}
	
	public static void unload()
	{
		for(int mapX = 0;mapX < World.WORLD_SIZE_X;++mapX)
		{
			for(int mapY = 0;mapY < World.WORLD_SIZE_Y;++mapY)
			{
				geodata[mapX][mapY] = null;
			}
		}
	}
}