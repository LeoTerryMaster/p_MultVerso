package l2.gameserver.instancemanager;

import l2.commons.dbutils.DbUtils;
import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Manor;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.items.ClanWarehouse;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.templates.manor.CropProcure;
import l2.gameserver.templates.manor.SeedProduction;
import l2.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class CastleManorManager
{
	public static final int PERIOD_CURRENT = 0;
	public static final int PERIOD_NEXT = 1;
	protected static final String var_name = "ManorApproved";
	protected static final long MAINTENANCE_PERIOD;
	private static final Logger _log = LoggerFactory.getLogger(CastleManorManager.class);
	private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
	private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
	private static final int NEXT_PERIOD_APPROVE;
	private static final int NEXT_PERIOD_APPROVE_MIN;
	private static final int MANOR_REFRESH;
	private static final int MANOR_REFRESH_MIN;
	private static CastleManorManager _instance;
	
	static
	{
		NEXT_PERIOD_APPROVE = Config.MANOR_APPROVE_TIME;
		NEXT_PERIOD_APPROVE_MIN = Config.MANOR_APPROVE_MIN;
		MANOR_REFRESH = Config.MANOR_REFRESH_TIME;
		MANOR_REFRESH_MIN = Config.MANOR_REFRESH_MIN;
		MAINTENANCE_PERIOD = Config.MANOR_MAINTENANCE_PERIOD / 60000;
	}
	
	private boolean _underMaintenance;
	private boolean _disabled;
	
	private CastleManorManager()
	{
		load();
		init();
		_underMaintenance = false;
		_disabled = !Config.ALLOW_MANOR;
		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
		{
			c.setNextPeriodApproved(ServerVariables.getBool("ManorApproved"));
		}
	}
	
	public static CastleManorManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Manor System: Initializing...");
			_instance = new CastleManorManager();
		}
		return _instance;
	}
	
	private void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
			for(Castle castle : castleList)
			{
				statement = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?");
				statement.setInt(1, castle.getId());
				rs = statement.executeQuery();
				ArrayList<SeedProduction> productionNext = new ArrayList<>();
				ArrayList<SeedProduction> production = new ArrayList<>();
				while(rs.next())
				{
					int seedId = rs.getInt("seed_id");
					long canProduce = rs.getLong("can_produce");
					long startProduce = rs.getLong("start_produce");
					long price = rs.getLong("seed_price");
					int period = rs.getInt("period");
					if(period == 0)
					{
						production.add(new SeedProduction(seedId, canProduce, price, startProduce));
						continue;
					}
					productionNext.add(new SeedProduction(seedId, canProduce, price, startProduce));
				}
				DbUtils.close(statement, rs);
				castle.setSeedProduction(production, 0);
				castle.setSeedProduction(productionNext, 1);
				statement = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?");
				statement.setInt(1, castle.getId());
				rs = statement.executeQuery();
				ArrayList<CropProcure> procureNext = new ArrayList<>();
				ArrayList<CropProcure> procure = new ArrayList<>();
				while(rs.next())
				{
					int cropId = rs.getInt("crop_id");
					long canBuy = rs.getLong("can_buy");
					long startBuy = rs.getLong("start_buy");
					int rewardType = rs.getInt("reward_type");
					long price = rs.getLong("price");
					int period = rs.getInt("period");
					if(period == 0)
					{
						procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
						continue;
					}
					procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
				}
				castle.setCropProcure(procure, 0);
				castle.setCropProcure(procureNext, 1);
				if(!(procure.isEmpty() && procureNext.isEmpty() && production.isEmpty() && productionNext.isEmpty()))
				{
					_log.info("Manor System: Loaded data for " + castle.getName() + " castle");
				}
				DbUtils.close(statement, rs);
			}
		}
		catch(Exception e)
		{
			_log.error("Manor System: Error restoring manor data!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}
	
	protected void init()
	{
		if(ServerVariables.getString("ManorApproved", "").isEmpty())
		{
			Calendar manorRefresh = Calendar.getInstance();
			manorRefresh.set(11, MANOR_REFRESH);
			manorRefresh.set(12, MANOR_REFRESH_MIN);
			manorRefresh.set(13, 0);
			manorRefresh.set(14, 0);
			Calendar periodApprove = Calendar.getInstance();
			periodApprove.set(11, NEXT_PERIOD_APPROVE);
			periodApprove.set(12, NEXT_PERIOD_APPROVE_MIN);
			periodApprove.set(13, 0);
			periodApprove.set(14, 0);
			boolean isApproved = periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
			ServerVariables.set("ManorApproved", isApproved);
		}
		Calendar FirstDelay = Calendar.getInstance();
		FirstDelay.set(13, 0);
		FirstDelay.set(14, 0);
		FirstDelay.add(12, 1);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new ManorTask(), FirstDelay.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), 60000);
	}
	
	public void setNextPeriod()
	{
		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
		{
			Clan clan;
			if(c.getOwnerId() <= 0 || (clan = ClanTable.getInstance().getClan(c.getOwnerId())) == null)
				continue;
			ClanWarehouse cwh = clan.getWarehouse();
			for(CropProcure crop : c.getCropProcure(0))
			{
				if(crop.getStartAmount() == 0)
					continue;
				if(crop.getStartAmount() > crop.getAmount())
				{
					_log.info("Manor System [" + c.getName() + "]: Start Amount of Crop " + crop.getStartAmount() + " > Amount of current " + crop.getAmount());
					long count = crop.getStartAmount() - crop.getAmount();
					count = count * 90 / 100;
					if(count < 1 && Rnd.get(99) < 90)
					{
						count = 1;
					}
					if(count >= 1)
					{
						int id = Manor.getInstance().getMatureCrop(crop.getId());
						cwh.addItem(id, count);
					}
				}
				if(crop.getAmount() > 0)
				{
					c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice(), false, false);
					Log.add(c.getName() + "|" + crop.getAmount() * crop.getPrice() + "|ManorManager|" + crop.getAmount() + "*" + crop.getPrice(), "treasury");
				}
				c.setCollectedShops(0);
				c.setCollectedSeed(0);
			}
			c.setSeedProduction(c.getSeedProduction(1), 0);
			c.setCropProcure(c.getCropProcure(1), 0);
			long manor_cost = c.getManorCost(0);
			if(c.getTreasury() < manor_cost)
			{
				c.setSeedProduction(getNewSeedsList(c.getId()), 1);
				c.setCropProcure(getNewCropsList(c.getId()), 1);
				Log.add(c.getName() + "|" + manor_cost + "|ManorManager Error@setNextPeriod", "treasury");
			}
			else
			{
				ArrayList<SeedProduction> production = new ArrayList<>();
				for(SeedProduction s : c.getSeedProduction(0))
				{
					s.setCanProduce(s.getStartProduce());
					production.add(s);
				}
				ArrayList<CropProcure> procure = new ArrayList<>();
				for(CropProcure cr : c.getCropProcure(0))
				{
					cr.setAmount(cr.getStartAmount());
					procure.add(cr);
				}
				c.setSeedProduction(production, 1);
				c.setCropProcure(procure, 1);
			}
			c.saveCropData();
			c.saveSeedData();
			PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
			c.setNextPeriodApproved(false);
		}
	}
	
	public void approveNextPeriod()
	{
		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
		{
			if(c.getOwnerId() <= 0)
				continue;
			long manor_cost = c.getManorCost(1);
			if(c.getTreasury() < manor_cost)
			{
				c.setSeedProduction(getNewSeedsList(c.getId()), 1);
				c.setCropProcure(getNewCropsList(c.getId()), 1);
				manor_cost = c.getManorCost(1);
				if(manor_cost > 0)
				{
					Log.add(c.getName() + "|" + -manor_cost + "|ManorManager Error@approveNextPeriod", "treasury");
				}
				Clan clan = c.getOwner();
				PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
			}
			else
			{
				c.addToTreasuryNoTax(-manor_cost, false, false);
				Log.add(c.getName() + "|" + -manor_cost + "|ManorManager", "treasury");
			}
			c.setNextPeriodApproved(true);
		}
	}
	
	private List<SeedProduction> getNewSeedsList(int castleId)
	{
		ArrayList<SeedProduction> seeds = new ArrayList<>();
		List<Integer> seedsIds = Manor.getInstance().getSeedsForCastle(castleId);
		Iterator<Integer> iterator = seedsIds.iterator();
		while(iterator.hasNext())
		{
			int sd = iterator.next();
			seeds.add(new SeedProduction(sd));
		}
		return seeds;
	}
	
	private List<CropProcure> getNewCropsList(int castleId)
	{
		ArrayList<CropProcure> crops = new ArrayList<>();
		List<Integer> cropsIds = Manor.getInstance().getCropsForCastle(castleId);
		Iterator<Integer> iterator = cropsIds.iterator();
		while(iterator.hasNext())
		{
			int cr = iterator.next();
			crops.add(new CropProcure(cr));
		}
		return crops;
	}
	
	public boolean isUnderMaintenance()
	{
		return _underMaintenance;
	}
	
	public void setUnderMaintenance(boolean mode)
	{
		_underMaintenance = mode;
	}
	
	public boolean isDisabled()
	{
		return _disabled;
	}
	
	public void setDisabled(boolean mode)
	{
		_disabled = mode;
	}
	
	public SeedProduction getNewSeedProduction(int id, long amount, long price, long sales)
	{
		return new SeedProduction(id, amount, price, sales);
	}
	
	public CropProcure getNewCropProcure(int id, long amount, int type, long price, long buy)
	{
		return new CropProcure(id, amount, type, buy, price);
	}
	
	public void save()
	{
		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
		{
			c.saveSeedData();
			c.saveCropData();
		}
	}
	
	private class ManorTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			int H = Calendar.getInstance().get(11);
			int M = Calendar.getInstance().get(12);
			if(ServerVariables.getBool("ManorApproved"))
			{
				if(H < NEXT_PERIOD_APPROVE || H > MANOR_REFRESH || H == MANOR_REFRESH && M >= MANOR_REFRESH_MIN)
				{
					ServerVariables.set("ManorApproved", false);
					setUnderMaintenance(true);
					_log.info("Manor System: Under maintenance mode started");
				}
			}
			else if(isUnderMaintenance())
			{
				if(H != MANOR_REFRESH || (long) M >= (long) MANOR_REFRESH_MIN + MAINTENANCE_PERIOD)
				{
					setUnderMaintenance(false);
					_log.info("Manor System: Next period started");
					if(isDisabled())
					{
						return;
					}
					setNextPeriod();
					try
					{
						save();
					}
					catch(Exception e)
					{
						_log.info("Manor System: Failed to save manor data: " + e);
					}
				}
			}
			else if(H > NEXT_PERIOD_APPROVE && H < MANOR_REFRESH || H == NEXT_PERIOD_APPROVE && M >= NEXT_PERIOD_APPROVE_MIN)
			{
				ServerVariables.set("ManorApproved", true);
				_log.info("Manor System: Next period approved");
				if(isDisabled())
				{
					return;
				}
				approveNextPeriod();
			}
		}
	}
}