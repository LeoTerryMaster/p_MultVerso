package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _167_DwarvenKinship extends Quest implements ScriptFile
{
	private static final int Carlon = 30350;
	private static final int Haprock = 30255;
	private static final int Norman = 30210;
	private static final int CarlonsLetter = 1076;
	private static final int NormansLetter = 1106;
	
	public _167_DwarvenKinship()
	{
		super(false);
		addStartNpc(30350);
		addTalkId(30255);
		addTalkId(30210);
		addQuestItem(1076, 1106);
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
		if(event.equalsIgnoreCase("30350-04.htm"))
		{
			st.giveItems(1076, 1);
			st.playSound("ItemSound.quest_accept");
			st.setCond(1);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30255-03.htm"))
		{
			st.takeItems(1076, -1);
			st.giveItems(57, 2000);
			st.giveItems(1106, 1);
			st.setCond(2);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30255-04.htm"))
		{
			st.takeItems(1076, -1);
			st.giveItems(57, 2000);
			st.playSound("ItemSound.quest_giveup");
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("30210-02.htm"))
		{
			st.takeItems(1106, -1);
			st.giveItems(57, 22000);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30350)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 15)
				{
					htmltext = "30350-03.htm";
				}
				else
				{
					htmltext = "30350-02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond > 0)
			{
				htmltext = "30350-05.htm";
			}
		}
		else if(npcId == 30255)
		{
			if(cond == 1)
			{
				htmltext = "30255-01.htm";
			}
			else if(cond > 1)
			{
				htmltext = "30255-05.htm";
			}
		}
		else if(npcId == 30210 && cond == 2)
		{
			htmltext = "30210-01.htm";
		}
		return htmltext;
	}
}