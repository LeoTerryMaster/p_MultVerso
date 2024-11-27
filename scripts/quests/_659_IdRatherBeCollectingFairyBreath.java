package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _659_IdRatherBeCollectingFairyBreath extends Quest implements ScriptFile
{
	public final int GALATEA = 30634;
	public final int[] MOBS = {20078, 21026, 21025, 21024, 21023};
	public final int FAIRY_BREATH = 8286;
	
	public _659_IdRatherBeCollectingFairyBreath()
	{
		super(false);
		addStartNpc(30634);
		addTalkId(30634);
		addTalkId(30634);
		addTalkId(30634);
		for(int i : MOBS)
		{
			addKillId(i);
		}
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
		if(event.equalsIgnoreCase("high_summoner_galatea_q0659_0103.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("high_summoner_galatea_q0659_0203.htm"))
		{
			long count = st.getQuestItemsCount(8286);
			if(count > 0)
			{
				long reward = count < 10 ? count * 50 : count * 50 + 5365;
				st.takeItems(8286, -1);
				st.giveItems(57, reward);
			}
		}
		else if(event.equalsIgnoreCase("high_summoner_galatea_q0659_0204.htm"))
		{
			st.exitCurrentQuest(true);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = 0;
		if(id != 1)
		{
			cond = st.getCond();
		}
		String htmltext = "noquest";
		if(npcId == 30634)
		{
			if(st.getPlayer().getLevel() < 26)
			{
				htmltext = "high_summoner_galatea_q0659_0102.htm";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				htmltext = "high_summoner_galatea_q0659_0101.htm";
			}
			else if(cond == 1)
			{
				htmltext = "high_summoner_galatea_q0659_0105.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
		{
			for(int i : MOBS)
			{
				if(npcId != i || !Rnd.chance(30))
					continue;
				st.giveItems(8286, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}