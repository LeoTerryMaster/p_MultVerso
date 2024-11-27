package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _266_PleaOfPixies extends Quest implements ScriptFile
{
	private static final int PREDATORS_FANG = 1334;
	private static final int EMERALD = 1337;
	private static final int BLUE_ONYX = 1338;
	private static final int ONYX = 1339;
	private static final int GLASS_SHARD = 1336;
	private static final int REC_LEATHER_BOOT = 2176;
	private static final int REC_SPIRITSHOT = 3032;
	
	public _266_PleaOfPixies()
	{
		super(false);
		addStartNpc(31852);
		addKillId(20525, 20530, 20534, 20537);
		addQuestItem(1334);
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
		if(event.equalsIgnoreCase("pixy_murika_q0266_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getRace() != Race.elf)
			{
				htmltext = "pixy_murika_q0266_00.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() < 3)
			{
				htmltext = "pixy_murika_q0266_01.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "pixy_murika_q0266_02.htm";
			}
		}
		else if(st.getQuestItemsCount(1334) < 100)
		{
			htmltext = "pixy_murika_q0266_04.htm";
		}
		else
		{
			st.takeItems(1334, -1);
			int n = Rnd.get(100);
			if(n < 2)
			{
				st.giveItems(1337, 1);
				st.giveItems(3032, 1);
				st.playSound("ItemSound.quest_jackpot");
			}
			else if(n < 20)
			{
				st.giveItems(1338, 1);
				st.giveItems(2176, 1);
			}
			else if(n < 45)
			{
				st.giveItems(1339, 1);
			}
			else
			{
				st.giveItems(1336, 1);
			}
			htmltext = "pixy_murika_q0266_05.htm";
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.rollAndGive(1334, 1, 1, 100, (double) (60 + npc.getLevel() * 5));
		}
		return null;
	}
}