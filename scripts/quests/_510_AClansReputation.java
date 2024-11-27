package quests;

import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.ScriptFile;

public class _510_AClansReputation extends Quest implements ScriptFile
{
	private static final int VALDIS = 31331;
	private static final int CLAW = 8767;
	private static final int CLAN_POINTS_REWARD = 50;
	
	public _510_AClansReputation()
	{
		super(2);
		addStartNpc(31331);
		int npc = 22215;
		while(npc <= 22217)
		{
			addKillId(npc++);
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
		if(event.equals("31331-3.htm"))
		{
			if(cond == 0)
			{
				st.setCond(1);
				st.setState(2);
			}
		}
		else if(event.equals("31331-6.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		Player player = st.getPlayer();
		Clan clan = player.getClan();
		if(player.getClan() == null || !player.isClanLeader())
		{
			st.exitCurrentQuest(true);
			htmltext = "31331-0.htm";
		}
		else if(player.getClan().getLevel() < 5)
		{
			st.exitCurrentQuest(true);
			htmltext = "31331-0.htm";
		}
		else
		{
			int cond = st.getCond();
			int id = st.getState();
			if(id == 1 && cond == 0)
			{
				htmltext = "31331-1.htm";
			}
			else if(id == 2 && cond == 1)
			{
				long count = st.getQuestItemsCount(8767);
				if(count == 0)
				{
					htmltext = "31331-4.htm";
				}
				else if(count >= 1)
				{
					htmltext = "31331-7.htm";
					st.takeItems(8767, -1);
					int pointsCount = 50 * (int) count * (int) Config.RATE_CLAN_REP_SCORE;
					int increasedPoints = clan.incReputation(pointsCount, true, "_510_AClansReputation");
					player.sendPacket(new SystemMessage(1777).addNumber(increasedPoints));
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId;
		if(!st.getPlayer().isClanLeader())
		{
			st.exitCurrentQuest(true);
		}
		else if(st.getState() == 2 && (npcId = npc.getNpcId()) >= 22215 && npcId <= 22218)
		{
			st.giveItems(8767, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}