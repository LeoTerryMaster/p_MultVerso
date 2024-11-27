package l2.gameserver.taskmanager;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.commons.threading.SteppingRunnableQueueManager;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.dao.AccountBonusDAO;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;

import java.util.concurrent.Future;

public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
	private static final LazyPrecisionTaskManager _instance = new LazyPrecisionTaskManager();
	
	private LazyPrecisionTaskManager()
	{
		super(1000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000, 1000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				purge();
			}
		}, 60000, 60000);
	}
	
	public static final LazyPrecisionTaskManager getInstance()
	{
		return _instance;
	}
	
	public Future<?> addPCCafePointsTask(Player player)
	{
		long delay = (long) Config.ALT_PCBANG_POINTS_DELAY * 60000;
		return scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				if(player.isInOfflineMode() || player.getLevel() < Config.ALT_PCBANG_POINTS_MIN_LVL)
				{
					return;
				}
				player.addPcBangPoints(Config.ALT_PCBANG_POINTS_BONUS, Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0.0 && Rnd.chance(Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE));
			}
		}, delay, delay);
	}
	
	public Future<?> startBonusExpirationTask(Player player)
	{
		HardReference<Player> playerRef = player.getRef();
		long delay = player.getBonus().getBonusExpire() * 1000 - System.currentTimeMillis();
		return schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				Player player = playerRef.get();
				if(player == null)
				{
					return;
				}
				player.getBonus().reset();
				if(player.getParty() != null)
				{
					player.getParty().recalculatePartyData();
				}
				String msg = new CustomMessage("scripts.services.RateBonus.LuckEnded", player, new Object[0]).toString();
				player.sendPacket(new ExShowScreenMessage(msg, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				player.sendMessage(msg);
				AccountBonusDAO.getInstance().delete(player.getAccountName());
			}
		}, delay);
	}
	
	public Future<?> addNpcAnimationTask(NpcInstance npc)
	{
		return scheduleAtFixedRate(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				if(npc.isVisible() && !npc.isActionsDisabled() && !npc.isMoving() && !npc.isInCombat())
				{
					npc.onRandomAnimation();
				}
			}
		}, 1000, (long) Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000);
	}
}