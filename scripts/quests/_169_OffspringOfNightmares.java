package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _169_OffspringOfNightmares extends Quest implements ScriptFile
{
	private static final int Vlasty = 30145;
	private static final int CrackedSkull = 1030;
	private static final int PerfectSkull = 1031;
	private static final int BoneGaiters = 31;
	private static final int DarkHorror = 20105;
	private static final int LesserDarkHorror = 20025;
	
	public _169_OffspringOfNightmares()
	{
		super(false);
		addStartNpc(30145);
		addTalkId(30145);
		addKillId(20105);
		addKillId(20025);
		addQuestItem(1030, 1031);
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
		if(event.equalsIgnoreCase("30145-04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30145-08.htm"))
		{
			st.takeItems(1030, -1);
			st.takeItems(1031, -1);
			st.giveItems(31, 1);
			st.giveItems(57, 17050, true);
			st.getPlayer().addExpAndSp(17475, 818);
			if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4"))
			{
				st.getPlayer().setVar("p1q4", "1", -1);
				st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30145)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.darkelf)
				{
					htmltext = "30145-00.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() >= 15)
				{
					htmltext = "30145-03.htm";
				}
				else
				{
					htmltext = "30145-02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = st.getQuestItemsCount(1030) == 0 ? "30145-05.htm" : "30145-06.htm";
			}
			else if(cond == 2)
			{
				htmltext = "30145-07.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(Rnd.chance(20) && st.getQuestItemsCount(1031) == 0)
			{
				st.giveItems(1031, 1);
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
				st.setState(2);
			}
			if(Rnd.chance(70))
			{
				st.giveItems(1030, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}