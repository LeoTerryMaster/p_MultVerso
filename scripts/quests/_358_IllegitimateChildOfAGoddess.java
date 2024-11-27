package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _358_IllegitimateChildOfAGoddess extends Quest implements ScriptFile
{
	private static final int grandmaster_oltlin = 30862;
	private static final int trives = 20672;
	private static final int falibati = 20673;
	private static final int snake_scale = 5868;
	private static final int rp_sealed_phoenixs_necklace_i = 6329;
	private static final int rp_sealed_phoenixs_earing_i = 6331;
	private static final int rp_sealed_phoenixs_ring_i = 6333;
	private static final int rp_sealed_majestic_necklace_i = 6335;
	private static final int rp_sealed_majestic_earing_i = 6337;
	private static final int rp_sealed_majestic_ring_i = 6339;
	private static final int rp_sealed_dark_crystal_shield_i = 5364;
	private static final int rp_sealed_shield_of_nightmare_i = 5366;
	
	public _358_IllegitimateChildOfAGoddess()
	{
		super(false);
		addStartNpc(30862);
		addKillId(20672, 20673);
		addQuestItem(5868);
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
		int npcId = npc.getNpcId();
		if(npcId == 30862)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("an_illegitimate_child_of_godness", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "grandmaster_oltlin_q0358_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "grandmaster_oltlin_q0358_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("an_illegitimate_child_of_godness");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30862)
					break;
				if(st.getPlayer().getLevel() < 63)
				{
					htmltext = "grandmaster_oltlin_q0358_01.htm";
					st.exitCurrentQuest(true);
					break;
				}
				if(st.getPlayer().getLevel() < 63)
					break;
				htmltext = "grandmaster_oltlin_q0358_02.htm";
				break;
			}
			case 2:
			{
				if(npcId != 30862)
					break;
				if(GetMemoState == 1 && st.getQuestItemsCount(5868) < 108)
				{
					htmltext = "grandmaster_oltlin_q0358_06.htm";
					break;
				}
				if(GetMemoState != 1 || st.getQuestItemsCount(5868) < 108)
					break;
				int i0 = Rnd.get(1000);
				if(i0 < 125)
				{
					st.giveItems(6331, 1);
				}
				else if(i0 < 250)
				{
					st.giveItems(6337, 1);
				}
				else if(i0 < 375)
				{
					st.giveItems(6329, 1);
				}
				else if(i0 < 500)
				{
					st.giveItems(6335, 1);
				}
				else if(i0 < 625)
				{
					st.giveItems(6333, 1);
				}
				else if(i0 < 750)
				{
					st.giveItems(6339, 1);
				}
				else if(i0 < 875)
				{
					st.giveItems(5366, 1);
				}
				else
				{
					st.giveItems(5364, 1);
				}
				st.takeItems(5868, -1);
				st.unset("an_illegitimate_child_of_godness");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "grandmaster_oltlin_q0358_07.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("an_illegitimate_child_of_godness");
		int npcId = npc.getNpcId();
		if(GetMemoState == 1)
		{
			if(npcId == 20672)
			{
				if(st.getQuestItemsCount(5868) < 108 && Rnd.get(100) < 71)
				{
					st.giveItems(5868, 1);
					st.playSound("ItemSound.quest_itemget");
					if(st.getQuestItemsCount(5868) >= 107)
					{
						st.setCond(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 20673 && st.getQuestItemsCount(5868) < 108 && Rnd.get(100) < 74)
			{
				st.giveItems(5868, 1);
				if(st.getQuestItemsCount(5868) >= 107)
				{
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}