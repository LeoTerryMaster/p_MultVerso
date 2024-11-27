package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _038_DragonFangs extends Quest implements ScriptFile
{
	private static final int magister_rohmer = 30344;
	private static final int guard_luis = 30386;
	private static final int iris = 30034;
	private static final int langk_lizardman_sub_ldr = 20357;
	private static final int langk_lizardman_sentinel = 21100;
	private static final int langk_lizardman_leader = 20356;
	private static final int langk_lizardman_shaman = 21101;
	private static final int q_liz_feather = 7173;
	private static final int q_liz_totem_tooth1 = 7174;
	private static final int q_liz_totem_tooth2 = 7175;
	private static final int q_liz_letter1 = 7176;
	private static final int q_liz_letter2 = 7177;
	private static final int bone_helmet = 45;
	private static final int aspis = 627;
	private static final int leather_gauntlet = 605;
	private static final int blue_buckskin_boots = 1123;
	
	public _038_DragonFangs()
	{
		super(false);
		addStartNpc(30386);
		addTalkId(30034, 30344);
		addKillId(20357, 21100, 20356, 21101);
		addQuestItem(7173, 7174, 7175, 7176, 7177);
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
		int GetHTMLCookie = st.getInt("tooth_of_dragon_cookie");
		int npcId = npc.getNpcId();
		if(npcId == 30386)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("tooth_of_dragon", String.valueOf(11), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "guard_luis_q0038_0104.htm";
			}
			else if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 1)
			{
				if(st.getQuestItemsCount(7173) >= 100)
				{
					st.setCond(3);
					st.set("tooth_of_dragon", String.valueOf(21), true);
					st.takeItems(7173, 100);
					st.giveItems(7174, 1);
					htmltext = "guard_luis_q0038_0201.htm";
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					htmltext = "guard_luis_q0038_0202.htm";
				}
			}
		}
		else if(npcId == 30034)
		{
			if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 2)
			{
				if(st.getQuestItemsCount(7174) >= 1)
				{
					st.setCond(4);
					st.set("tooth_of_dragon", String.valueOf(31), true);
					st.takeItems(7174, 1);
					st.giveItems(7176, 1);
					htmltext = "iris_q0038_0301.htm";
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					htmltext = "iris_q0038_0302.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 4)
			{
				if(st.getQuestItemsCount(7177) >= 1)
				{
					st.setCond(6);
					st.set("tooth_of_dragon", String.valueOf(51), true);
					st.takeItems(7177, 1);
					htmltext = "iris_q0038_0501.htm";
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					htmltext = "iris_q0038_0502.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_3") && GetHTMLCookie == 5)
			{
				if(st.getQuestItemsCount(7175) >= 50)
				{
					int i1 = Rnd.get(1000);
					st.takeItems(7175, -1);
					st.addExpAndSp(435117, 23977);
					if(i1 < 250)
					{
						st.giveItems(45, 1);
						st.giveItems(57, 5200);
					}
					else if(i1 < 500)
					{
						st.giveItems(627, 1);
						st.giveItems(57, 1500);
					}
					else if(i1 < 750)
					{
						st.giveItems(1123, 1);
						st.giveItems(57, 3200);
					}
					else if(i1 < 1000)
					{
						st.giveItems(605, 1);
						st.giveItems(57, 3200);
					}
					st.unset("tooth_of_dragon");
					st.unset("tooth_of_dragon_cookie");
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(false);
					htmltext = "iris_q0038_0601.htm";
				}
				else
				{
					htmltext = "iris_q0038_0602.htm";
				}
			}
		}
		else if(npcId == 30344 && event.equalsIgnoreCase("reply_1") && GetHTMLCookie == 3)
		{
			if(st.getQuestItemsCount(7176) >= 1)
			{
				st.setCond(5);
				st.set("tooth_of_dragon", String.valueOf(41), true);
				st.takeItems(7176, 1);
				st.giveItems(7177, 1);
				htmltext = "magister_rohmer_q0038_0401.htm";
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "magister_rohmer_q0038_0402.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("tooth_of_dragon");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30386)
					break;
				if(st.getPlayer().getLevel() >= 19)
				{
					htmltext = "guard_luis_q0038_0101.htm";
					break;
				}
				htmltext = "guard_luis_q0038_0103.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 30386)
				{
					if(GetMemoState >= 11 && GetMemoState <= 12)
					{
						if(GetMemoState == 12 && st.getQuestItemsCount(7173) >= 100)
						{
							st.set("tooth_of_dragon_cookie", String.valueOf(1), true);
							htmltext = "guard_luis_q0038_0105.htm";
							break;
						}
						htmltext = "guard_luis_q0038_0106.htm";
						break;
					}
					if(GetMemoState != 21)
						break;
					htmltext = "guard_luis_q0038_0203.htm";
					break;
				}
				if(npcId == 30034)
				{
					if(st.getQuestItemsCount(7174) >= 1 && GetMemoState == 21)
					{
						st.set("tooth_of_dragon_cookie", String.valueOf(2), true);
						htmltext = "iris_q0038_0201.htm";
						break;
					}
					if(GetMemoState == 31)
					{
						htmltext = "iris_q0038_0303.htm";
						break;
					}
					if(st.getQuestItemsCount(7177) >= 1 && GetMemoState == 41)
					{
						st.set("tooth_of_dragon_cookie", String.valueOf(4), true);
						htmltext = "iris_q0038_0401.htm";
						break;
					}
					if(GetMemoState > 52 || GetMemoState < 51)
						break;
					if(GetMemoState == 52 && st.getQuestItemsCount(7175) >= 50)
					{
						st.set("tooth_of_dragon_cookie", String.valueOf(5), true);
						htmltext = "iris_q0038_0503.htm";
						break;
					}
					htmltext = "iris_q0038_0504.htm";
					break;
				}
				if(npcId != 30344)
					break;
				if(st.getQuestItemsCount(7176) >= 1 && GetMemoState == 31)
				{
					st.set("tooth_of_dragon_cookie", String.valueOf(3), true);
					htmltext = "magister_rohmer_q0038_0301.htm";
					break;
				}
				if(GetMemoState != 41)
					break;
				htmltext = "magister_rohmer_q0038_0403.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("tooth_of_dragon");
		int npcId = npc.getNpcId();
		if(GetMemoState == 11)
		{
			if(npcId == 20357 || npcId == 21100)
			{
				if(st.getQuestItemsCount(7173) + 1 >= 100)
				{
					if(st.getQuestItemsCount(7173) < 100)
					{
						st.giveItems(7173, 100 - st.getQuestItemsCount(7173));
						st.playSound("ItemSound.quest_middle");
					}
					st.setCond(2);
					st.set("tooth_of_dragon", String.valueOf(12), true);
				}
				else
				{
					st.giveItems(7173, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(GetMemoState == 51 && (npcId == 20356 || npcId == 21101) && Rnd.get(1000) < 500)
		{
			if(st.getQuestItemsCount(7175) + 1 >= 50)
			{
				if(st.getQuestItemsCount(7175) < 50)
				{
					st.giveItems(7175, 50 - st.getQuestItemsCount(7175));
					st.playSound("ItemSound.quest_middle");
				}
				st.setCond(7);
				st.set("tooth_of_dragon", String.valueOf(52), true);
			}
			else
			{
				st.giveItems(7175, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}