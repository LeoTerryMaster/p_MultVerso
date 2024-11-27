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

public class _504_CompetitionForTheBanditStronghold extends Quest implements ScriptFile
{
	private static final int MESSENGER = 35437;
	private static final int TARLK_BUGBEAR = 20570;
	private static final int TARLK_BUGBEAR_WARRIOR = 20571;
	private static final int TARLK_BUGBEAR_HIGH_WARRIOR = 20572;
	private static final int TARLK_BASILISK = 20573;
	private static final int ELDER_TARLK_BASILISK = 20574;
	private static final int AMULET = 4332;
	private static final int ALIANCE_TROPHEY = 5009;
	private static final int CONTEST_CERTIFICATE = 4333;
	
	public _504_CompetitionForTheBanditStronghold()
	{
		super(2);
		addStartNpc(35437);
		addTalkId(35437);
		addKillId(20570);
		addKillId(20571);
		addKillId(20572);
		addKillId(20573);
		addKillId(20574);
		addQuestItem(4333, 4332, 5009);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("azit_messenger_q0504_02.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.giveItems(4333, 1);
			st.playSound("ItemSound.quest_accept");
		}
		String htmlText = event;
		return htmlText;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		Player player = st.getPlayer();
		Clan clan = player.getClan();
		ClanHall clanhall = ResidenceHolder.getInstance().getResidence(35);
		if(clanhall.getSiegeEvent().isRegistrationOver())
		{
			htmltext = null;
			showHtmlFile(player, "azit_messenger_q0504_03.htm", false, "%siege_time%", TimeUtils.toSimpleFormat(clanhall.getSiegeDate()));
		}
		else if(clan == null || player.getObjectId() != clan.getLeaderId())
		{
			htmltext = "azit_messenger_q0504_05.htm";
		}
		else if(player.getObjectId() == clan.getLeaderId() && clan.getLevel() < 4)
		{
			htmltext = "azit_messenger_q0504_04.htm";
		}
		else if(clanhall.getSiegeEvent().getSiegeClan("attackers", player.getClan()) != null)
		{
			htmltext = "azit_messenger_q0504_06.htm";
		}
		else if(clan.getHasHideout() > 0)
		{
			htmltext = "azit_messenger_q0504_10.htm";
		}
		else if(cond == 0)
		{
			htmltext = "azit_messenger_q0504_01.htm";
		}
		else if(st.getQuestItemsCount(4333) == 1 && st.getQuestItemsCount(4332) < 30)
		{
			htmltext = "azit_messenger_q0504_07.htm";
		}
		else if(st.getQuestItemsCount(5009) >= 1)
		{
			htmltext = "azit_messenger_q0504_07a.htm";
		}
		else if(st.getQuestItemsCount(4333) == 1 && st.getQuestItemsCount(4332) == 30)
		{
			st.takeItems(4332, -1);
			st.takeItems(4333, -1);
			st.giveItems(5009, 1);
			st.playSound("ItemSound.quest_finish");
			st.setCond(-1);
			htmltext = "azit_messenger_q0504_08.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(4332) < 30)
		{
			st.giveItems(4332, 1);
			st.playSound("ItemSound.quest_itemget");
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