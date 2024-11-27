package npc.model;

import bosses.FourSepulchersSpawn;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.Future;

public class SepulcherRaidInstance extends RaidBossInstance
{
	public int mysteriousBoxId;
	private Future<?> _onDeadEventTask;
	
	public SepulcherRaidInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		Player player = killer.getPlayer();
		if(player != null)
		{
			giveCup(player);
		}
		if(_onDeadEventTask != null)
		{
			_onDeadEventTask.cancel(false);
		}
		_onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 5000);
	}
	
	@Override
	protected void onDelete()
	{
		_onDeadEventTask = null;
		super.onDelete();
	}
	
	private void giveCup(Player player)
	{
		int cupId = 0;
		switch(getNpcId())
		{
			case 25339:
			{
				cupId = 7256;
				break;
			}
			case 25342:
			{
				cupId = 7257;
				break;
			}
			case 25346:
			{
				cupId = 7258;
				break;
			}
			case 25349:
			{
				cupId = 7259;
			}
		}
		int oldBrooch = 7262;
		String questId = "_620_FourGoblets";
		if(player.getParty() != null)
		{
			for(Player mem : player.getParty().getPartyMembers())
			{
				QuestState qs = mem.getQuestState(questId);
				if(qs == null || !qs.isStarted() && !qs.isCompleted() || mem.getInventory().getItemByItemId(oldBrooch) != null || !player.isInRange(mem, 700))
					continue;
				Functions.addItem(mem, cupId, (long) 1);
			}
		}
		else
		{
			QuestState qs = player.getQuestState(questId);
			if(qs != null && (qs.isStarted() || qs.isCompleted()) && player.getInventory().getItemByItemId(oldBrooch) == null)
			{
				Functions.addItem(player, cupId, (long) 1);
			}
		}
	}
	
	@Override
	public boolean canChampion()
	{
		return false;
	}
	
	private class OnDeadEvent extends RunnableImpl
	{
		SepulcherRaidInstance _activeChar;
		
		public OnDeadEvent(SepulcherRaidInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			FourSepulchersSpawn.spawnEmperorsGraveNpc(_activeChar.mysteriousBoxId);
		}
	}
}