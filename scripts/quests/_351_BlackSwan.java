package quests;

import l2.gameserver.model.base.Experience;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _351_BlackSwan extends Quest implements ScriptFile
{
	private static final int Gosta = 30916;
	private static final int Heine = 30969;
	private static final int Ferris = 30847;
	private static final int ORDER_OF_GOSTA = 4296;
	private static final int LIZARD_FANG = 4297;
	private static final int BARREL_OF_LEAGUE = 4298;
	private static final int BILL_OF_IASON_HEINE = 4310;
	private static final int CHANCE = 100;
	private static final int CHANCE2 = 5;
	
	public _351_BlackSwan()
	{
		super(false);
		addStartNpc(30916);
		addTalkId(30969);
		addTalkId(30847);
		addKillId(20784, 20785, 21639, 21640, 21642, 21643);
		addQuestItem(4296, 4297, 4298);
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
		String htmltext = event;
		long amount = st.getQuestItemsCount(4297);
		long amount2 = st.getQuestItemsCount(4298);
		if(event.equalsIgnoreCase("30916-03.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.giveItems(4296, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30969-02a.htm") && amount > 0)
		{
			htmltext = "30969-02.htm";
			st.giveItems(57, amount * 30, false);
			st.takeItems(4297, -1);
		}
		else if(event.equalsIgnoreCase("30969-03a.htm") && amount2 > 0)
		{
			htmltext = "30969-03.htm";
			st.setCond(2);
			st.giveItems(57, amount2 * 500, false);
			st.giveItems(4310, amount2, false);
			st.takeItems(4298, -1);
		}
		else if(event.equalsIgnoreCase("30969-01.htm") && st.getCond() == 2)
		{
			htmltext = "30969-04.htm";
		}
		else if(event.equalsIgnoreCase("5"))
		{
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
			htmltext = "";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30916)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 32)
				{
					htmltext = "30916-01.htm";
				}
				else
				{
					htmltext = "30916-00.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond >= 1)
			{
				htmltext = "30916-04.htm";
			}
		}
		if(npcId == 30969)
		{
			if(cond == 1)
			{
				htmltext = "30969-01.htm";
			}
			if(cond == 2)
			{
				htmltext = "30969-04.htm";
			}
		}
		if(npcId == 30847)
		{
			htmltext = "30847.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		double mod = Experience.penaltyModifier((long) st.calculateLevelDiffForDrop(npc.getLevel(), st.getPlayer().getLevel()), 9.0);
		st.rollAndGive(4297, 1, 100.0 * mod);
		st.rollAndGive(4298, 1, 5.0 * mod);
		return null;
	}
}