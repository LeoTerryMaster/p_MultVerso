package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _298_LizardmensConspiracy extends Quest implements ScriptFile
{
	public final int PRAGA = 30333;
	public final int ROHMER = 30344;
	public final int MAILLE_LIZARDMAN_WARRIOR = 20922;
	public final int MAILLE_LIZARDMAN_SHAMAN = 20923;
	public final int MAILLE_LIZARDMAN_MATRIARCH = 20924;
	public final int POISON_ARANEID = 20926;
	public final int KING_OF_THE_ARANEID = 20927;
	public final int REPORT = 7182;
	public final int SHINING_GEM = 7183;
	public final int SHINING_RED_GEM = 7184;
	public final int[][] MobsTable = {{20922, 7183}, {20923, 7183}, {20924, 7183}, {20926, 7184}, {20927, 7184}};
	
	public _298_LizardmensConspiracy()
	{
		super(false);
		addStartNpc(30333);
		addTalkId(30333);
		addTalkId(30344);
		for(int[] element : MobsTable)
		{
			addKillId(element[0]);
		}
		addQuestItem(7182, 7183, 7184);
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
		if(event.equalsIgnoreCase("guard_praga_q0298_0104.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.giveItems(7182, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("magister_rohmer_q0298_0201.htm"))
		{
			st.takeItems(7182, -1);
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("magister_rohmer_q0298_0301.htm") && st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) > 99)
		{
			st.takeItems(7183, -1);
			st.takeItems(7184, -1);
			st.addExpAndSp(0, 42000);
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30333)
		{
			if(cond < 1)
			{
				if(st.getPlayer().getLevel() < 25)
				{
					htmltext = "guard_praga_q0298_0102.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "guard_praga_q0298_0101.htm";
				}
			}
			if(cond == 1)
			{
				htmltext = "guard_praga_q0298_0105.htm";
			}
		}
		else if(npcId == 30344)
		{
			if(cond < 1)
			{
				htmltext = "magister_rohmer_q0298_0202.htm";
			}
			else if(cond == 1)
			{
				htmltext = "magister_rohmer_q0298_0101.htm";
			}
			else if(cond == 2 | st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) < 100)
			{
				htmltext = "magister_rohmer_q0298_0204.htm";
				st.setCond(2);
			}
			else if(cond == 3 && st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) > 99)
			{
				htmltext = "magister_rohmer_q0298_0203.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int rand = Rnd.get(10);
		if(st.getCond() == 2)
		{
			for(int[] element : MobsTable)
			{
				if(npcId != element[0] || rand >= 6 || st.getQuestItemsCount(element[1]) >= 50)
					continue;
				if(rand < 2 && element[1] == 7183)
				{
					st.giveItems(element[1], 2);
				}
				else
				{
					st.giveItems(element[1], 1);
				}
				if(st.getQuestItemsCount(7183) + st.getQuestItemsCount(7184) > 99)
				{
					st.setCond(3);
					st.playSound("ItemSound.quest_middle");
					continue;
				}
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}