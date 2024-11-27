package l2.gameserver.model.entity.residence;

import l2.commons.dao.JdbcEntityState;
import l2.commons.dbutils.DbUtils;
import l2.commons.math.SafeMath;
import l2.gameserver.dao.CastleDAO;
import l2.gameserver.dao.CastleHiredGuardDAO;
import l2.gameserver.dao.ClanDataDAO;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Manor;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.items.ClanWarehouse;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.components.NpcString;
import l2.gameserver.templates.StatsSet;
import l2.gameserver.templates.item.support.MerchantGuard;
import l2.gameserver.templates.manor.CropProcure;
import l2.gameserver.templates.manor.SeedProduction;
import l2.gameserver.utils.GameStats;
import l2.gameserver.utils.Log;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Castle extends Residence
{
	private static final Logger _log = LoggerFactory.getLogger(Castle.class);
	private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
	private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
	private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
	private final IntObjectMap<MerchantGuard> _merchantGuards = new HashIntObjectMap();
	private final NpcString _npcStringName;
	private final Set<ItemInstance> _spawnMerchantTickets = new CopyOnWriteArraySet<>();
	private List<CropProcure> _procure;
	private List<SeedProduction> _production;
	private List<CropProcure> _procureNext;
	private List<SeedProduction> _productionNext;
	private boolean _isNextPeriodApproved;
	private int _TaxPercent;
	private double _TaxRate;
	private long _treasury;
	private long _collectedShops;
	private long _collectedSeed;
	
	public Castle(StatsSet set)
	{
		super(set);
		_npcStringName = NpcString.valueOf(1001000 + _id);
	}
	
	@Override
	public ResidenceType getType()
	{
		return ResidenceType.Castle;
	}
	
	@Override
	public void changeOwner(Clan newOwner)
	{
		Castle oldCastle;
		if(newOwner != null && newOwner.getCastle() != 0 && (oldCastle = ResidenceHolder.getInstance().getResidence(Castle.class, newOwner.getCastle())) != null)
		{
			oldCastle.changeOwner(null);
		}
		if(getOwnerId() > 0 && (newOwner == null || newOwner.getClanId() != getOwnerId()))
		{
			removeSkills();
			setTaxPercent(null, 0);
			cancelCycleTask();
			Clan oldOwner = getOwner();
			if(oldOwner != null)
			{
				ClanWarehouse warehouse;
				long amount = getTreasury();
				if(amount > 0 && (warehouse = oldOwner.getWarehouse()) != null)
				{
					warehouse.addItem(57, amount);
					addToTreasuryNoTax(-amount, false, false);
					Log.add(getName() + "|" + -amount + "|Castle:changeOwner", "treasury");
				}
				for(Player clanMember : oldOwner.getOnlineMembers(0))
				{
					if(clanMember == null || clanMember.getInventory() == null)
						continue;
					clanMember.getInventory().validateItems();
				}
				oldOwner.setHasCastle(0);
			}
		}
		if(newOwner != null)
		{
			newOwner.setHasCastle(getId());
		}
		updateOwnerInDB(newOwner);
		rewardSkills();
		update();
	}
	
	@Override
	protected void loadData()
	{
		_TaxPercent = 0;
		_TaxRate = 0.0;
		_treasury = 0;
		_procure = new ArrayList<>();
		_production = new ArrayList<>();
		_procureNext = new ArrayList<>();
		_productionNext = new ArrayList<>();
		_isNextPeriodApproved = false;
		_owner = ClanDataDAO.getInstance().getOwner(this);
		CastleDAO.getInstance().select(this);
		CastleHiredGuardDAO.getInstance().load(this);
	}
	
	private void updateOwnerInDB(Clan clan)
	{
		_owner = clan;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=? LIMIT 1");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			if(clan != null)
			{
				statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=? LIMIT 1");
				statement.setInt(1, getId());
				statement.setInt(2, getOwnerId());
				statement.execute();
				clan.broadcastClanStatus(true, false, false);
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public int getTaxPercent()
	{
		if(_TaxPercent > 5 && SevenSigns.getInstance().getSealOwner(3) == 1)
		{
			_TaxPercent = 5;
		}
		return _TaxPercent;
	}
	
	public void setTaxPercent(int p)
	{
		_TaxPercent = Math.min(Math.max(0, p), 100);
		_TaxRate = (double) _TaxPercent / 100.0;
	}
	
	public int getTaxPercent0()
	{
		return _TaxPercent;
	}
	
	public long getCollectedShops()
	{
		return _collectedShops;
	}
	
	public void setCollectedShops(long value)
	{
		_collectedShops = value;
	}
	
	public long getCollectedSeed()
	{
		return _collectedSeed;
	}
	
	public void setCollectedSeed(long value)
	{
		_collectedSeed = value;
	}
	
	public void addToTreasury(long amount, boolean shop, boolean seed)
	{
		if(getOwnerId() <= 0)
		{
			return;
		}
		if(amount == 0)
		{
			return;
		}
		Castle royal;
		if(amount > 1 && _id != 5 && _id != 8 && (royal = ResidenceHolder.getInstance().getResidence(Castle.class, _id >= 7 ? 8 : 5)) != null)
		{
			long royalTax = (long) ((double) amount * royal.getTaxRate());
			if(royal.getOwnerId() > 0)
			{
				royal.addToTreasury(royalTax, shop, seed);
				if(_id == 5)
				{
					Log.add("Aden|" + royalTax + "|Castle:adenTax", "treasury");
				}
				else if(_id == 8)
				{
					Log.add("Rune|" + royalTax + "|Castle:runeTax", "treasury");
				}
			}
			amount -= royalTax;
		}
		addToTreasuryNoTax(amount, shop, seed);
	}
	
	public void addToTreasuryNoTax(long amount, boolean shop, boolean seed)
	{
		if(getOwnerId() <= 0)
		{
			return;
		}
		if(amount == 0)
		{
			return;
		}
		GameStats.addAdena(amount);
		_treasury = SafeMath.addAndLimit(_treasury, amount);
		if(shop)
		{
			_collectedShops += amount;
		}
		if(seed)
		{
			_collectedSeed += amount;
		}
		setJdbcState(JdbcEntityState.UPDATED);
		update();
	}
	
	public int getCropRewardType(int crop)
	{
		int rw = 0;
		for(CropProcure cp : _procure)
		{
			if(cp.getId() != crop)
				continue;
			rw = cp.getReward();
		}
		return rw;
	}
	
	public void setTaxPercent(Player activeChar, int taxPercent)
	{
		setTaxPercent(taxPercent);
		setJdbcState(JdbcEntityState.UPDATED);
		update();
		if(activeChar != null)
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.model.entity.Castle.OutOfControl.CastleTaxChangetTo", activeChar).addString(getName()).addNumber(taxPercent));
		}
	}
	
	public double getTaxRate()
	{
		if(_TaxRate > 0.05 && SevenSigns.getInstance().getSealOwner(3) == 1)
		{
			_TaxRate = 0.05;
		}
		return _TaxRate;
	}
	
	public long getTreasury()
	{
		return _treasury;
	}
	
	public void setTreasury(long t)
	{
		_treasury = t;
	}
	
	public List<SeedProduction> getSeedProduction(int period)
	{
		return period == 0 ? _production : _productionNext;
	}
	
	public List<CropProcure> getCropProcure(int period)
	{
		return period == 0 ? _procure : _procureNext;
	}
	
	public void setSeedProduction(List<SeedProduction> seed, int period)
	{
		if(period == 0)
		{
			_production = seed;
		}
		else
		{
			_productionNext = seed;
		}
	}
	
	public void setCropProcure(List<CropProcure> crop, int period)
	{
		if(period == 0)
		{
			_procure = crop;
		}
		else
		{
			_procureNext = crop;
		}
	}
	
	public synchronized SeedProduction getSeed(int seedId, int period)
	{
		for(SeedProduction seed : getSeedProduction(period))
		{
			if(seed.getId() != seedId)
				continue;
			return seed;
		}
		return null;
	}
	
	public synchronized CropProcure getCrop(int cropId, int period)
	{
		for(CropProcure crop : getCropProcure(period))
		{
			if(crop.getId() != cropId)
				continue;
			return crop;
		}
		return null;
	}
	
	public long getManorCost(int period)
	{
		List<CropProcure> procure;
		List<SeedProduction> production;
		if(period == 0)
		{
			procure = _procure;
			production = _production;
		}
		else
		{
			procure = _procureNext;
			production = _productionNext;
		}
		long total = 0;
		if(production != null)
		{
			for(SeedProduction seed : production)
			{
				total += Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
			}
		}
		if(procure != null)
		{
			for(CropProcure crop : procure)
			{
				total += crop.getPrice() * crop.getStartAmount();
			}
		}
		return total;
	}
	
	public void saveSeedData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			String[] values;
			int count;
			String query;
			if(_production != null)
			{
				count = 0;
				query = "INSERT INTO castle_manor_production VALUES ";
				values = new String[_production.size()];
				for(SeedProduction s : _production)
				{
					values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 0 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query = query + values[0];
					for(int i = 1;i < values.length;++i)
					{
						query = query + "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
			if(_productionNext != null)
			{
				count = 0;
				query = "INSERT INTO castle_manor_production VALUES ";
				values = new String[_productionNext.size()];
				for(SeedProduction s : _productionNext)
				{
					values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 1 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query = query + values[0];
					for(int i = 1;i < values.length;++i)
					{
						query = query + "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			_log.error("Error adding seed production data for castle " + getName() + "!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void saveSeedData(int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
			statement.setInt(1, getId());
			statement.setInt(2, period);
			statement.execute();
			DbUtils.close(statement);
			List<SeedProduction> prod = getSeedProduction(period);
			if(prod != null)
			{
				int count = 0;
				String[] values = new String[prod.size()];
				for(SeedProduction s : prod)
				{
					values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
					++count;
				}
				if(values.length > 0)
				{
					String query = "INSERT INTO castle_manor_production VALUES ";
					query = query + values[0];
					for(int i = 1;i < values.length;++i)
					{
						query = query + "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			_log.error("Error adding seed production data for castle " + getName() + "!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void saveCropData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			String[] values;
			int count;
			String query;
			if(_procure != null)
			{
				count = 0;
				query = "INSERT INTO castle_manor_procure VALUES ";
				values = new String[_procure.size()];
				for(CropProcure cp : _procure)
				{
					values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 0 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query = query + values[0];
					for(int i = 1;i < values.length;++i)
					{
						query = query + "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
			if(_procureNext != null)
			{
				count = 0;
				query = "INSERT INTO castle_manor_procure VALUES ";
				values = new String[_procureNext.size()];
				for(CropProcure cp : _procureNext)
				{
					values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 1 + ")";
					++count;
				}
				if(values.length > 0)
				{
					query = query + values[0];
					for(int i = 1;i < values.length;++i)
					{
						query = query + "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			_log.error("Error adding crop data for castle " + getName() + "!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void saveCropData(int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
			statement.setInt(1, getId());
			statement.setInt(2, period);
			statement.execute();
			DbUtils.close(statement);
			List<CropProcure> proc = getCropProcure(period);
			if(proc != null)
			{
				int count = 0;
				String[] values = new String[proc.size()];
				for(CropProcure cp : proc)
				{
					values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
					++count;
				}
				if(values.length > 0)
				{
					String query = "INSERT INTO castle_manor_procure VALUES ";
					query = query + values[0];
					for(int i = 1;i < values.length;++i)
					{
						query = query + "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					DbUtils.close(statement);
				}
			}
		}
		catch(Exception e)
		{
			_log.error("Error adding crop data for castle " + getName() + "!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void updateCrop(int cropId, long amount, int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
			statement.setLong(1, amount);
			statement.setInt(2, cropId);
			statement.setInt(3, getId());
			statement.setInt(4, period);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Error adding crop data for castle " + getName() + "!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void updateSeed(int seedId, long amount, int period)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
			statement.setLong(1, amount);
			statement.setInt(2, seedId);
			statement.setInt(3, getId());
			statement.setInt(4, period);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Error adding seed production data for castle " + getName() + "!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public boolean isNextPeriodApproved()
	{
		return _isNextPeriodApproved;
	}
	
	public void setNextPeriodApproved(boolean val)
	{
		_isNextPeriodApproved = val;
	}
	
	@Override
	public void update()
	{
		CastleDAO.getInstance().update(this);
	}
	
	public NpcString getNpcStringName()
	{
		return _npcStringName;
	}
	
	public void addMerchantGuard(MerchantGuard merchantGuard)
	{
		_merchantGuards.put(merchantGuard.getItemId(), merchantGuard);
	}
	
	public MerchantGuard getMerchantGuard(int itemId)
	{
		return _merchantGuards.get(itemId);
	}
	
	public IntObjectMap<MerchantGuard> getMerchantGuards()
	{
		return _merchantGuards;
	}
	
	public Set<ItemInstance> getSpawnMerchantTickets()
	{
		return _spawnMerchantTickets;
	}
	
	@Override
	public void startCycleTask()
	{
	}
}