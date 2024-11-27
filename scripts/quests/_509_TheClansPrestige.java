package quests;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Util;

public class _509_TheClansPrestige extends Quest implements ScriptFile
{
	private static final int GRAND_MAGISTER_VALDIS = 31331;
	private static final int DAIMONS_EYES = 8489;
	private static final int HESTIAS_FAIRY_STONE = 8490;
	private static final int NUCLEUS_OF_LESSER_GOLEM = 8491;
	private static final int FALSTONS_FANG = 8492;
	private static final int SHADIS_TANOL = 8493;
	private static final int DAIMON_THE_WHITE_EYED = 25290;
	private static final int HESTIA_GUARDIAN_DEITY = 25293;
	private static final int PLAGUE_GOLEM = 25523;
	private static final int DEMONS_AGENT_FALSTON = 25322;
	private static final int QUEEN_SHYEED = 25514;
	private static final int[][] REWARDS_LIST = {{0, 0}, {25290, 8489, 200}, {25293, 8490, 438}, {25523, 8491, 400}, {25322, 8492, 250}, {25514, 8493, 150}};
	private static final int[][] RADAR = {{0, 0, 0}, {186304, -43744, -3193}, {134672, -115600, -1216}, {168641, -60417, -3888}, {93296, -75104, -1824}, {79130, -55930, -6144}};
	
	public _509_TheClansPrestige()
	{
		super(2);
		addStartNpc(31331);
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
		if(event.equalsIgnoreCase("31331-0.htm") && cond == 0)
		{
			st.setCond(1);
			st.setState(2);
		}
		else if(Util.isNumber(event))
		{
			st.set("raid", event);
			htmltext = "31331-" + event + ".htm";
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
		else if(event.equalsIgnoreCase("31331-6.htm"))
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
			htmltext = "31331-0a.htm";
		}
		else if(clan.getLeader().getPlayer() != st.getPlayer())
		{
			st.exitCurrentQuest(true);
			htmltext = "31331-0a.htm";
		}
		else if(clan.getLevel() < 6)
		{
			st.exitCurrentQuest(true);
			htmltext = "31331-0b.htm";
		}
		else
		{
			int cond = st.getCond();
			int raid = st.getInt("raid");
			int id = st.getState();
			if(id == 1 && cond == 0)
			{
				htmltext = "31331-0c.htm";
			}
			else if(id == 2 && cond == 1)
			{
				int item = REWARDS_LIST[raid][1];
				long count = st.getQuestItemsCount(item);
				if(count == 0)
				{
					htmltext = "31331-" + raid + "a.htm";
				}
				else if(count == 1)
				{
					htmltext = "31331-" + raid + "b.htm";
					int increasedPoints = clan.incReputation(REWARDS_LIST[raid][2], true, "_509_TheClansPrestige");
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
		Clan clan = st.getPlayer().getClan();
		if(clan == null)
		{
			return null;
		}
		Player clan_leader = clan.getLeader().getPlayer();
		if(clan_leader == null)
		{
			return null;
		}
		QuestState id = null;
		if(clan_leader.equals(st.getPlayer()) || clan_leader.getDistance(npc) <= 1600.0)
		{
			id = clan_leader.getQuestState(getName());
		}
		if(id == null)
		{
			return null;
		}
		if(st.getCond() == 1 && st.getState() == 2)
		{
			int raid = REWARDS_LIST[st.getInt("raid")][0];
			int item = REWARDS_LIST[st.getInt("raid")][1];
			int npcId = npc.getNpcId();
			if(npcId == raid && st.getQuestItemsCount(item) == 0)
			{
				st.giveItems(item, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
}