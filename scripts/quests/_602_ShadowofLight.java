package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _602_ShadowofLight extends Quest implements ScriptFile
{
	private static final int eye_of_argos = 31683;
	private static final int buffalo_slave = 21299;
	private static final int grendel_slave = 21304;
	private static final int sealed_sanddragons_earing_piece = 6698;
	private static final int sealed_ring_of_aurakyria_gem = 6699;
	private static final int sealed_dragon_necklace_wire = 6700;
	private static final int q_dark_eye = 7189;
	
	public _602_ShadowofLight()
	{
		super(false);
		addStartNpc(31683);
		addKillId(21299, 21304);
		addQuestItem(7189);
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
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("shadow_of_light", String.valueOf(11), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "eye_of_argos_q0602_0104.htm";
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			if(st.getQuestItemsCount(7189) >= 100)
			{
				int i1 = Rnd.get(1000);
				st.takeItems(7189, 100);
				if(i1 < 200)
				{
					st.giveItems(6699, 3);
					st.giveItems(57, 40000);
					st.addExpAndSp(120000, 20000);
				}
				else if(i1 < 400)
				{
					st.giveItems(6698, 3);
					st.giveItems(57, 60000);
					st.addExpAndSp(110000, 15000);
				}
				else if(i1 < 500)
				{
					st.giveItems(6700, 3);
					st.giveItems(57, 40000);
					st.addExpAndSp(150000, 10000);
				}
				else if(i1 < 1000)
				{
					st.giveItems(57, 100000);
					st.addExpAndSp(140000, 11250);
				}
				st.unset("shadow_of_light");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "eye_of_argos_q0602_0201.htm";
			}
			else
			{
				htmltext = "eye_of_argos_q0602_0202.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("shadow_of_light");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31683)
					break;
				if(st.getPlayer().getLevel() >= 68)
				{
					htmltext = "eye_of_argos_q0602_0101.htm";
					break;
				}
				htmltext = "eye_of_argos_q0602_0103.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId != 31683 || GetMemoState < 11 || GetMemoState > 12)
					break;
				if(GetMemoState == 12 && st.getQuestItemsCount(7189) >= 100)
				{
					htmltext = "eye_of_argos_q0602_0105.htm";
					break;
				}
				htmltext = "eye_of_argos_q0602_0106.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("shadow_of_light");
		int npcId = npc.getNpcId();
		if(GetMemoState == 11)
		{
			if(npcId == 21299)
			{
				int i42 = Rnd.get(1000);
				if(i42 < 560)
				{
					if(st.getQuestItemsCount(7189) + 1 >= 100)
					{
						st.setCond(2);
						st.set("shadow_of_light", String.valueOf(12), true);
						st.giveItems(7189, 100 - st.getQuestItemsCount(7189));
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.giveItems(7189, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else if(npcId == 21304 && Rnd.get(1000) < 800)
			{
				if(st.getQuestItemsCount(7189) + 1 >= 100)
				{
					st.setCond(2);
					st.set("shadow_of_light", String.valueOf(12), true);
					st.giveItems(7189, 100 - st.getQuestItemsCount(7189));
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(7189, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}