package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _621_EggDelivery extends Quest implements ScriptFile
{
	private static final int JEREMY = 31521;
	private static final int VALENTINE = 31584;
	private static final int PULIN = 31543;
	private static final int NAFF = 31544;
	private static final int CROCUS = 31545;
	private static final int KUBER = 31546;
	private static final int BEOLIN = 31547;
	private static final int BoiledEgg = 7206;
	private static final int FeeOfBoiledEgg = 7196;
	private static final int HastePotion = 734;
	private static final int RecipeSealedTateossianRing = 6849;
	private static final int RecipeSealedTateossianEarring = 6847;
	private static final int RecipeSealedTateossianNecklace = 6851;
	private static final int Tateossian_CHANCE = 20;
	
	public _621_EggDelivery()
	{
		super(false);
		addStartNpc(JEREMY);
		addTalkId(VALENTINE);
		addTalkId(PULIN);
		addTalkId(NAFF);
		addTalkId(CROCUS);
		addTalkId(KUBER);
		addTalkId(BEOLIN);
		addQuestItem(7206);
		addQuestItem(7196);
	}
	
	private static void takeEgg(QuestState st, int setcond)
	{
		st.setCond(Integer.valueOf(setcond).intValue());
		st.takeItems(7206, 1);
		st.giveItems(7196, 1);
		st.playSound("ItemSound.quest_middle");
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		int cond = st.getCond();
		long BoiledEgg_count = st.getQuestItemsCount(7206);
		if(event.equalsIgnoreCase("jeremy_q0621_0104.htm") && _state == 1)
		{
			st.takeItems(7206, -1);
			st.takeItems(7196, -1);
			st.setState(2);
			st.setCond(1);
			st.giveItems(7206, 5);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("pulin_q0621_0201.htm") && cond == 1 && BoiledEgg_count > 0)
		{
			takeEgg(st, 2);
		}
		else if(event.equalsIgnoreCase("naff_q0621_0301.htm") && cond == 2 && BoiledEgg_count > 0)
		{
			takeEgg(st, 3);
		}
		else if(event.equalsIgnoreCase("crocus_q0621_0401.htm") && cond == 3 && BoiledEgg_count > 0)
		{
			takeEgg(st, 4);
		}
		else if(event.equalsIgnoreCase("kuber_q0621_0501.htm") && cond == 4 && BoiledEgg_count > 0)
		{
			takeEgg(st, 5);
		}
		else if(event.equalsIgnoreCase("beolin_q0621_0601.htm") && cond == 5 && BoiledEgg_count > 0)
		{
			takeEgg(st, 6);
		}
		else if(event.equalsIgnoreCase("jeremy_q0621_0701.htm") && cond == 6 && st.getQuestItemsCount(7196) >= 5)
		{
			st.setCond(7);
		}
		else if(event.equalsIgnoreCase("brewer_valentine_q0621_0801.htm") && cond == 7 && st.getQuestItemsCount(7196) >= 5)
		{
			st.takeItems(7206, -1);
			st.takeItems(7196, -1);
			if(Rnd.chance(Tateossian_CHANCE))
			{
				if(Rnd.chance(40))
				{
					st.giveItems(6849, 1);
				}
				else if(Rnd.chance(40))
				{
					st.giveItems(6847, 1);
				}
				else
				{
					st.giveItems(6851, 1);
				}
			}
			else
			{
				st.giveItems(57, 18800);
				st.giveItems(734, 1, true);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		if(st.getState() == 1)
		{
			if(npcId != JEREMY)
			{
				return htmltext;
			}
			if(st.getPlayer().getLevel() >= 68)
			{
				st.setCond(0);
				return "jeremy_q0621_0101.htm";
			}
			st.exitCurrentQuest(true);
			return "jeremy_q0621_0103.htm";
		}
		int cond = st.getCond();
		long BoiledEgg_count = st.getQuestItemsCount(7206);
		long FeeOfBoiledEgg_count = st.getQuestItemsCount(7196);
		if(cond == 1 && npcId == PULIN && BoiledEgg_count > 0)
		{
			htmltext = "pulin_q0621_0101.htm";
		}
		if(cond == 2 && npcId == NAFF && BoiledEgg_count > 0)
		{
			htmltext = "naff_q0621_0201.htm";
		}
		if(cond == 3 && npcId == CROCUS && BoiledEgg_count > 0)
		{
			htmltext = "crocus_q0621_0301.htm";
		}
		if(cond == 4 && npcId == KUBER && BoiledEgg_count > 0)
		{
			htmltext = "kuber_q0621_0401.htm";
		}
		if(cond == 5 && npcId == BEOLIN && BoiledEgg_count > 0)
		{
			htmltext = "beolin_q0621_0501.htm";
		}
		if(cond == 6 && npcId == JEREMY && FeeOfBoiledEgg_count >= 5)
		{
			htmltext = "jeremy_q0621_0601.htm";
		}
		if(cond == 7 && npcId == JEREMY && FeeOfBoiledEgg_count >= 5)
		{
			htmltext = "jeremy_q0621_0703.htm";
		}
		if(cond == 7 && npcId == VALENTINE && FeeOfBoiledEgg_count >= 5)
		{
			htmltext = "brewer_valentine_q0621_0701.htm";
		}
		else if(cond > 0 && npcId == JEREMY && BoiledEgg_count > 0)
		{
			htmltext = "jeremy_q0621_0104.htm";
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