package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _164_BloodFiend extends Quest implements ScriptFile
{
	private static final int Creamees = 30149;
	private static final int KirunakSkull = 1044;
	private static final int Kirunak = 27021;
	
	public _164_BloodFiend()
	{
		super(false);
		addStartNpc(30149);
		addTalkId(30149);
		addKillId(27021);
		addQuestItem(1044);
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
		if(event.equalsIgnoreCase("30149-04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
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
		if(npcId == 30149)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.darkelf)
				{
					htmltext = "30149-00.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 21)
				{
					htmltext = "30149-02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "30149-03.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "30149-05.htm";
			}
			else if(cond == 2)
			{
				st.takeItems(1044, -1);
				st.giveItems(57, 42130, true);
				htmltext = "30149-06.htm";
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == 27021)
		{
			if(st.getQuestItemsCount(1044) == 0)
			{
				st.giveItems(1044, 1);
			}
			st.playSound("ItemSound.quest_middle");
			st.setCond(2);
			st.setState(2);
		}
		return null;
	}
}