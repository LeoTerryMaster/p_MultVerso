package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _640_TheZeroHour extends Quest implements ScriptFile
{
	private static final int KAHMAN = 31554;
	private static final int FANG = 8085;
	private static final int Enria = 4042;
	private static final int Asofe = 4043;
	private static final int Thons = 4044;
	private static final int Varnish_of_Purity = 1887;
	private static final int Synthetic_Cokes = 1888;
	private static final int Compound_Braid = 1889;
	private static final int Durable_Metal_Plate = 5550;
	private static final int Mithril_Alloy = 1890;
	private static final int Oriharukon = 1893;
	private static final int DROP_CHANCE = 50;
	private static final int[] mobs = {22105, 22106, 22107, 22108, 22109, 22110, 22111, 22115, 22116, 22117, 22118, 22119, 22120, 22121, 22112, 22113, 22114};
	
	public _640_TheZeroHour()
	{
		super(true);
		addStartNpc(KAHMAN);
		addKillId(mobs);
		addQuestItem(FANG);
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
		int cond = st.getCond();
		if(event.equals("merc_kahmun_q0640_0103.htm") && cond == 0)
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		if(cond == 1)
		{
			if(event.equals("0"))
			{
				if(st.getQuestItemsCount(FANG) >= 12)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 12);
					st.giveItems(Enria, 1);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("1"))
			{
				if(st.getQuestItemsCount(FANG) >= 6)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 6);
					st.giveItems(Asofe, 1);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("2"))
			{
				if(st.getQuestItemsCount(FANG) >= 6)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 6);
					st.giveItems(Thons, 1);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("3"))
			{
				if(st.getQuestItemsCount(FANG) >= 81)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 81);
					st.giveItems(Varnish_of_Purity, 10);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("4"))
			{
				if(st.getQuestItemsCount(FANG) >= 33)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 33);
					st.giveItems(Synthetic_Cokes, 5);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("5"))
			{
				if(st.getQuestItemsCount(FANG) >= 30)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 30);
					st.giveItems(Compound_Braid, 10);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("6"))
			{
				if(st.getQuestItemsCount(FANG) >= 150)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 150);
					st.giveItems(Durable_Metal_Plate, 10);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("7"))
			{
				if(st.getQuestItemsCount(FANG) >= 131)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 131);
					st.giveItems(Mithril_Alloy, 10);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
			if(event.equals("8"))
			{
				if(st.getQuestItemsCount(FANG) >= 123)
				{
					htmltext = "merc_kahmun_q0640_0203.htm";
					st.takeItems(FANG, 123);
					st.giveItems(Oriharukon, 5);
				}
				else
				{
					htmltext = "merc_kahmun_q0640_0201.htm";
				}
			}
		}
		if(event.equals("close"))
		{
			htmltext = "merc_kahmun_q0640_0205.htm";
			st.takeItems(FANG, -1);
			st.exitCurrentQuest(true);
		}
		if(event.equals("more"))
		{
			htmltext = "merc_kahmun_q0640_0101.htm";
			st.unset("cond");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		QuestState InSearchOfTheNest = st.getPlayer().getQuestState(_109_InSearchOfTheNest.class);
		if(npcId == KAHMAN)
		{
			if(cond == 0)
			{
				htmltext = st.getPlayer().getLevel() >= 66 ? InSearchOfTheNest != null && InSearchOfTheNest.isCompleted() ? "merc_kahmun_q0640_0101.htm" : "merc_kahmun_q0640_0104.htm" : "merc_kahmun_q0640_0102.htm";
			}
			if(cond == 1)
			{
				htmltext = "merc_kahmun_q0640_0105.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() == 2)
		{
			st.rollAndGive(FANG, 1, (double) DROP_CHANCE);
		}
		return null;
	}
}