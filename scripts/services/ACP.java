package services;

import l2.commons.lang.reference.HardReference;
import l2.commons.listener.Listener;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.listener.PlayerListener;
import l2.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2.gameserver.listener.actor.OnCurrentMpReduceListener;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.listener.actor.player.OnPlayerExitListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ExUseSharedGroupItem;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.skills.TimeStamp;
import l2.gameserver.stats.Stats;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class ACP implements OnPlayerEnterListener, OnPlayerExitListener, ScriptFile, IVoicedCommandHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(ACP.class);
	private static final long ACP_ACT_DELAY = 100;
	private final String[] _commandList = {"acp"};
	
	private static void showACPInteface(Player player)
	{
		NpcHtmlMessage dialog = new NpcHtmlMessage(5);
		dialog.setFile("command/acp.htm");
		if(ACPType.HPACP.isEnabled(player))
		{
			dialog.replace("%hpap%", String.valueOf(ACPType.HPACP.getActPercent(player)) + "%");
		}
		else
		{
			dialog.replace("%hpap%", "Off");
		}
		if(ACPType.CPACP.isEnabled(player))
		{
			dialog.replace("%cpap%", String.valueOf(ACPType.CPACP.getActPercent(player)) + "%");
		}
		else
		{
			dialog.replace("%cpap%", "Off");
		}
		if(ACPType.MPACP.isEnabled(player))
		{
			dialog.replace("%mpap%", String.valueOf(ACPType.MPACP.getActPercent(player)) + "%");
		}
		else
		{
			dialog.replace("%mpap%", "Off");
		}
		player.sendPacket(dialog);
	}
	
	private static boolean isACPEnabled()
	{
		return Config.SERVICES_HPACP_ENABLE || Config.SERVICES_CPACP_ENABLE || Config.SERVICES_MPACP_ENABLE;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(isACPEnabled() && command.equalsIgnoreCase("acp"))
		{
			String[] param = args.split("\\s+");
			if(param.length > 1)
			{
				String type = param[0];
				String val = param[1];
				for(ACPType acpType : ACPType.VALUES)
				{
					if(!acpType.isEnabled())
					{
						acpType.remove(player);
						continue;
					}
					if(!acpType.getCfgName().equalsIgnoreCase(type))
						continue;
					if(val.equalsIgnoreCase("true") || val.equalsIgnoreCase("on") || val.equalsIgnoreCase("enable") || val.equalsIgnoreCase("1") || val.equalsIgnoreCase("yes"))
					{
						acpType.apply(player);
						acpType.setEnabled(player, true);
						player.sendMessage(new CustomMessage("services.ACP.Enabled", player, acpType.getCfgName()));
						continue;
					}
					if(val.equalsIgnoreCase("false") || val.equalsIgnoreCase("of") || val.equalsIgnoreCase("off") || val.equalsIgnoreCase("disable") || val.equalsIgnoreCase("0") || val.equalsIgnoreCase("no"))
					{
						acpType.remove(player);
						acpType.setEnabled(player, false);
						player.sendMessage(new CustomMessage("services.ACP.Disabled", player, acpType.getCfgName()));
						continue;
					}
					if(!acpType.isEnabled(player))
						continue;
					int actPercent = acpType.setActPercent(player, NumberUtils.toInt(val, acpType.getActDefPercent()));
					acpType.apply(player);
					player.sendMessage(new CustomMessage("services.ACP.ActPercentSet", player, acpType.getCfgName(), actPercent));
				}
			}
			showACPInteface(player);
			return true;
		}
		return false;
	}
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if(isACPEnabled())
		{
			for(ACPType acpType : ACPType.VALUES)
			{
				if(!acpType.isEnabled() || !acpType.isEnabled(player))
					continue;
				acpType.apply(player);
			}
		}
	}
	
	@Override
	public void onPlayerExit(Player player)
	{
		if(isACPEnabled())
		{
			for(ACPType acpType : ACPType.VALUES)
			{
				acpType.remove(player);
			}
		}
	}
	
	@Override
	public void onLoad()
	{
		if(isACPEnabled())
		{
			CharListenerList.addGlobal(this);
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
			LOG.info("ACPService: Loaded");
		}
	}
	
	@Override
	public void onReload()
	{
		onShutdown();
		onLoad();
	}
	
	@Override
	public void onShutdown()
	{
		CharListenerList.removeGlobal(this);
	}
	
	enum ACPType
	{
		HPACP("HP")
				{
					@Override
					public boolean isEnabled()
					{
						return Config.SERVICES_HPACP_ENABLE;
					}
					
					@Override
					public void apply(Player player)
					{
						for(Listener listener : player.getListeners().getListeners())
						{
							if(!(listener instanceof HPACPHelper))
								continue;
							return;
						}
						player.addListener(new HPACPHelper(player));
					}
					
					@Override
					public void remove(Player player)
					{
						for(Listener listener : player.getListeners().getListeners())
						{
							if(!(listener instanceof HPACPHelper))
								continue;
							player.getListeners().remove(listener);
						}
					}
					
					@Override
					public int[] getPotionsItemIds()
					{
						return Config.SERVICES_HPACP_POTIONS_ITEM_IDS;
					}
					
					@Override
					protected int getActMinPercent()
					{
						return Config.SERVICES_HPACP_MIN_PERCENT;
					}
					
					@Override
					protected int getActMaxPercent()
					{
						return Config.SERVICES_HPACP_MAX_PERCENT;
					}
					
					@Override
					protected int getActDefPercent()
					{
						return Config.SERVICES_HPACP_DEF_PERCENT;
					}
				},
		CPACP("CP")
				{
					@Override
					public boolean isEnabled()
					{
						return Config.SERVICES_CPACP_ENABLE;
					}
					
					@Override
					public void apply(Player player)
					{
						for(Listener listener : player.getListeners().getListeners())
						{
							if(!(listener instanceof CPACPHelper))
								continue;
							return;
						}
						player.addListener(new CPACPHelper(player));
					}
					
					@Override
					public void remove(Player player)
					{
						for(Listener listener : player.getListeners().getListeners())
						{
							if(!(listener instanceof CPACPHelper))
								continue;
							player.getListeners().remove(listener);
						}
					}
					
					@Override
					public int[] getPotionsItemIds()
					{
						return Config.SERVICES_CPACP_POTIONS_ITEM_IDS;
					}
					
					@Override
					protected int getActMinPercent()
					{
						return Config.SERVICES_CPACP_MIN_PERCENT;
					}
					
					@Override
					protected int getActMaxPercent()
					{
						return Config.SERVICES_CPACP_MAX_PERCENT;
					}
					
					@Override
					protected int getActDefPercent()
					{
						return Config.SERVICES_CPACP_DEF_PERCENT;
					}
				},
		MPACP("MP")
				{
					@Override
					public boolean isEnabled()
					{
						return Config.SERVICES_MPACP_ENABLE;
					}
					
					@Override
					public void apply(Player player)
					{
						for(Listener listener : player.getListeners().getListeners())
						{
							if(!(listener instanceof MPACPHelper))
								continue;
							return;
						}
						player.addListener(new MPACPHelper(player));
					}
					
					@Override
					public void remove(Player player)
					{
						for(Listener listener : player.getListeners().getListeners())
						{
							if(!(listener instanceof MPACPHelper))
								continue;
							player.getListeners().remove(listener);
						}
					}
					
					@Override
					public int[] getPotionsItemIds()
					{
						return Config.SERVICES_MPACP_POTIONS_ITEM_IDS;
					}
					
					@Override
					protected int getActMinPercent()
					{
						return Config.SERVICES_MPACP_MIN_PERCENT;
					}
					
					@Override
					protected int getActMaxPercent()
					{
						return Config.SERVICES_MPACP_MAX_PERCENT;
					}
					
					@Override
					protected int getActDefPercent()
					{
						return Config.SERVICES_MPACP_DEF_PERCENT;
					}
				};
		
		public static final ACPType[] VALUES;
		private static final String ENABLE_VAR_NAME_SUFIX;
		private static final String ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX;
		
		static
		{
			ENABLE_VAR_NAME_SUFIX = "Enabled";
			ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX = "ActPercent";
			VALUES = ACPType.values();
		}
		
		private final String _cfgName;
		
		ACPType(String cfgName)
		{
			_cfgName = cfgName;
		}
		
		public String getCfgName()
		{
			return _cfgName;
		}
		
		public abstract boolean isEnabled();
		
		public boolean isEnabled(Player player)
		{
			return isEnabled() && player.getVarB(name() + ENABLE_VAR_NAME_SUFIX, false);
		}
		
		public int getActPercent(Player player)
		{
			int percent = player.getVarInt(name() + ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX, getActDefPercent());
			return Math.min(Math.max(getActMinPercent(), percent), getActMaxPercent());
		}
		
		public int setActPercent(Player player, int percent)
		{
			percent = Math.min(Math.max(getActMinPercent(), percent), getActMaxPercent());
			player.setVar(name() + ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX, percent, -1);
			return percent;
		}
		
		public void setEnabled(Player player, boolean enabled)
		{
			if(enabled)
			{
				player.setVar(name() + ENABLE_VAR_NAME_SUFIX, "true", -1);
				return;
			}
			player.unsetVar(name() + ENABLE_VAR_NAME_SUFIX);
			player.unsetVar(name() + ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX);
		}
		
		public abstract void apply(Player player);
		
		public abstract void remove(Player player);
		
		public abstract int[] getPotionsItemIds();
		
		protected abstract int getActMinPercent();
		
		protected abstract int getActMaxPercent();
		
		protected abstract int getActDefPercent();
	}
	
	private static final class MPACPHelper extends ACPHelper implements OnCurrentMpReduceListener
	{
		protected MPACPHelper(Player player)
		{
			super(player, ACPType.MPACP);
		}
		
		@Override
		protected boolean canUse(Player player)
		{
			if(super.canUse(player))
			{
				double useLim = player.calcStat(Stats.MP_LIMIT, null, null) * (double) player.getMaxMp() / 100.0 * ((double) getType().getActPercent(player) / 100.0);
				return player.getCurrentMp() < useLim;
			}
			return false;
		}
		
		@Override
		public void onCurrentMpReduce(Creature actor, double reduce, Creature attacker)
		{
			Player player = actor.getPlayer();
			if(player == null)
			{
				actor.removeListener(this);
				return;
			}
			if(!getType().isEnabled(player))
			{
				actor.removeListener(this);
			}
			act(player);
		}
	}
	
	private static final class CPACPHelper extends ACPHelper implements OnCurrentHpDamageListener
	{
		protected CPACPHelper(Player player)
		{
			super(player, ACPType.CPACP);
		}
		
		@Override
		protected boolean canUse(Player player)
		{
			if(super.canUse(player))
			{
				double useLim = player.calcStat(Stats.CP_LIMIT, null, null) * (double) player.getMaxCp() / 100.0 * ((double) getType().getActPercent(player) / 100.0);
				return player.getCurrentCp() < useLim;
			}
			return false;
		}
		
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			Player player = actor.getPlayer();
			if(player == null)
			{
				actor.removeListener(this);
				return;
			}
			if(!getType().isEnabled(player))
			{
				actor.removeListener(this);
			}
			act(player);
		}
	}
	
	private static final class HPACPHelper extends ACPHelper implements OnCurrentHpDamageListener
	{
		protected HPACPHelper(Player player)
		{
			super(player, ACPType.HPACP);
		}
		
		@Override
		protected boolean canUse(Player player)
		{
			if(super.canUse(player))
			{
				double useLim = player.calcStat(Stats.HP_LIMIT, null, null) * (double) player.getMaxHp() / 100.0 * ((double) getType().getActPercent(player) / 100.0);
				return player.getCurrentHp() < useLim;
			}
			return false;
		}
		
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			Player player = actor.getPlayer();
			if(player == null)
			{
				actor.removeListener(this);
				return;
			}
			if(!getType().isEnabled(player))
			{
				actor.removeListener(this);
			}
			act(player);
		}
	}
	
	private abstract static class ACPHelper implements Runnable, PlayerListener
	{
		private final HardReference<Player> _pleyerRef;
		private final AtomicReference<ACPHelperState> _state;
		private final ACPType _type;
		
		protected ACPHelper(Player player, ACPType type)
		{
			_type = type;
			_pleyerRef = player.getRef();
			_state = new AtomicReference<>(ACPHelperState.IDLE);
		}
		
		protected Player getPlayer()
		{
			return _pleyerRef.get();
		}
		
		public ACPType getType()
		{
			return _type;
		}
		
		protected void act(Player player)
		{
			if(getPlayer() != player)
			{
				if(player != null)
				{
					player.removeListener(this);
				}
				if(getPlayer() != null)
				{
					getPlayer().removeListener(this);
				}
				return;
			}
			if(_state.compareAndSet(ACPHelperState.IDLE, ACPHelperState.APPLY))
			{
				schedule(100);
			}
		}
		
		@Override
		public void run()
		{
			Player player = getPlayer();
			if(player == null)
			{
				return;
			}
			try
			{
				if(_state.compareAndSet(ACPHelperState.APPLY, ACPHelperState.USE))
				{
					use(player);
				}
			}
			catch(Exception e)
			{
				LOG.error("Exception in ACP helper task", e);
				_state.set(ACPHelperState.IDLE);
			}
		}
		
		private void schedule(long delay)
		{
			ThreadPoolManager.getInstance().schedule(this, delay);
		}
		
		protected boolean canUse(Player player)
		{
			if(player == null)
			{
				return false;
			}
			if(player.isDead() || player.isOutOfControl() || player.isInStoreMode())
			{
				return false;
			}
			if(player.isFishing() || player.isHealBlocked() || player.isOlyParticipant())
			{
				return false;
			}
			return Config.SERVICES_HPACP_WORK_IN_PEACE_ZONE || !player.isInZonePeace();
		}
		
		private void use(Player player)
		{
			if(!canUse(player))
			{
				_state.compareAndSet(ACPHelperState.USE, ACPHelperState.IDLE);
				return;
			}
			ItemInstance potionItem = findUsableItem(player);
			if(potionItem == null)
			{
				_state.compareAndSet(ACPHelperState.USE, ACPHelperState.IDLE);
				return;
			}
			long remaining = getReuseRemaining(potionItem, player);
			if(remaining <= 0)
			{
				useItem(player, potionItem);
				if(canUse(player) && _state.compareAndSet(ACPHelperState.USE, ACPHelperState.APPLY))
				{
					schedule(100);
				}
				else
				{
					_state.compareAndSet(ACPHelperState.USE, ACPHelperState.IDLE);
				}
			}
			else if(_state.compareAndSet(ACPHelperState.USE, ACPHelperState.APPLY))
			{
				schedule(100 + remaining);
			}
		}
		
		private long getReuseRemaining(ItemInstance item, Player player)
		{
			if((long) item.getTemplate().getReuseDelay() == 0)
			{
				return 0;
			}
			TimeStamp timeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup());
			if(timeStamp == null || !timeStamp.hasNotPassed())
			{
				return 0;
			}
			return Math.max(0, timeStamp.getEndTime() - System.currentTimeMillis());
		}
		
		private ItemInstance findUsableItem(Player player)
		{
			if(player.isInStoreMode() || player.isOutOfControl())
			{
				return null;
			}
			int[] itemIds = _type.getPotionsItemIds();
			ItemInstance candidateItem = null;
			for(int itemIdx = 0;itemIdx < itemIds.length;++itemIdx)
			{
				int itemId = itemIds[itemIdx];
				ItemInstance item = player.getInventory().getItemByItemId(itemId);
				if(item == null || !item.getTemplate().testCondition(player, item, false) || player.getInventory().isLockedItem(item))
					continue;
				if(candidateItem == null)
				{
					candidateItem = item;
					continue;
				}
				TimeStamp itemTimeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup());
				if(itemTimeStamp == null)
				{
					candidateItem = item;
					continue;
				}
				TimeStamp candidateTimeStamp = player.getSharedGroupReuse(candidateItem.getTemplate().getReuseGroup());
				if(candidateTimeStamp == null || candidateTimeStamp.getEndTime() <= itemTimeStamp.getEndTime())
					continue;
				candidateItem = item;
			}
			return candidateItem;
		}
		
		private boolean useItem(Player player, ItemInstance item)
		{
			if(item.getTemplate().getHandler().useItem(player, item, false))
			{
				long nextTimeUse = item.getTemplate().getReuseType().next(item);
				if(nextTimeUse > System.currentTimeMillis())
				{
					TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, (long) item.getTemplate().getReuseDelay());
					player.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);
					if(item.getTemplate().getReuseDelay() > 0)
					{
						player.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
					}
				}
				return true;
			}
			return false;
		}
		
		enum ACPHelperState
		{
			IDLE,
			APPLY,
			USE;
			
			
		}
	}
}