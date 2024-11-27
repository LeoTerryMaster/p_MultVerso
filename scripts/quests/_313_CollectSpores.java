package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _313_CollectSpores extends Quest implements ScriptFile
{
	public final int Herbiel = 30150;
	public final int SporeFungus = 20509;
	public final int SporeSac = 1118;
	
	public _313_CollectSpores()
	{
		super(false);
		addStartNpc(30150);
		addTalkId(30150);
		addKillId(20509);
		addQuestItem(1118);
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
		if(event.equalsIgnoreCase("green_q0313_05.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 8)
			{
				htmltext = "green_q0313_03.htm";
			}
			else
			{
				htmltext = "green_q0313_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			htmltext = "green_q0313_06.htm";
		}
		else if(cond == 2)
		{
			if(st.getQuestItemsCount(1118) < 10)
			{
				st.setCond(1);
				htmltext = "green_q0313_06.htm";
			}
			else
			{
				st.takeItems(1118, -1);
				st.giveItems(57, 3500, true);
				st.playSound("ItemSound.quest_finish");
				htmltext = "green_q0313_07.htm";
				st.exitCurrentQuest(true);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == 20509 && Rnd.chance(70))
		{
			st.giveItems(1118, 1);
			if(st.getQuestItemsCount(1118) < 10)
			{
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
				st.setState(2);
			}
		}
		return null;
	}
}