package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _704_Missqueen extends Quest implements ScriptFile
{
	public final int m_q = 31760;
	public final int item_1 = 7832;
	public final int item_2 = 7833;
	
	public _704_Missqueen()
	{
		super(false);
		addStartNpc(31760);
		addTalkId(31760);
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
		String htmltext = "noquest";
		if(event.equals("31760-02.htm"))
		{
			if(st.getCond() == 0 && st.getPlayer().getLevel() <= 20 && st.getPlayer().getLevel() >= 6 && st.getPlayer().getPkKills() == 0)
			{
				st.giveItems(7832, 1);
				st.setCond(1);
				htmltext = "c_1.htm";
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "fail-01.htm";
			}
		}
		else if(event.equals("31760-03.htm"))
		{
			if(st.getInt("m_scond") == 0 && st.getPlayer().getLevel() <= 25 && st.getPlayer().getLevel() >= 20 && st.getPlayer().getPkKills() == 0)
			{
				st.giveItems(7833, 1);
				st.set("m_scond", "1");
				htmltext = "c_2.htm";
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "fail-02.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		if(npcId == 31760)
		{
			htmltext = "31760-01.htm";
		}
		return htmltext;
	}
}