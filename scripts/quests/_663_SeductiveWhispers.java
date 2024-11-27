package quests;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.base.Experience;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

import java.util.HashMap;
import java.util.Map;

public class _663_SeductiveWhispers extends Quest implements ScriptFile
{
	private static final int Wilbert = 30846;
	private static final int[] mobs = {20674, 20678, 20954, 20955, 20956, 20957, 20958, 20959, 20960, 20961, 20962, 20974, 20975, 20976, 20996, 20997, 20998, 20999, 21001, 21002, 21006, 21007, 21008, 21009, 21010};
	private static final int Spirit_Bead = 8766;
	private static final int Enchant_Weapon_D = 955;
	private static final int Enchant_Weapon_C = 951;
	private static final int Enchant_Weapon_B = 947;
	private static final int Enchant_Armor_B = 948;
	private static final int Enchant_Weapon_A = 729;
	private static final int Enchant_Armor_A = 730;
	private static final int[] Recipes_Weapon_B = {4963, 4966, 4967, 4968, 5001, 5003, 5004, 5005, 5006, 5007};
	private static final int[] Ingredients_Weapon_B = {4101, 4107, 4108, 4109, 4115, 4117, 4118, 4119, 4120, 4121};
	private static final int drop_chance = 15;
	private static final int WinChance = 68;
	private static final LevelRewards[] rewards = {new LevelRewards("%n% adena").add(57, 40000), new LevelRewards("%n% adena").add(57, 80000), new LevelRewards("%n% adena, %n% D-grade Enchant Weapon Scroll(s)").add(57, 110000).add(955, 1), new LevelRewards("%n% adena, %n% C-grade Enchant Weapon Scroll(s)").add(57, 199000).add(951, 1), new LevelRewards("%n% adena, %n% recipe(s) for a B-grade Weapon").add(57, 388000).add(Recipes_Weapon_B, 1), new LevelRewards("%n% adena, %n% essential ingredient(s) for a B-grade Weapon").add(57, 675000).add(Ingredients_Weapon_B, 1), new LevelRewards("%n% adena, %n% B-grade Enchant Weapon Scroll(s), %n% B-grade Enchat Armor Scroll(s)").add(57, 1284000).add(947, 2).add(948, 2), new LevelRewards("%n% adena, %n% A-grade Enchant Weapon Scroll(s), %n% A-grade Enchat Armor Scroll(s)").add(57, 2384000).add(729, 1).add(730, 2)};
	private static String Dialog_WinLevel = "<font color=\"LEVEL\">Blacksmith Wilbert:</font><br><br>";
	private static String Dialog_WinGame = "<font color=\"LEVEL\">Blacksmith Wilbert:</font><br><br>";
	private static String Dialog_Rewards = "<font color=\"LEVEL\">Blacksmith Wilbert:</font><br><br>";
	
	static
	{
		Dialog_WinLevel = Dialog_WinLevel + "You won round %level%!<br>";
		Dialog_WinLevel = Dialog_WinLevel + "You can stop game now and take your prize:<br>";
		Dialog_WinLevel = Dialog_WinLevel + "<font color=\"LEVEL\">%prize%</font><br><br>";
		Dialog_WinLevel = Dialog_WinLevel + "<a action=\"bypass -h Quest _663_SeductiveWhispers 30846_12.htm\">Pull next card!</a><br>";
		Dialog_WinLevel = Dialog_WinLevel + "<a action=\"bypass -h Quest _663_SeductiveWhispers 30846_13.htm\">\"No, enough for me, end game and take my prize.\"</a>";
		Dialog_WinGame = Dialog_WinGame + "Congratulations! You won round %n%!<br>";
		Dialog_WinGame = Dialog_WinGame + "Game ends now and you get your prize:<br>";
		Dialog_WinGame = Dialog_WinGame + "<font color=\"LEVEL\">%prize%</font><br><br>";
		Dialog_WinGame = Dialog_WinGame + "<a action=\"bypass -h Quest _663_SeductiveWhispers 30846_03.htm\">Return</a>";
		Dialog_Rewards = Dialog_Rewards + "If you win the game, the master running it owes you the appropriate amount. The higher the round, the bigger the payout. That's why the game anly allows you to win up to 8 round in a row. If -- and that's a big if -- you manage to win 8 straight times, the game will end.<br>";
		Dialog_Rewards = Dialog_Rewards + "Keep in mind that <font color=\"LEVEL\">if you lose any of the rounds, you get nothing</font>. That's fair warning, my friend. Here's how the prize system works:<br>";
		for(int i = 0;i < rewards.length;++i)
		{
			Dialog_Rewards = Dialog_Rewards + "<font color=\"LEVEL\">" + (i + 1) + " winning round";
			if(i > 0)
			{
				Dialog_Rewards = Dialog_Rewards + "s";
			}
			Dialog_Rewards = Dialog_Rewards + ": </font>" + rewards[i] + "<br>";
		}
		Dialog_Rewards = Dialog_Rewards + "<br>My advice is to identify what you'd like to win and then to play for that prize. Any questions?<br>";
		Dialog_Rewards = Dialog_Rewards + "<a action=\"bypass -h Quest _663_SeductiveWhispers 30846_03.htm\">Return</a>";
	}
	
	public _663_SeductiveWhispers()
	{
		super(false);
		addStartNpc(30846);
		addKillId(mobs);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		long Spirit_Bead_Count = st.getQuestItemsCount(8766);
		if(event.equalsIgnoreCase("30846_04.htm") && _state == 1)
		{
			st.setCond(1);
			st.set("round", "0");
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else
		{
			if(event.equalsIgnoreCase("30846_07.htm") && _state == 2)
			{
				return Dialog_Rewards;
			}
			if(event.equalsIgnoreCase("30846_09.htm") && _state == 2)
			{
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else if(event.equalsIgnoreCase("30846_08.htm") && _state == 2)
			{
				if(Spirit_Bead_Count < 1)
				{
					return "30846_11.htm";
				}
				st.takeItems(8766, 1);
				if(!Rnd.chance(68))
				{
					return "30846_08a.htm";
				}
			}
			else if(event.equalsIgnoreCase("30846_10.htm") && _state == 2)
			{
				st.set("round", "0");
				if(Spirit_Bead_Count < 50)
				{
					return "30846_11.htm";
				}
			}
			else
			{
				if(event.equalsIgnoreCase("30846_12.htm") && _state == 2)
				{
					int round = st.getInt("round");
					if(round == 0)
					{
						if(Spirit_Bead_Count < 50)
						{
							return "30846_11.htm";
						}
						st.takeItems(8766, 50);
					}
					if(!Rnd.chance(68))
					{
						st.set("round", "0");
						return event;
					}
					LevelRewards current_reward = rewards[round];
					int next_round = round + 1;
					boolean LastLevel = next_round == rewards.length;
					String dialog = LastLevel ? Dialog_WinGame : Dialog_WinLevel;
					dialog = dialog.replaceFirst("%level%", String.valueOf(next_round));
					dialog = dialog.replaceFirst("%prize%", current_reward.toString());
					if(LastLevel)
					{
						next_round = 0;
						current_reward.giveRewards(st);
						st.playSound("ItemSound.quest_jackpot");
					}
					st.set("round", String.valueOf(next_round));
					return dialog;
				}
				if(event.equalsIgnoreCase("30846_13.htm") && _state == 2)
				{
					int round = st.getInt("round") - 1;
					st.set("round", "0");
					if(round < 0 || round >= rewards.length)
					{
						return "30846_13a.htm";
					}
					rewards[round].giveRewards(st);
				}
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(npc.getNpcId() != 30846)
		{
			return "noquest";
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getLevel() < 50)
			{
				st.exitCurrentQuest(true);
				return "30846_00.htm";
			}
			st.setCond(0);
			return "30846_01.htm";
		}
		return "30846_03.htm";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() == 2)
		{
			double rand = 15.0 * Experience.penaltyModifier((long) qs.calculateLevelDiffForDrop(npc.getLevel(), qs.getPlayer().getLevel()), 9.0) * npc.getTemplate().rateHp;
			qs.rollAndGive(8766, 1, rand);
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
	
	private static class LevelRewards
	{
		private final Map<int[], Integer> rewards = new HashMap<>();
		private String txt;
		
		public LevelRewards(String _txt)
		{
			txt = _txt;
		}
		
		public LevelRewards add(int item_id, int count)
		{
			return add(new int[] {item_id}, count);
		}
		
		public LevelRewards add(int[] items_id, int count)
		{
			int cnt = (int) ((double) count * Config.RATE_QUESTS_REWARD);
			txt = txt.replaceFirst("%n%", String.valueOf(cnt));
			rewards.put(items_id, cnt);
			return this;
		}
		
		public void giveRewards(QuestState qs)
		{
			for(int[] item_ids : rewards.keySet())
			{
				qs.giveItems(item_ids[Rnd.get(item_ids.length)], (long) rewards.get(item_ids).intValue(), false);
			}
		}
		
		@Override
		public String toString()
		{
			return txt;
		}
	}
}