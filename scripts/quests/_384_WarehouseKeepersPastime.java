package quests;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class _384_WarehouseKeepersPastime extends Quest implements ScriptFile
{
	private static final int Cliff = 30182;
	private static final int Baxt = 30685;
	private static final int Warehouse_Keepers_Medal = 5964;
	private static final Map<Integer, Integer> Medal_Chances = new HashMap<>();
	private static final Map<Integer, Bingo> bingos = new HashMap<>();
	private static final int[][] Rewards_Win = {{16, 1888, 1}, {32, 1887, 1}, {50, 1894, 1}, {80, 952, 1}, {89, 1890, 1}, {98, 1893, 1}, {100, 951, 1}};
	private static final int[][] Rewards_Win_Big = {{50, 883, 1}, {80, 951, 1}, {98, 852, 1}, {100, 401, 1}};
	private static final int[][] Rewards_Lose = {{50, 4041, 1}, {80, 952, 1}, {98, 1892, 1}, {100, 917, 1}};
	private static final int[][] Rewards_Lose_Big = {{50, 951, 1}, {80, 500, 1}, {98, 2437, 2}, {100, 135, 1}};
	
	public _384_WarehouseKeepersPastime()
	{
		super(false);
		addStartNpc(30182);
		addTalkId(30685);
		Medal_Chances.put(20948, 18);
		Medal_Chances.put(20945, 12);
		Medal_Chances.put(20946, 15);
		Medal_Chances.put(20947, 16);
		Medal_Chances.put(20635, 15);
		Medal_Chances.put(20773, 61);
		Medal_Chances.put(20774, 60);
		Medal_Chances.put(20760, 24);
		Medal_Chances.put(20758, 24);
		Medal_Chances.put(20759, 23);
		Medal_Chances.put(20242, 22);
		Medal_Chances.put(20281, 22);
		Medal_Chances.put(20556, 14);
		Medal_Chances.put(20668, 21);
		Medal_Chances.put(20241, 22);
		Medal_Chances.put(20286, 22);
		Medal_Chances.put(20950, 20);
		Medal_Chances.put(20949, 19);
		Medal_Chances.put(20942, 9);
		Medal_Chances.put(20943, 12);
		Medal_Chances.put(20944, 11);
		Medal_Chances.put(20559, 14);
		Medal_Chances.put(20243, 21);
		Medal_Chances.put(20282, 21);
		Medal_Chances.put(20677, 34);
		Medal_Chances.put(20605, 15);
		Iterator<Integer> iterator = Medal_Chances.keySet().iterator();
		while(iterator.hasNext())
		{
			int id = iterator.next();
			addKillId(id);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		long medals = st.getQuestItemsCount(5964);
		if(event.equalsIgnoreCase("30182-05.htm") && _state == 1)
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if((event.equalsIgnoreCase("30182-08.htm") || event.equalsIgnoreCase("30685-08.htm")) && _state == 2)
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		else
		{
			if(event.contains("-game") && _state == 2)
			{
				int need_medals;
				boolean big_game = event.contains("-big");
				int n = need_medals = big_game ? 100 : 10;
				if(medals < (long) need_medals)
				{
					return event.replaceFirst("-big", "").replaceFirst("game", "09.htm");
				}
				st.takeItems(5964, (long) need_medals);
				int char_obj_id = st.getPlayer().getObjectId();
				if(bingos.containsKey(char_obj_id))
				{
					bingos.remove(char_obj_id);
				}
				Bingo bingo = new Bingo(big_game, st);
				bingos.put(char_obj_id, bingo);
				return bingo.getDialog("");
			}
			if(event.contains("choice-") && _state == 2)
			{
				int char_obj_id = st.getPlayer().getObjectId();
				if(!bingos.containsKey(char_obj_id))
				{
					return null;
				}
				Bingo bingo = bingos.get(char_obj_id);
				return bingo.Select(event.replaceFirst("choice-", ""));
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int _state = st.getState();
		int npcId = npc.getNpcId();
		if(_state == 1)
		{
			if(npcId != 30182)
			{
				return "noquest";
			}
			if(st.getPlayer().getLevel() < 40)
			{
				st.exitCurrentQuest(true);
				return "30182-04.htm";
			}
			st.setCond(0);
			return "30182-01.htm";
		}
		if(_state != 2)
		{
			return "noquest";
		}
		long medals = st.getQuestItemsCount(5964);
		if(medals >= 100)
		{
			return String.valueOf(npcId) + "-06.htm";
		}
		if(medals >= 10)
		{
			return String.valueOf(npcId) + "-06a.htm";
		}
		return String.valueOf(npcId) + "-06b.htm";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		Integer chance = Medal_Chances.get(npc.getNpcId());
		if(chance != null && Rnd.chance((double) chance.intValue() * Config.RATE_QUESTS_REWARD))
		{
			qs.giveItems(5964, 1);
			qs.playSound(qs.getQuestItemsCount(5964) == 10 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
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
	
	public static class Bingo extends quests.Bingo
	{
		protected static final String msg_begin = "I've arranged 9 numbers on the panel. Don't peek! Ha ha ha!<br>Now give me your 10 medals. Some players run away when they realize that they don't stand a good chance of winning. Therefore, I prefer to hold the medals before the game starts. If you quit during game play, you'll forfeit your bet. Is that satisfactory?<br>Now, select your %choicenum% number.";
		protected static final String msg_0lines = "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?<br>Usually, I don't give a reward when you don't create a single line, but since I'm feeling sorry for you, I'll be generous this time. Wait here.<br>.<br>.<br>.<br><br><br>Here, take this. I hope it will bring you better luck in the future.";
		protected static final String msg_3lines = "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations! As I promised, I'll give you an unclaimed item from my warehouse. Wait here.<br>.<br>.<br>.<br><br><br>Puff puff... it's very dusty. Here it is. Do you like it?";
		private static final String template_choice = "<a action=\"bypass -h Quest _384_WarehouseKeepersPastime choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ";
		private final boolean _BigGame;
		private final QuestState _qs;
		
		public Bingo(boolean BigGame, QuestState qs)
		{
			super("<a action=\"bypass -h Quest _384_WarehouseKeepersPastime choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ");
			_BigGame = BigGame;
			_qs = qs;
		}
		
		@Override
		protected String getFinal()
		{
			String result = super.getFinal();
			if(lines == 3)
			{
				reward(_BigGame ? Rewards_Win_Big : Rewards_Win);
			}
			else if(lines == 0)
			{
				reward(_BigGame ? Rewards_Lose_Big : Rewards_Lose);
			}
			bingos.remove(_qs.getPlayer().getObjectId());
			return result;
		}
		
		private void reward(int[][] rew)
		{
			int r = Rnd.get(100);
			for(int[] l : rew)
			{
				if(r >= l[0])
					continue;
				_qs.giveItems(l[1], (long) l[2], true);
				return;
			}
		}
	}
}