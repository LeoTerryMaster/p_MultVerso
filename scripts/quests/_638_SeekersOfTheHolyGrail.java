package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _638_SeekersOfTheHolyGrail extends Quest implements ScriptFile
{
	private static final int DROP_CHANCE = 10;
	private static final int INNOCENTIN = 31328;
	private static final int TOTEM = 8068;
	private static final int EAS = 960;
	private static final int EWS = 959;
	
	public _638_SeekersOfTheHolyGrail()
	{
		super(true);
		addStartNpc(31328);
		addQuestItem(8068);
		int i = 22137;
		while(i <= 22176)
		{
			addKillId(i++);
		}
		addKillId(22194);
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
		if(event.equals("highpriest_innocentin_q0638_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("highpriest_innocentin_q0638_09.htm"))
		{
			st.playSound("ItemSound.quest_giveup");
			st.exitCurrentQuest(true);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int id = st.getState();
		String htmltext = id == 1 ? st.getPlayer().getLevel() >= 73 ? "highpriest_innocentin_q0638_01.htm" : "highpriest_innocentin_q0638_02.htm" : tryRevard(st);
		return htmltext;
	}
	
	private String tryRevard(QuestState st)
	{
		boolean ok = false;
		while(st.getQuestItemsCount(8068) >= 2000)
		{
			st.takeItems(8068, 2000);
			int rnd = Rnd.get(100);
			if(rnd < 50)
			{
				st.giveItems(57, 3576000, false);
			}
			else if(rnd < 85)
			{
				st.giveItems(960, 1, false);
			}
			else
			{
				st.giveItems(959, 1, false);
			}
			ok = true;
		}
		if(ok)
		{
			st.playSound("ItemSound.quest_middle");
			return "highpriest_innocentin_q0638_10.htm";
		}
		return "highpriest_innocentin_q0638_05.htm";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(8068, 1, 10.0 * npc.getTemplate().rateHp);
		if((npc.getNpcId() == 22146 || npc.getNpcId() == 22151) && Rnd.chance(10))
		{
			npc.dropItem(st.getPlayer(), 8275, 1);
		}
		if((npc.getNpcId() == 22140 || npc.getNpcId() == 22149) && Rnd.chance(10))
		{
			npc.dropItem(st.getPlayer(), 8273, 1);
		}
		if((npc.getNpcId() == 22142 || npc.getNpcId() == 22143) && Rnd.chance(10))
		{
			npc.dropItem(st.getPlayer(), 8274, 1);
		}
		return null;
	}
}