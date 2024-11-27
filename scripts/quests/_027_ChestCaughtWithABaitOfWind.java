package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _027_ChestCaughtWithABaitOfWind extends Quest implements ScriptFile
{
	private static final int Lanosco = 31570;
	private static final int Shaling = 31434;
	private static final int StrangeGolemBlueprint = 7625;
	private static final int BigBlueTreasureChest = 6500;
	private static final int BlackPearlRing = 880;
	
	public _027_ChestCaughtWithABaitOfWind()
	{
		super(false);
		addStartNpc(31570);
		addTalkId(31434);
		addQuestItem(7625);
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
		if(event.equals("fisher_lanosco_q0027_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("fisher_lanosco_q0027_0201.htm"))
		{
			if(st.getQuestItemsCount(6500) > 0)
			{
				st.takeItems(6500, 1);
				st.giveItems(7625, 1);
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "fisher_lanosco_q0027_0202.htm";
			}
		}
		else if(event.equals("blueprint_seller_shaling_q0027_0301.htm"))
		{
			if(st.getQuestItemsCount(7625) == 1)
			{
				st.takeItems(7625, -1);
				st.giveItems(880, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
			else
			{
				htmltext = "blueprint_seller_shaling_q0027_0302.htm";
				st.exitCurrentQuest(true);
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
		int id = st.getState();
		if(npcId == 31570)
		{
			if(id == 1)
			{
				if(st.getPlayer().getLevel() < 27)
				{
					htmltext = "fisher_lanosco_q0027_0101.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					QuestState LanoscosSpecialBait = st.getPlayer().getQuestState(_050_LanoscosSpecialBait.class);
					if(LanoscosSpecialBait != null)
					{
						if(LanoscosSpecialBait.isCompleted())
						{
							htmltext = "fisher_lanosco_q0027_0101.htm";
						}
						else
						{
							htmltext = "fisher_lanosco_q0027_0102.htm";
							st.exitCurrentQuest(true);
						}
					}
					else
					{
						htmltext = "fisher_lanosco_q0027_0103.htm";
						st.exitCurrentQuest(true);
					}
				}
			}
			else if(cond == 1)
			{
				htmltext = "fisher_lanosco_q0027_0105.htm";
				if(st.getQuestItemsCount(6500) == 0)
				{
					htmltext = "fisher_lanosco_q0027_0106.htm";
				}
			}
			else if(cond == 2)
			{
				htmltext = "fisher_lanosco_q0027_0203.htm";
			}
		}
		else if(npcId == 31434)
		{
			htmltext = cond == 2 ? "blueprint_seller_shaling_q0027_0201.htm" : "blueprint_seller_shaling_q0027_0302.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		return null;
	}
}