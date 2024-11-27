package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExPlayScene;
import l2.gameserver.scripts.ScriptFile;

public class _127_KamaelAWindowToTheFuture extends Quest implements ScriptFile
{
	private final int AKLAN = 31288;
	private final int ALDER = 32092;
	private final int DOMINIC = 31350;
	private final int JURIS = 30113;
	private final int KLAUS = 30187;
	private final int OLTLIN = 30862;
	private final int RODEMAI = 30756;
	private final int MARK_DOMINIC = 8939;
	private final int MARK_HUMAN = 8940;
	private final int MARK_DWARF = 8941;
	private final int MARK_ELF = 8942;
	private final int MARK_DELF = 8943;
	private final int MARK_ORC = 8944;
	
	public _127_KamaelAWindowToTheFuture()
	{
		super(false);
		addStartNpc(DOMINIC);
		addTalkId(AKLAN);
		addTalkId(ALDER);
		addTalkId(JURIS);
		addTalkId(KLAUS);
		addTalkId(OLTLIN);
		addTalkId(RODEMAI);
		addQuestItem(MARK_DOMINIC, MARK_HUMAN, MARK_DWARF, MARK_ELF, MARK_DELF, MARK_ORC);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("dominic4.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(MARK_DOMINIC, 1);
		}
		else if(event.equalsIgnoreCase("dominic6.htm"))
		{
			st.takeItems(MARK_HUMAN, -1);
			st.takeItems(MARK_DWARF, -1);
			st.takeItems(MARK_ELF, -1);
			st.takeItems(MARK_DELF, -1);
			st.takeItems(MARK_ORC, -1);
			st.takeItems(MARK_DOMINIC, -1);
			st.giveItems(57, 15910, false);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("klaus6.htm"))
		{
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("klaus8.htm"))
		{
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("alder5.htm"))
		{
			st.setCond(4);
			st.giveItems(MARK_DWARF, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("aklan4.htm"))
		{
			st.setCond(5);
			st.giveItems(MARK_ORC, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("oltlin4.htm"))
		{
			st.setCond(6);
			st.giveItems(MARK_DELF, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("juris4.htm"))
		{
			st.setCond(7);
			st.giveItems(MARK_ELF, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("kamaelstory"))
		{
			htmltext = "rodemai6.htm";
			st.setCond(8);
			st.playSound("ItemSound.quest_middle");
			st.getPlayer().sendPacket(ExPlayScene.STATIC);
		}
		else if(event.equalsIgnoreCase("rodemai5.htm"))
		{
			st.setCond(9);
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == DOMINIC)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 1)
				{
					htmltext = "dominic1.htm";
				}
				else
				{
					htmltext = "dominic0.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 9)
			{
				htmltext = "dominic5.htm";
			}
		}
		else if(npcId == KLAUS)
		{
			if(cond == 1)
			{
				htmltext = "klaus1.htm";
			}
			else if(cond == 2)
			{
				htmltext = "klaus6.htm";
			}
		}
		else if(npcId == ALDER)
		{
			if(cond == 3)
			{
				htmltext = "alder1.htm";
			}
		}
		else if(npcId == AKLAN)
		{
			if(cond == 4)
			{
				htmltext = "aklan1.htm";
			}
		}
		else if(npcId == OLTLIN)
		{
			if(cond == 5)
			{
				htmltext = "oltlin1.htm";
			}
		}
		else if(npcId == JURIS)
		{
			if(cond == 6)
			{
				htmltext = "juris1.htm";
			}
		}
		else if(npcId == RODEMAI)
		{
			if(cond == 7)
			{
				htmltext = "rodemai1.htm";
			}
			else if(cond == 8)
			{
				htmltext = "rodemai4.htm";
			}
		}
		return htmltext;
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
}