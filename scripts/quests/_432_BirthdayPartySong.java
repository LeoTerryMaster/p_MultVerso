package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _432_BirthdayPartySong extends Quest implements ScriptFile
{
	private static final int MELODY_MAESTRO_OCTAVIA = 31043;
	private static final int ROUGH_HEWN_ROCK_GOLEMS = 21103;
	private static final int RED_CRYSTALS = 7541;
	private static final int BIRTHDAY_ECHO_CRYSTAL = 7061;
	
	public _432_BirthdayPartySong()
	{
		super(false);
		addStartNpc(MELODY_MAESTRO_OCTAVIA);
		addKillId(ROUGH_HEWN_ROCK_GOLEMS);
		addQuestItem(RED_CRYSTALS);
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
		if(event.equalsIgnoreCase("muzyko_q0432_0104.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("muzyko_q0432_0201.htm"))
		{
			if(st.getQuestItemsCount(RED_CRYSTALS) == 50)
			{
				st.takeItems(RED_CRYSTALS, -1);
				st.giveItems(BIRTHDAY_ECHO_CRYSTAL, 25);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "muzyko_q0432_0202.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int condition = st.getCond();
		int npcId = npc.getNpcId();
		if(npcId == MELODY_MAESTRO_OCTAVIA)
		{
			if(condition == 0)
			{
				if(st.getPlayer().getLevel() >= 31)
				{
					htmltext = "muzyko_q0432_0101.htm";
				}
				else
				{
					htmltext = "muzyko_q0432_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(condition == 1)
			{
				htmltext = "muzyko_q0432_0106.htm";
			}
			else if(condition == 2 && st.getQuestItemsCount(RED_CRYSTALS) == 50)
			{
				htmltext = "muzyko_q0432_0105.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(npcId == ROUGH_HEWN_ROCK_GOLEMS && st.getCond() == 1 && st.getQuestItemsCount(RED_CRYSTALS) < 50)
		{
			st.giveItems(RED_CRYSTALS, 1);
			if(st.getQuestItemsCount(RED_CRYSTALS) == 50)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}