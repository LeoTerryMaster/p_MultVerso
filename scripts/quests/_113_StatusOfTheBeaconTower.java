package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _113_StatusOfTheBeaconTower extends Quest implements ScriptFile
{
	private static final int MOIRA = 31979;
	private static final int TORRANT = 32016;
	private static final int BOX = 8086;
	
	public _113_StatusOfTheBeaconTower()
	{
		super(false);
		addStartNpc(31979);
		addTalkId(32016);
		addQuestItem(8086);
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
		if(event.equalsIgnoreCase("seer_moirase_q0113_0104.htm"))
		{
			st.setCond(1);
			st.giveItems(8086, 1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("torant_q0113_0201.htm"))
		{
			st.giveItems(57, 12020);
			st.takeItems(8086, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
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
		if(id == 3)
		{
			htmltext = "completed";
		}
		else if(npcId == 31979)
		{
			if(id == 1)
			{
				if(st.getPlayer().getLevel() >= 40)
				{
					htmltext = "seer_moirase_q0113_0101.htm";
				}
				else
				{
					htmltext = "seer_moirase_q0113_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "seer_moirase_q0113_0105.htm";
			}
		}
		else if(npcId == 32016 && st.getQuestItemsCount(8086) == 1)
		{
			htmltext = "torant_q0113_0101.htm";
		}
		return htmltext;
	}
}