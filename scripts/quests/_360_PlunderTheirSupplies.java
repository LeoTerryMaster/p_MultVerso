package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _360_PlunderTheirSupplies extends Quest implements ScriptFile
{
	private static final int COLEMAN = 30873;
	private static final int TAIK_SEEKER = 20666;
	private static final int TAIK_LEADER = 20669;
	private static final int SUPPLY_ITEM = 5872;
	private static final int SUSPICIOUS_DOCUMENT = 5871;
	private static final int RECIPE_OF_SUPPLY = 5870;
	private static final int ITEM_DROP_SEEKER = 50;
	private static final int ITEM_DROP_LEADER = 65;
	private static final int DOCUMENT_DROP = 5;
	
	public _360_PlunderTheirSupplies()
	{
		super(false);
		addStartNpc(30873);
		addKillId(20666);
		addKillId(20669);
		addQuestItem(5872);
		addQuestItem(5871);
		addQuestItem(5870);
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
		if(event.equalsIgnoreCase("guard_coleman_q0360_04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("guard_coleman_q0360_10.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		int id = st.getState();
		long docs = st.getQuestItemsCount(5870);
		long supplies = st.getQuestItemsCount(5872);
		if(id != 2)
		{
			htmltext = st.getPlayer().getLevel() >= 52 ? "guard_coleman_q0360_02.htm" : "guard_coleman_q0360_01.htm";
		}
		else if(docs > 0 || supplies > 0)
		{
			st.takeItems(5872, -1);
			st.takeItems(5870, -1);
			long reward = 6000 + supplies * 100 + docs * 6000;
			st.giveItems(57, reward);
			htmltext = "guard_coleman_q0360_08.htm";
		}
		else
		{
			htmltext = "guard_coleman_q0360_05.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 20666 && Rnd.chance(50) || npcId == 20669 && Rnd.chance(65))
		{
			st.giveItems(5872, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		if(Rnd.chance(5))
		{
			if(st.getQuestItemsCount(5871) < 4)
			{
				st.giveItems(5871, 1);
			}
			else
			{
				st.takeItems(5871, -1);
				st.giveItems(5870, 1);
			}
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}