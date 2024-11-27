package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _262_TradewiththeIvoryTower extends Quest implements ScriptFile
{
	public final int VOLODOS = 30137;
	public final int GREEN_FUNGUS = 20007;
	public final int BLOOD_FUNGUS = 20400;
	public final int FUNGUS_SAC = 707;
	
	public _262_TradewiththeIvoryTower()
	{
		super(false);
		addStartNpc(30137);
		addKillId(20400, 20007);
		addQuestItem(707);
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
		if(event.equals("vollodos_q0262_03.htm"))
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
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 8)
			{
				htmltext = "vollodos_q0262_02.htm";
				return htmltext;
			}
			htmltext = "vollodos_q0262_01.htm";
			st.exitCurrentQuest(true);
		}
		else if(cond == 1 && st.getQuestItemsCount(707) < 10)
		{
			htmltext = "vollodos_q0262_04.htm";
		}
		else if(cond == 2 && st.getQuestItemsCount(707) >= 10)
		{
			st.giveItems(57, 3000);
			st.takeItems(707, -1);
			st.setCond(0);
			st.playSound("ItemSound.quest_finish");
			htmltext = "vollodos_q0262_05.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int random = Rnd.get(10);
		if(st.getCond() == 1 && st.getQuestItemsCount(707) < 10 && (npcId == 20007 && random < 3 || npcId == 20400 && random < 4))
		{
			st.giveItems(707, 1);
			if(st.getQuestItemsCount(707) == 10)
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}