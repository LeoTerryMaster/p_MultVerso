package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _296_SilkOfTarantula extends Quest implements ScriptFile
{
	private static final int TARANTULA_SPIDER_SILK = 1493;
	private static final int TARANTULA_SPINNERETTE = 1494;
	private static final int RING_OF_RACCOON = 1508;
	private static final int RING_OF_FIREFLY = 1509;
	
	public _296_SilkOfTarantula()
	{
		super(false);
		addStartNpc(30519);
		addTalkId(30548);
		addKillId(20394);
		addKillId(20403);
		addKillId(20508);
		addQuestItem(1493);
		addQuestItem(1494);
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
		String htmltext = event;
		if(event.equalsIgnoreCase("trader_mion_q0296_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("quit"))
		{
			htmltext = "trader_mion_q0296_06.htm";
			st.takeItems(1494, -1);
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if(event.equalsIgnoreCase("exchange"))
		{
			if(st.getQuestItemsCount(1494) >= 1)
			{
				htmltext = "defender_nathan_q0296_03.htm";
				st.giveItems(1493, 17);
				st.takeItems(1494, -1);
			}
			else
			{
				htmltext = "defender_nathan_q0296_02.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30519)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 15)
				{
					if(st.getQuestItemsCount(1508) > 0)
						return "trader_mion_q0296_02.htm";
					if(st.getQuestItemsCount(1509) <= 0)
						return "trader_mion_q0296_08.htm";
					return "trader_mion_q0296_02.htm";
				}
				htmltext = "trader_mion_q0296_01.htm";
				st.exitCurrentQuest(true);
				return htmltext;
			}
			else
			{
				if(cond != 1)
					return htmltext;
				if(st.getQuestItemsCount(1493) < 1)
				{
					return "trader_mion_q0296_04.htm";
				}
				if(st.getQuestItemsCount(1493) < 1)
					return htmltext;
				htmltext = "trader_mion_q0296_05.htm";
				st.giveItems(57, st.getQuestItemsCount(1493) * 23);
				st.takeItems(1493, -1);
				if(st.getPlayer().getClassId().getLevel() != 1)
					return htmltext;
				if(st.getPlayer().getVarB("p1q4"))
					return htmltext;
				st.getPlayer().setVar("p1q4", "1", -1);
				st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
			}
			return htmltext;
		}
		else
		{
			if(npcId != 30548)
				return htmltext;
			if(cond != 1)
				return htmltext;
			return "defender_nathan_q0296_01.htm";
		}
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			if(Rnd.chance(50))
			{
				st.rollAndGive(1494, 1, 45.0);
			}
			else
			{
				st.rollAndGive(1493, 1, 45.0);
			}
		}
		return null;
	}
}