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

public class _616_MagicalPowerofFire2 extends Quest implements ScriptFile
{
	private static final int KETRAS_HOLY_ALTAR = 31558;
	private static final int UDAN = 31379;
	private static final int FIRE_HEART_OF_NASTRON = 7244;
	private static final int RED_TOTEM = 7243;
	private static final int Reward_First = 4589;
	private static final int Reward_Last = 4594;
	private static final int SoulOfFireNastron = 25306;
	private NpcInstance SoulOfFireNastronSpawn;
	
	public _616_MagicalPowerofFire2()
	{
		super(true);
		addStartNpc(31379);
		addTalkId(31558);
		addKillId(25306);
		addQuestItem(7244);
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
		NpcInstance isQuest = GameObjectsStorage.getByNpcId(25306);
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "shaman_udan_q0616_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("616_1"))
		{
			if(ServerVariables.getLong(_616_MagicalPowerofFire2.class.getSimpleName(), (long) 0) + 10800000 > System.currentTimeMillis())
			{
				htmltext = "totem_of_ketra_q0616_0204.htm";
			}
			else if(st.getQuestItemsCount(7243) >= 1 && isQuest == null)
			{
				st.takeItems(7243, 1);
				SoulOfFireNastronSpawn = st.addSpawn(25306, 142528, -82528, -6496);
				SoulOfFireNastronSpawn.addListener((Listener) new DeathListener());
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "totem_of_ketra_q0616_0203.htm";
			}
		}
		else if(event.equalsIgnoreCase("616_3"))
		{
			if(st.getQuestItemsCount(7244) >= 1)
			{
				st.takeItems(7244, -1);
				st.addExpAndSp(10000, 0);
				st.giveItems(Rnd.get(Reward_First, Reward_Last), 5, true);
				st.playSound("ItemSound.quest_finish");
				htmltext = "shaman_udan_q0616_0301.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "shaman_udan_q0616_0302.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		NpcInstance isQuest = GameObjectsStorage.getByNpcId(25306);
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case 31379:
			{
				if(cond == 0)
				{
					if(st.getPlayer().getLevel() >= 75)
					{
						if(st.getQuestItemsCount(7243) >= 1)
						{
							htmltext = "shaman_udan_q0616_0101.htm";
							break;
						}
						htmltext = "shaman_udan_q0616_0102.htm";
						st.exitCurrentQuest(true);
						break;
					}
					htmltext = "shaman_udan_q0616_0103.htm";
					st.exitCurrentQuest(true);
					break;
				}
				if(cond == 1)
				{
					htmltext = "shaman_udan_q0616_0105.htm";
					break;
				}
				if(cond == 2)
				{
					htmltext = "shaman_udan_q0616_0202.htm";
					break;
				}
				if(cond != 3 || st.getQuestItemsCount(7244) < 1)
					break;
				htmltext = "shaman_udan_q0616_0201.htm";
				break;
			}
			case 31558:
			{
				if(ServerVariables.getLong(_616_MagicalPowerofFire2.class.getSimpleName(), (long) 0) + 10800000 > System.currentTimeMillis())
				{
					htmltext = "totem_of_ketra_q0616_0204.htm";
					break;
				}
				if(npc.isBusy())
				{
					htmltext = "totem_of_ketra_q0616_0202.htm";
					break;
				}
				if(cond == 1)
				{
					htmltext = "totem_of_ketra_q0616_0101.htm";
					break;
				}
				if(cond != 2)
					break;
				if(isQuest == null)
				{
					SoulOfFireNastronSpawn = st.addSpawn(25306, 142528, -82528, -6496);
					SoulOfFireNastronSpawn.addListener((Listener) new DeathListener());
					htmltext = "totem_of_ketra_q0616_0204.htm";
					break;
				}
				htmltext = "<html><body>Already in spawn.</body></html>";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(7244) == 0)
		{
			st.giveItems(7244, 1);
			st.setCond(3);
			if(SoulOfFireNastronSpawn != null)
			{
				SoulOfFireNastronSpawn.deleteMe();
			}
			SoulOfFireNastronSpawn = null;
		}
		return null;
	}
	
	private static class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			ServerVariables.set(_616_MagicalPowerofFire2.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
		}
	}
}