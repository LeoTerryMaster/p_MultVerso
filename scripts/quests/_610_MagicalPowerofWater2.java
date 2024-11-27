package quests;

import l2.commons.listener.Listener;
import l2.commons.util.Rnd;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _610_MagicalPowerofWater2 extends Quest implements ScriptFile
{
	private static final int ASEFA = 31372;
	private static final int VARKAS_HOLY_ALTAR = 31560;
	private static final int GREEN_TOTEM = 7238;
	private static final int Reward_First = 4589;
	private static final int Reward_Last = 4594;
	private static final int SoulOfWaterAshutar = 25316;
	int ICE_HEART_OF_ASHUTAR = 7239;
	private NpcInstance SoulOfWaterAshutarSpawn;
	
	public _610_MagicalPowerofWater2()
	{
		super(true);
		addStartNpc(31372);
		addTalkId(31560);
		addKillId(25316);
		addQuestItem(ICE_HEART_OF_ASHUTAR);
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		NpcInstance isQuest = GameObjectsStorage.getByNpcId(25316);
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "shaman_asefa_q0610_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("610_1"))
		{
			if(ServerVariables.getLong(_610_MagicalPowerofWater2.class.getSimpleName(), (long) 0) + 10800000 > System.currentTimeMillis())
			{
				htmltext = "totem_of_barka_q0610_0204.htm";
			}
			else if(st.getQuestItemsCount(7238) >= 1 && isQuest == null)
			{
				st.takeItems(7238, 1);
				SoulOfWaterAshutarSpawn = st.addSpawn(25316, 104825, -36926, -1136);
				SoulOfWaterAshutarSpawn.addListener((Listener) new DeathListener());
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "totem_of_barka_q0610_0203.htm";
			}
		}
		else if(event.equalsIgnoreCase("610_3"))
		{
			if(st.getQuestItemsCount(ICE_HEART_OF_ASHUTAR) >= 1)
			{
				st.takeItems(ICE_HEART_OF_ASHUTAR, -1);
				st.addExpAndSp(10000, 0);
				st.giveItems(Rnd.get(4589, 4594), 5, true);
				st.playSound("ItemSound.quest_finish");
				htmltext = "shaman_asefa_q0610_0301.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "shaman_asefa_q0610_0302.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		NpcInstance isQuest = GameObjectsStorage.getByNpcId(25316);
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31372)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 75)
				{
					if(st.getQuestItemsCount(7238) >= 1)
					{
						htmltext = "shaman_asefa_q0610_0101.htm";
					}
					else
					{
						htmltext = "shaman_asefa_q0610_0102.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "shaman_asefa_q0610_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "shaman_asefa_q0610_0105.htm";
			}
			else if(cond == 2)
			{
				htmltext = "shaman_asefa_q0610_0202.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(ICE_HEART_OF_ASHUTAR) >= 1)
			{
				htmltext = "shaman_asefa_q0610_0201.htm";
			}
		}
		else if(npcId == 31560)
		{
			if(!npc.isBusy())
			{
				if(ServerVariables.getLong(_610_MagicalPowerofWater2.class.getSimpleName(), (long) 0) + 10800000 > System.currentTimeMillis())
				{
					htmltext = "totem_of_barka_q0610_0204.htm";
				}
				else if(cond == 1)
				{
					htmltext = "totem_of_barka_q0610_0101.htm";
				}
				else if(cond == 2 && isQuest == null)
				{
					SoulOfWaterAshutarSpawn = st.addSpawn(25316, 104825, -36926, -1136);
					SoulOfWaterAshutarSpawn.addListener((Listener) new DeathListener());
					htmltext = "totem_of_barka_q0610_0204.htm";
				}
			}
			else
			{
				htmltext = "totem_of_barka_q0610_0202.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(ICE_HEART_OF_ASHUTAR) == 0 && npc.getNpcId() == 25316)
		{
			st.giveItems(ICE_HEART_OF_ASHUTAR, 1);
			st.setCond(3);
			if(SoulOfWaterAshutarSpawn != null)
			{
				SoulOfWaterAshutarSpawn.deleteMe();
			}
			SoulOfWaterAshutarSpawn = null;
		}
		return null;
	}
	
	private static class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			ServerVariables.set(_610_MagicalPowerofWater2.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
		}
	}
}