package quests;

import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.ClanHall;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.TimeUtils;

public class _655_AGrandPlanForTamingWildBeasts extends Quest implements ScriptFile
{
	private static final int MESSENGER = 35627;
	private static final int STONE = 8084;
	private static final int TRAINER_LICENSE = 8293;
	
	public _655_AGrandPlanForTamingWildBeasts()
	{
		super(0);
		addStartNpc(35627);
		addTalkId(35627);
		addQuestItem(8084, 8293);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("farm_messenger_q0655_06.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmlText = event;
		return htmlText;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmlText = "noquest";
		int cond = st.getCond();
		Player player = st.getPlayer();
		Clan clan = player.getClan();
		ClanHall clanhall = ResidenceHolder.getInstance().getResidence(63);
		if(clanhall.getSiegeEvent().isRegistrationOver())
		{
			htmlText = null;
			showHtmlFile(player, "farm_messenger_q0655_02.htm", false, "%siege_time%", TimeUtils.toSimpleFormat(clanhall.getSiegeDate()));
		}
		else if(clan == null || player.getObjectId() != clan.getLeaderId())
		{
			htmlText = "farm_messenger_q0655_03.htm";
		}
		else if(player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4)
		{
			htmlText = "farm_messenger_q0655_05.htm";
		}
		else if(clanhall.getSiegeEvent().getSiegeClan("attackers", player.getClan()) != null)
		{
			htmlText = "farm_messenger_q0655_07.htm";
		}
		else if(clan.getHasHideout() > 0)
		{
			htmlText = "farm_messenger_q0655_04.htm";
		}
		else if(cond == 0)
		{
			htmlText = "farm_messenger_q0655_01.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(8084) < 10)
		{
			htmlText = "farm_messenger_q0655_08.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(8084) == 10)
		{
			st.setCond(-1);
			st.takeItems(8084, -1);
			st.giveItems(8293, 1);
			htmlText = "farm_messenger_q0655_10.htm";
		}
		else if(st.getQuestItemsCount(8293) == 1)
		{
			htmlText = "farm_messenger_q0655_09.htm";
		}
		return htmlText;
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