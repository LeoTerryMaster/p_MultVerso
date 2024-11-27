package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _639_GuardiansOfTheHolyGrail extends Quest implements ScriptFile
{
	private static final int DROP_CHANCE = 10;
	private static final int DOMINIC = 31350;
	private static final int GREMORY = 32008;
	private static final int GRAIL = 32028;
	private static final int SCRIPTURES = 8069;
	private static final int WATER_BOTTLE = 8070;
	private static final int HOLY_WATER_BOTTLE = 8071;
	private static final int EAS = 960;
	private static final int EWS = 959;
	
	public _639_GuardiansOfTheHolyGrail()
	{
		super(true);
		addStartNpc(31350);
		addTalkId(32008);
		addTalkId(32028);
		addQuestItem(8069);
		int i = 22122;
		while(i <= 22136)
		{
			addKillId(i++);
		}
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
		if(event.equals("falsepriest_dominic_q0639_04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("falsepriest_dominic_q0639_09.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equals("falsepriest_dominic_q0639_08.htm"))
		{
			st.giveItems(57, st.takeAllItems(8069) * 1625, false);
		}
		else if(event.equals("falsepriest_gremory_q0639_05.htm"))
		{
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(8070, 1, false);
		}
		else if(event.equals("holy_grail_q0639_02.htm"))
		{
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
			st.takeItems(8070, -1);
			st.giveItems(8071, 1);
		}
		else if(event.equals("falsepriest_gremory_q0639_09.htm"))
		{
			st.setCond(4);
			st.playSound("ItemSound.quest_middle");
			st.takeItems(8071, -1);
		}
		else if(event.equals("falsepriest_gremory_q0639_11.htm"))
		{
			st.takeItems(8069, 4000);
			st.giveItems(959, 1, false);
		}
		else if(event.equals("falsepriest_gremory_q0639_13.htm"))
		{
			st.takeItems(8069, 400);
			st.giveItems(960, 1, false);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(npcId == 31350)
		{
			if(id == 1)
			{
				htmltext = st.getPlayer().getLevel() >= 73 ? "falsepriest_dominic_q0639_01.htm" : "falsepriest_dominic_q0639_02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = st.getQuestItemsCount(8069) >= 1 ? "falsepriest_dominic_q0639_05.htm" : "falsepriest_dominic_q0639_06.htm";
			}
		}
		else if(npcId == 32008)
		{
			if(cond == 1)
			{
				htmltext = "falsepriest_gremory_q0639_01.htm";
			}
			else if(cond == 2)
			{
				htmltext = "falsepriest_gremory_q0639_06.htm";
			}
			else if(cond == 3)
			{
				htmltext = "falsepriest_gremory_q0639_08.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(8069) < 400)
			{
				htmltext = "falsepriest_gremory_q0639_09.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(8069) >= 4000)
			{
				htmltext = "falsepriest_gremory_q0639_10.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(8069) >= 400 && st.getQuestItemsCount(8069) < 4000)
			{
				htmltext = "falsepriest_gremory_q0639_14.htm";
			}
		}
		else if(npcId == 32028 && cond == 2)
		{
			htmltext = "holy_grail_q0639_01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(8069, 1, 10.0 * npc.getTemplate().rateHp);
		return null;
	}
}