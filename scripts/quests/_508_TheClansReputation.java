package quests;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Util;

public class _508_TheClansReputation extends Quest implements ScriptFile
{
	private static final int SIR_ERIC_RODEMAI = 30868;
	private static final int NUCLEUS_OF_FLAMESTONE_GIANT = 8494;
	private static final int THEMIS_SCALE = 8277;
	private static final int NUCLEUS_OF_HEKATON_PRIME = 8279;
	private static final int TIPHON_SHARD = 8280;
	private static final int GLAKIS_NECLEUS = 8281;
	private static final int RAHHAS_FANG = 8282;
	private static final int FLAMESTONE_GIANT = 25524;
	private static final int PALIBATI_QUEEN_THEMIS = 25252;
	private static final int HEKATON_PRIME = 25140;
	private static final int GARGOYLE_LORD_TIPHON = 25255;
	private static final int LAST_LESSER_GIANT_GLAKI = 25245;
	private static final int RAHHA = 25051;
	private static final int[][] REWARDS_LIST = {{0, 0}, {25252, 8277, 85}, {25140, 8279, 65}, {25255, 8280, 50}, {25245, 8281, 125}, {25051, 8282, 71}, {25524, 8494, 80}};
	private static final int[][] RADAR = {{0, 0, 0}, {192346, 21528, -3648}, {191979, 54902, -7658}, {170038, -26236, -3824}, {171762, 55028, -5992}, {117232, -9476, -3320}, {144218, -5816, -4722}};
	
	public _508_TheClansReputation()
	{
		super(2);
		addStartNpc(30868);
		for(int[] i : REWARDS_LIST)
		{
			if(i[0] > 0)
			{
				addKillId(i[0]);
			}
			if(i[1] <= 0)
				continue;
			addQuestItem(i[1]);
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
		int cond = st.getCond();
		String htmltext = event;
		if(event.equalsIgnoreCase("30868-0.htm") && cond == 0)
		{
			st.setCond(1);
			st.setState(2);
		}
		else if(Util.isNumber(event))
		{
			st.set("raid", event);
			htmltext = "30868-" + event + ".htm";
			int evt = Integer.parseInt(event);
			int x = RADAR[evt][0];
			int y = RADAR[evt][1];
			int z = RADAR[evt][2];
			if(x + y + z > 0)
			{
				st.addRadar(x, y, z);
			}
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30868-7.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		Clan clan = st.getPlayer().getClan();
		if(clan == null)
		{
			st.exitCurrentQuest(true);
			htmltext = "30868-0a.htm";
		}
		else if(clan.getLeader().getPlayer() != st.getPlayer())
		{
			st.exitCurrentQuest(true);
			htmltext = "30868-0a.htm";
		}
		else if(clan.getLevel() < 5)
		{
			st.exitCurrentQuest(true);
			htmltext = "30868-0b.htm";
		}
		else
		{
			int cond = st.getCond();
			int raid = st.getInt("raid");
			int id = st.getState();
			if(id == 1 && cond == 0)
			{
				htmltext = "30868-0c.htm";
			}
			else if(id == 2 && cond == 1)
			{
				int item = REWARDS_LIST[raid][1];
				long count = st.getQuestItemsCount(item);
				if(count == 0)
				{
					htmltext = "30868-" + raid + "a.htm";
				}
				else if(count == 1)
				{
					htmltext = "30868-" + raid + "b.htm";
					int increasedPoints = clan.incReputation(REWARDS_LIST[raid][2], true, "_508_TheClansReputation");
					st.getPlayer().sendPacket(new SystemMessage(1777).addNumber(increasedPoints));
					st.takeItems(item, 1);
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		Player clan_leader;
		try
		{
			clan_leader = st.getPlayer().getClan().getLeader().getPlayer();
		}
		catch(Exception E)
		{
			return null;
		}
		if(clan_leader == null)
		{
			return null;
		}
		if(!st.getPlayer().equals(clan_leader) && clan_leader.getDistance(npc) > (double) Config.ALT_PARTY_DISTRIBUTION_RANGE)
		{
			return null;
		}
		QuestState qs = clan_leader.getQuestState(getName());
		if(qs == null || !qs.isStarted() || qs.getCond() != 1)
		{
			return null;
		}
		int raid = REWARDS_LIST[st.getInt("raid")][0];
		int item = REWARDS_LIST[st.getInt("raid")][1];
		if(npc.getNpcId() == raid && st.getQuestItemsCount(item) == 0)
		{
			st.giveItems(item, 1);
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}
}