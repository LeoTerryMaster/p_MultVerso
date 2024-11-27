package l2.gameserver.dao;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.ItemStateFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

public class ItemsDAO
{
	private static final Logger _log = LoggerFactory.getLogger(ItemsDAO.class);
	private static final ItemsDAO _instance = new ItemsDAO();
	private static final String SQLP_GET_ITEM = "{CALL `lip_GetItem`(?)}";
	private static final String SQLP_LOAD_ITEMS_BY_OWNER = "{CALL `lip_LoadItemsByOwner`(?)}";
	private static final String SQLP_LOAD_ITEMS_BY_OWNER_AND_LOC = "{CALL `lip_LoadItemsByOwnerAndLoc`(?, ?)}";
	private static final String SQLP_DELETE_ITEM = "{CALL `lip_DeleteItem`(?)}";
	private static final String SQLP_STORE_ITEM = "{CALL `lip_StoreItem`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
	
	public static final ItemsDAO getInstance()
	{
		return _instance;
	}
	
	public void store(ItemInstance item)
	{
		if(!item.getItemStateFlag().get(ItemStateFlags.STATE_CHANGED))
		{
			return;
		}
		Connection conn = null;
		CallableStatement cstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cstmt = conn.prepareCall("{CALL `lip_StoreItem`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
			store0(item, cstmt);
			cstmt.execute();
			item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
		}
		catch(SQLException e)
		{
			_log.error("Exception while store item", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cstmt);
		}
	}
	
	public void store(Collection<ItemInstance> items)
	{
		Connection conn = null;
		CallableStatement cstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cstmt = conn.prepareCall("{CALL `lip_StoreItem`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
			for(ItemInstance item : items)
			{
				if(!item.getItemStateFlag().get(ItemStateFlags.STATE_CHANGED))
					continue;
				store0(item, cstmt);
				cstmt.execute();
				item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
			}
		}
		catch(SQLException e)
		{
			_log.error("Exception while store items", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cstmt);
		}
	}
	
	private void store0(ItemInstance item, CallableStatement cStmt) throws SQLException
	{
		cStmt.setInt(1, item.getObjectId());
		cStmt.setInt(2, item.getOwnerId());
		cStmt.setInt(3, item.getItemId());
		cStmt.setLong(4, item.getCount());
		cStmt.setInt(5, item.getLocData());
		cStmt.setString(6, item.getLocName());
		cStmt.setInt(7, item.getEnchantLevel());
		cStmt.setInt(8, item.getDuration());
		cStmt.setInt(9, item.getPeriodBegin());
		if(item.isWeapon())
		{
			cStmt.setByte(10, item.getAttackElement().getId());
			cStmt.setInt(11, item.getAttackElementValue());
			cStmt.setInt(12, 0);
			cStmt.setInt(13, 0);
			cStmt.setInt(14, 0);
			cStmt.setInt(15, 0);
			cStmt.setInt(16, 0);
			cStmt.setInt(17, 0);
		}
		else
		{
			cStmt.setByte(10, Element.NONE.getId());
			cStmt.setInt(11, 0);
			cStmt.setInt(12, item.getAttributes().getFire());
			cStmt.setInt(13, item.getAttributes().getWater());
			cStmt.setInt(14, item.getAttributes().getWind());
			cStmt.setInt(15, item.getAttributes().getEarth());
			cStmt.setInt(16, item.getAttributes().getHoly());
			cStmt.setInt(17, item.getAttributes().getUnholy());
		}
		cStmt.setInt(18, item.getVariationStat1());
		cStmt.setInt(19, item.getVariationStat2());
		cStmt.setInt(20, item.getBlessed());
		cStmt.setInt(21, item.getDamaged());
		cStmt.setInt(22, item.getAgathionEnergy());
		cStmt.setInt(23, item.getCustomFlags());
		if(item.getVisibleItemId() != item.getItemId())
		{
			cStmt.setInt(24, item.getVisibleItemId());
		}
		else
		{
			cStmt.setInt(24, 0);
		}
	}
	
	public void delete(Collection<ItemInstance> items)
	{
		Connection conn = null;
		CallableStatement cStmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cStmt = conn.prepareCall("{CALL `lip_DeleteItem`(?)}");
			for(ItemInstance item : items)
			{
				cStmt.setInt(1, item.getObjectId());
				cStmt.execute();
				item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
			}
		}
		catch(SQLException e)
		{
			_log.error("Exception while deleting items", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cStmt);
		}
	}
	
	public void delete(int itemObjectId)
	{
		Connection conn = null;
		CallableStatement cStmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cStmt = conn.prepareCall("{CALL `lip_DeleteItem`(?)}");
			cStmt.setInt(1, itemObjectId);
			cStmt.execute();
		}
		catch(SQLException e)
		{
			_log.error("Exception while deleting item", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cStmt);
		}
	}
	
	public void delete(ItemInstance item)
	{
		Connection conn = null;
		CallableStatement cStmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cStmt = conn.prepareCall("{CALL `lip_DeleteItem`(?)}");
			cStmt.setInt(1, item.getObjectId());
			cStmt.execute();
			item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
		}
		catch(SQLException e)
		{
			_log.error("Exception while deleting item", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cStmt);
		}
	}
	
	private ItemInstance load0(ResultSet rset) throws SQLException
	{
		int item_obj_id = rset.getInt("item_id");
		ItemInstance item = new ItemInstance(item_obj_id);
		item.setOwnerId(rset.getInt("owner_id"));
		item.setItemId(rset.getInt("item_type"));
		item.setCount(rset.getLong("amount"));
		item.setLocName(rset.getString("location"));
		item.setLocData(rset.getInt("slot"));
		item.setEnchantLevel(rset.getInt("enchant"));
		item.setDuration(rset.getInt("duration"));
		item.setPeriodBegin(rset.getInt("period"));
		byte atkAttrElem = rset.getByte("attack_attr_type");
		int atkAttrVal = rset.getInt("attack_attr_val");
		int defAttrFire = rset.getInt("defence_attr_fire");
		int defAttrWater = rset.getInt("defence_attr_water");
		int defAttrWind = rset.getInt("defence_attr_wind");
		int defAttrEarth = rset.getInt("defence_attr_earth");
		int defAttrHoly = rset.getInt("defence_attr_holy");
		int defAttrUnholy = rset.getInt("defence_attr_unholy");
		if(atkAttrElem != Element.NONE.getId())
		{
			item.setAttributeElement(Element.VALUES[atkAttrElem], atkAttrVal);
		}
		else
		{
			item.setAttributeElement(Element.FIRE, defAttrFire);
			item.setAttributeElement(Element.WATER, defAttrWater);
			item.setAttributeElement(Element.WIND, defAttrWind);
			item.setAttributeElement(Element.EARTH, defAttrEarth);
			item.setAttributeElement(Element.HOLY, defAttrHoly);
			item.setAttributeElement(Element.UNHOLY, defAttrUnholy);
		}
		item.setVariationStat1(rset.getInt("variation_stat1"));
		item.setVariationStat2(rset.getInt("variation_stat2"));
		item.setBlessed(rset.getInt("blessed"));
		item.setDamaged(rset.getInt("damaged"));
		item.setAgathionEnergy(rset.getInt("item_energy"));
		item.setCustomFlags(rset.getInt("custom_flags"));
		item.setVisibleItemId(rset.getInt("item_vis_type"));
		item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
		return item;
	}
	
	public Collection<ItemInstance> loadItemsByOwnerIdAndLoc(int ownerId, ItemInstance.ItemLocation baseLocation)
	{
		LinkedList<ItemInstance> result = new LinkedList<>();
		Connection conn = null;
		CallableStatement cStmt = null;
		ResultSet rSet = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cStmt = conn.prepareCall("{CALL `lip_LoadItemsByOwnerAndLoc`(?, ?)}");
			cStmt.setInt(1, ownerId);
			cStmt.setString(2, baseLocation.name());
			rSet = cStmt.executeQuery();
			while(rSet.next())
			{
				result.add(load0(rSet));
			}
		}
		catch(SQLException e)
		{
			_log.error("Exception while load items", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cStmt, rSet);
		}
		return result;
	}
	
	public Collection<Integer> loadItemObjectIdsByOwner(int ownerId)
	{
		LinkedList<Integer> result = new LinkedList<>();
		ResultSet rSet = null;
		CallableStatement cStmt = null;
		Connection conn = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cStmt = conn.prepareCall("{CALL `lip_LoadItemsByOwner`(?)}");
			cStmt.setInt(1, ownerId);
			rSet = cStmt.executeQuery();
			while(rSet.next())
			{
				result.add(rSet.getInt("item_id"));
			}
		}
		catch(SQLException e)
		{
			_log.error("Exception while load items", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cStmt, rSet);
		}
		return result;
	}
	
	public ItemInstance load(int item_obj_id)
	{
		ItemInstance result = null;
		Connection conn = null;
		CallableStatement cStmt = null;
		ResultSet rSet = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			cStmt = conn.prepareCall("{CALL `lip_GetItem`(?)}");
			cStmt.setInt(1, item_obj_id);
			rSet = cStmt.executeQuery();
			if(rSet.next())
			{
				result = load0(rSet);
			}
		}
		catch(SQLException e)
		{
			_log.error("Exception while load items", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, cStmt, rSet);
		}
		return result;
	}
}