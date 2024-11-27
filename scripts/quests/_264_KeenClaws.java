package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _264_KeenClaws extends Quest implements ScriptFile
{
	private static final int Payne = 30136;
	private static final int WolfClaw = 1367;
	private static final int LeatherSandals = 36;
	private static final int WoodenHelmet = 43;
	private static final int Stockings = 462;
	private static final int HealingPotion = 1061;
	private static final int ShortGloves = 48;
	private static final int ClothShoes = 35;
	private static final int Goblin = 20003;
	private static final int AshenWolf = 20456;
	private static final int[][] DROPLIST_COND = {{1, 2, 20003, 0, 1367, 50, 50, 2}, {1, 2, 20456, 0, 1367, 50, 50, 2}};
	
	public _264_KeenClaws()
	{
		super(false);
		addStartNpc(30136);
		addKillId(20003);
		addKillId(20456);
		addQuestItem(1367);
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
		if(event.equalsIgnoreCase("paint_q0264_03.htm"))
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
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId != 30136)
			return htmltext;
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 3)
			{
				return "paint_q0264_02.htm";
			}
			st.exitCurrentQuest(true);
			return "paint_q0264_01.htm";
		}
		if(cond == 1)
		{
			return "paint_q0264_04.htm";
		}
		if(cond != 2)
			return htmltext;
		st.takeItems(1367, -1);
		int n = Rnd.get(17);
		if(n == 0)
		{
			st.giveItems(43, 1);
			st.playSound("ItemSound.quest_jackpot");
		}
		else if(n < 2)
		{
			st.giveItems(57, 1000);
		}
		else if(n < 5)
		{
			st.giveItems(36, 1);
		}
		else if(n < 8)
		{
			st.giveItems(462, 1);
			st.giveItems(57, 50);
		}
		else if(n < 11)
		{
			st.giveItems(1061, 1);
		}
		else if(n < 14)
		{
			st.giveItems(48, 1);
		}
		else
		{
			st.giveItems(35, 1);
		}
		htmltext = "paint_q0264_05.htm";
		st.playSound("ItemSound.quest_finish");
		st.exitCurrentQuest(true);
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			if(cond != DROPLIST_COND[i][0] || npcId != DROPLIST_COND[i][2] || DROPLIST_COND[i][3] != 0 && st.getQuestItemsCount(DROPLIST_COND[i][3]) <= 0)
				continue;
			if(DROPLIST_COND[i][5] == 0)
			{
				st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], (double) DROPLIST_COND[i][6]);
				continue;
			}
			if(!st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], (double) DROPLIST_COND[i][6]) || DROPLIST_COND[i][1] == cond || DROPLIST_COND[i][1] == 0)
				continue;
			st.setCond(Integer.valueOf(DROPLIST_COND[i][1]).intValue());
			st.setState(2);
		}
		return null;
	}
}