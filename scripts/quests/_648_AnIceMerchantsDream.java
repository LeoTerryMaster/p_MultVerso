package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

import java.util.ArrayList;
import java.util.List;

public class _648_AnIceMerchantsDream extends Quest implements ScriptFile
{
	private static final int Rafforty = 32020;
	private static final int Ice_Shelf = 32023;
	private static final int Silver_Hemocyte = 8057;
	private static final int Silver_Ice_Crystal = 8077;
	private static final int Black_Ice_Crystal = 8078;
	private static final int Silver_Hemocyte_Chance = 10;
	private static final int Silver2Black_Chance = 30;
	private static final List<Integer> silver2black = new ArrayList<>();
	
	public _648_AnIceMerchantsDream()
	{
		super(true);
		addStartNpc(Rafforty);
		addStartNpc(Ice_Shelf);
		for(int i = 22080;i <= 22098;++i)
		{
			if(i == 22095)
				continue;
			addKillId(i);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("repre_q0648_04.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("repre_q0648_22.htm") && _state == 2)
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		if(_state != 2)
		{
			return event;
		}
		long Silver_Ice_Crystal_Count = st.getQuestItemsCount(Silver_Ice_Crystal);
		long Black_Ice_Crystal_Count = st.getQuestItemsCount(Black_Ice_Crystal);
		if(event.equalsIgnoreCase("repre_q0648_14.htm"))
		{
			long reward = Silver_Ice_Crystal_Count * 300 + Black_Ice_Crystal_Count * 1200;
			if(reward <= 0)
				return "repre_q0648_15.htm";
			st.takeItems(Silver_Ice_Crystal, -1);
			st.takeItems(Black_Ice_Crystal, -1);
			st.giveItems(57, reward);
			return event;
		}
		if(event.equalsIgnoreCase("ice_lathe_q0648_06.htm"))
		{
			int char_obj_id = st.getPlayer().getObjectId();
			List<Integer> list = silver2black;
			synchronized(list)
			{
				if(silver2black.contains(char_obj_id))
				{
					return event;
				}
				if(Silver_Ice_Crystal_Count <= 0)
				{
					return "cheat.htm";
				}
				silver2black.add(char_obj_id);
			}
			st.takeItems(Silver_Ice_Crystal, 1);
			st.playSound("ItemSound2.broken_key");
			return event;
		}
		if(!event.equalsIgnoreCase("ice_lathe_q0648_08.htm"))
			return event;
		Integer char_obj_id = st.getPlayer().getObjectId();
		List<Integer> list = silver2black;
		synchronized(list)
		{
			if(!silver2black.contains(char_obj_id))
				return "cheat.htm";
			while(silver2black.contains(char_obj_id))
			{
				silver2black.remove(char_obj_id);
			}
		}
		if(Rnd.chance(Silver2Black_Chance))
		{
			st.giveItems(Black_Ice_Crystal, 1);
			st.playSound("ItemSound3.sys_enchant_sucess");
			return event;
		}
		else
		{
			st.playSound("ItemSound3.sys_enchant_failed");
			return "ice_lathe_q0648_09.htm";
		}
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int _state = st.getState();
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(_state == 1)
		{
			if(npcId == Rafforty)
			{
				if(st.getPlayer().getLevel() >= 53)
				{
					st.setCond(0);
					return "repre_q0648_03.htm";
				}
				st.exitCurrentQuest(true);
				return "repre_q0648_01.htm";
			}
			if(npcId == Ice_Shelf)
			{
				return "ice_lathe_q0648_01.htm";
			}
		}
		if(_state != 2)
		{
			return "noquest";
		}
		long Silver_Ice_Crystal_Count = st.getQuestItemsCount(Silver_Ice_Crystal);
		if(npcId == Ice_Shelf)
		{
			return Silver_Ice_Crystal_Count > 0 ? "ice_lathe_q0648_03.htm" : "ice_lathe_q0648_02.htm";
		}
		long Black_Ice_Crystal_Count = st.getQuestItemsCount(Black_Ice_Crystal);
		if(npcId == Rafforty)
		{
			QuestState st_115 = st.getPlayer().getQuestState(_115_TheOtherSideOfTruth.class);
			if(st_115 != null && st_115.isCompleted())
			{
				cond = 2;
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			if(cond == 1)
			{
				if(Silver_Ice_Crystal_Count > 0 || Black_Ice_Crystal_Count > 0)
				{
					return "repre_q0648_10.htm";
				}
				return "repre_q0648_08.htm";
			}
			if(cond == 2)
			{
				return Silver_Ice_Crystal_Count > 0 || Black_Ice_Crystal_Count > 0 ? "repre_q0648_11.htm" : "repre_q0648_09.htm";
			}
		}
		return "noquest";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		int cond = qs.getCond();
		if(cond > 0)
		{
			qs.rollAndGive(Silver_Ice_Crystal, 1, (double) (npc.getNpcId() - 22050));
			if(cond == 2)
			{
				qs.rollAndGive(Silver_Hemocyte, 1, (double) Silver_Hemocyte_Chance);
			}
		}
		return null;
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