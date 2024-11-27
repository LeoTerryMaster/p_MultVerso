package l2.gameserver.taskmanager;

import l2.commons.dbutils.DbUtils;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.items.PcInventory;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DelayedItemsManager extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(DelayedItemsManager.class);
	private static final Object _lock;
	private static DelayedItemsManager _instance;
	
	static
	{
		_lock = new Object();
	}
	
	private int last_payment_id;
	
	public DelayedItemsManager()
	{
		last_payment_id = 0;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			last_payment_id = get_last_payment_id(con);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
		ThreadPoolManager.getInstance().schedule(this, 10000);
	}
	
	public static DelayedItemsManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new DelayedItemsManager();
		}
		return _instance;
	}
	
	private int get_last_payment_id(Connection con)
	{
		int result = last_payment_id;
		ResultSet rset = null;
		PreparedStatement st = null;
		try
		{
			st = con.prepareStatement("SELECT MAX(payment_id) AS last FROM items_delayed");
			rset = st.executeQuery();
			if(rset.next())
			{
				result = rset.getInt("last");
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(st, rset);
		}
		return result;
	}
	
	@Override
	public void runImpl() throws Exception
	{
		block9:
		{
			Connection con = null;
			PreparedStatement st = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				int last_payment_id_temp = get_last_payment_id(con);
				if(last_payment_id_temp == last_payment_id)
					break block9;
				Object object = _lock;
				synchronized(object)
				{
					st = con.prepareStatement("SELECT DISTINCT owner_id FROM items_delayed WHERE payment_status=0 AND payment_id > ?");
					st.setInt(1, last_payment_id);
					rset = st.executeQuery();
					while(rset.next())
					{
						Player player = GameObjectsStorage.getPlayer(rset.getInt("owner_id"));
						if(player == null)
							continue;
						loadDelayed(player, true);
					}
					last_payment_id = last_payment_id_temp;
				}
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
			finally
			{
				DbUtils.closeQuietly(con, st, rset);
			}
		}
		ThreadPoolManager.getInstance().schedule(this, 10000);
	}
	
	public int loadDelayed(Player player, boolean notify)
	{
		if(player == null)
		{
			return 0;
		}
		int player_id = player.getObjectId();
		PcInventory inv = player.getInventory();
		if(inv == null)
		{
			return 0;
		}
		int restored_counter = 0;
		Object object = _lock;
		synchronized(object)
		{
			ResultSet rset = null;
			PreparedStatement st_delete = null;
			PreparedStatement st = null;
			Connection con = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				st = con.prepareStatement("SELECT * FROM items_delayed WHERE owner_id=? AND payment_status=0");
				st.setInt(1, player_id);
				rset = st.executeQuery();
				st_delete = con.prepareStatement("UPDATE items_delayed SET payment_status=1 WHERE payment_id=?");
				while(rset.next())
				{
					int ITEM_ID = rset.getInt("item_id");
					long ITEM_COUNT = rset.getLong("count");
					int ITEM_ENCHANT = rset.getInt("enchant_level");
					int PAYMENT_ID = rset.getInt("payment_id");
					int FLAGS = rset.getInt("flags");
					boolean stackable = ItemHolder.getInstance().getTemplate(ITEM_ID).isStackable();
					boolean success = false;
					int i = 0;
					while((long) i < (stackable ? 1 : ITEM_COUNT))
					{
						ItemInstance item = ItemFunctions.createItem(ITEM_ID);
						if(item.isStackable())
						{
							item.setCount(ITEM_COUNT);
						}
						else
						{
							item.setEnchantLevel(ITEM_ENCHANT);
						}
						item.setLocation(ItemInstance.ItemLocation.INVENTORY);
						item.setCustomFlags(FLAGS);
						if(ITEM_COUNT > 0 && inv.addItem(item) == null)
						{
							_log.warn("Unable to delayed create item " + ITEM_ID + " request " + PAYMENT_ID);
						}
						else
						{
							success = true;
							++restored_counter;
							if(notify && ITEM_COUNT > 0)
							{
								player.sendPacket(SystemMessage2.obtainItems(ITEM_ID, stackable ? ITEM_COUNT : 1, ITEM_ENCHANT));
							}
						}
						++i;
					}
					if(!success)
						continue;
					Log.add("<add owner_id=" + player_id + " item_id=" + ITEM_ID + " count=" + ITEM_COUNT + " enchant_level=" + ITEM_ENCHANT + " payment_id=" + PAYMENT_ID + "/>", "delayed_add");
					st_delete.setInt(1, PAYMENT_ID);
					st_delete.execute();
				}
			}
			catch(Exception e)
			{
				_log.error("Could not load delayed items for player " + player + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(st_delete);
				DbUtils.closeQuietly(con, st, rset);
			}
		}
		return restored_counter;
	}
	
	public void addDelayed(int ownerObjId, int itemTypeId, int amount, int enchant, String desc)
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			pstmt = con.prepareStatement("INSERT INTO \t`items_delayed`\t(\t`payment_id`, \t\t`owner_id`, \t\t`item_id`, \t\t`count`, \t\t`enchant_level`, \t\t`flags`, \t\t`payment_status`, \t\t`description`\t) SELECT \tMAX(`payment_id`) + 1, \t?, ?, ?, ?, 0, 0, ? \tFROM `items_delayed`");
			pstmt.setInt(1, ownerObjId);
			pstmt.setInt(2, itemTypeId);
			pstmt.setInt(3, amount);
			pstmt.setInt(4, enchant);
			pstmt.setString(5, desc);
			pstmt.executeUpdate();
		}
		catch(SQLException e)
		{
			_log.error("Could not add delayed items " + itemTypeId + " " + amount + "(+" + enchant + ") for objId " + ownerObjId + " desc \"" + desc + "\" !", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, pstmt);
		}
	}
}