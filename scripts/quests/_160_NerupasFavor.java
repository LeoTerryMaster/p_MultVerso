package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _160_NerupasFavor extends Quest implements ScriptFile
{
	private static final int SILVERY_SPIDERSILK = 1026;
	private static final int UNOS_RECEIPT = 1027;
	private static final int CELS_TICKET = 1028;
	private static final int NIGHTSHADE_LEAF = 1029;
	private static final int LESSER_HEALING_POTION = 1060;
	private static final int NERUPA = 30370;
	private static final int UNOREN = 30147;
	private static final int CREAMEES = 30149;
	private static final int JULIA = 30152;
	private static final int COND1 = 1;
	private static final int COND2 = 2;
	private static final int COND3 = 3;
	private static final int COND4 = 4;
	
	public _160_NerupasFavor()
	{
		super(false);
		addStartNpc(NERUPA);
		addTalkId(UNOREN, CREAMEES, JULIA);
		addQuestItem(SILVERY_SPIDERSILK, UNOS_RECEIPT, CELS_TICKET, NIGHTSHADE_LEAF);
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
		if(event.equals("30370-04.htm"))
		{
			st.setCond(COND1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(SILVERY_SPIDERSILK, 1);
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
		if(npcId == NERUPA)
		{
			if(st.getState() == 1)
			{
				if(st.getPlayer().getRace() != Race.elf)
				{
					htmltext = "30370-00.htm";
				}
				else if(st.getPlayer().getLevel() < 3)
				{
					htmltext = "30370-02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "30370-03.htm";
				}
			}
			else if(cond == COND1)
			{
				htmltext = "30370-04.htm";
			}
			else if(cond == COND4 && st.getQuestItemsCount(NIGHTSHADE_LEAF) > 0)
			{
				st.takeItems(NIGHTSHADE_LEAF, -1);
				st.giveItems(LESSER_HEALING_POTION, 5);
				st.addExpAndSp(1000, 0);
				st.playSound("ItemSound.quest_finish");
				htmltext = "30370-06.htm";
				st.exitCurrentQuest(false);
			}
			else
			{
				htmltext = "30370-05.htm";
			}
		}
		else if(npcId == UNOREN)
		{
			if(cond == COND1)
			{
				st.takeItems(SILVERY_SPIDERSILK, -1);
				st.giveItems(UNOS_RECEIPT, 1);
				st.setCond(COND2);
				htmltext = "30147-01.htm";
			}
			else if(cond == COND2 || cond == COND3)
			{
				htmltext = "30147-02.htm";
			}
			else if(cond == COND4)
			{
				htmltext = "30147-03.htm";
			}
		}
		else if(npcId == CREAMEES)
		{
			if(cond == COND2)
			{
				st.takeItems(UNOS_RECEIPT, -1);
				st.giveItems(CELS_TICKET, 1);
				st.setCond(COND3);
				htmltext = "30149-01.htm";
			}
			else if(cond == COND3)
			{
				htmltext = "30149-02.htm";
			}
			else if(cond == COND4)
			{
				htmltext = "30149-03.htm";
			}
		}
		else if(npcId == JULIA)
		{
			if(cond == COND3)
			{
				st.takeItems(CELS_TICKET, -1);
				st.giveItems(NIGHTSHADE_LEAF, 1);
				htmltext = "30152-01.htm";
				st.setCond(COND4);
			}
			else if(cond == COND4)
			{
				htmltext = "30152-02.htm";
			}
		}
		return htmltext;
	}
}