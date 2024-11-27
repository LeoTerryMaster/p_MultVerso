package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _326_VanquishRemnants extends Quest implements ScriptFile
{
	private static final int Leopold = 30435;
	private static final int RedCrossBadge = 1359;
	private static final int BlueCrossBadge = 1360;
	private static final int BlackCrossBadge = 1361;
	private static final int BlackLionMark = 1369;
	private static final int OlMahumPatrol = 30425;
	private static final int OlMahumGuard = 20058;
	private static final int OlMahumStraggler = 20061;
	private static final int OlMahumShooter = 20063;
	private static final int OlMahumCaptain = 20066;
	private static final int OlMahumCommander = 20076;
	private static final int OlMahumSupplier = 20436;
	private static final int OlMahumRecruit = 20437;
	private static final int OlMahumGeneral = 20438;
	public final int[][] DROPLIST_COND = {{30425, 1359}, {20058, 1359}, {20437, 1359}, {20061, 1360}, {20063, 1360}, {20436, 1360}, {20066, 1361}, {20438, 1361}, {20076, 1361}};
	
	public _326_VanquishRemnants()
	{
		super(false);
		addStartNpc(30435);
		addTalkId(30435);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][0]);
		}
		addQuestItem(1359);
		addQuestItem(1360);
		addQuestItem(1361);
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
		if(event.equalsIgnoreCase("leopold_q0326_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("leopold_q0326_03.htm"))
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
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30435)
		{
			if(st.getPlayer().getLevel() < 21)
			{
				htmltext = "leopold_q0326_01.htm";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				htmltext = "leopold_q0326_02.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(1359) == 0 && st.getQuestItemsCount(1360) == 0 && st.getQuestItemsCount(1361) == 0)
			{
				htmltext = "leopold_q0326_04.htm";
			}
			else if(cond == 1)
			{
				if(st.getQuestItemsCount(1359) + st.getQuestItemsCount(1360) + st.getQuestItemsCount(1361) >= 100)
				{
					if(st.getQuestItemsCount(1369) == 0)
					{
						htmltext = "leopold_q0326_09.htm";
						st.giveItems(1369, 1);
					}
					else
					{
						htmltext = "leopold_q0326_06.htm";
					}
				}
				else
				{
					htmltext = "leopold_q0326_05.htm";
				}
				st.giveItems(57, st.getQuestItemsCount(1359) * 46 + st.getQuestItemsCount(1360) * 52 + st.getQuestItemsCount(1361) * 58, true);
				st.takeItems(1359, -1);
				st.takeItems(1360, -1);
				st.takeItems(1361, -1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() == 2)
		{
			for(int i = 0;i < DROPLIST_COND.length;++i)
			{
				if(npc.getNpcId() != DROPLIST_COND[i][0])
					continue;
				st.giveItems(DROPLIST_COND[i][1], 1);
			}
		}
		return null;
	}
}