package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _001_LettersOfLove extends Quest implements ScriptFile
{
	private static final int DARIN = 30048;
	private static final int ROXXY = 30006;
	private static final int BAULRO = 30033;
	private static final int DARINGS_LETTER = 687;
	private static final int ROXXY_KERCHIEF = 688;
	private static final int DARINGS_RECEIPT = 1079;
	private static final int BAULS_POTION = 1080;
	private static final int SCROLL_OF_GIRAN = 7559;
	private static final int MARK_OF_TRAVELER_ID = 7570;
	
	public _001_LettersOfLove()
	{
		super(false);
		addStartNpc(30048);
		addTalkId(30006);
		addTalkId(30033);
		addQuestItem(687);
		addQuestItem(688);
		addQuestItem(1079);
		addQuestItem(1080);
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
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "daring_q0001_06.htm";
			qs.setCond(1);
			qs.setState(2);
			qs.giveItems(687, 1, false);
			qs.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case 30048:
			{
				if(cond == 0)
				{
					if(st.getPlayer().getLevel() >= 2)
					{
						htmltext = "daring_q0001_02.htm";
						break;
					}
					htmltext = "daring_q0001_01.htm";
					st.exitCurrentQuest(true);
					break;
				}
				if(cond == 1)
				{
					htmltext = "daring_q0001_07.htm";
					break;
				}
				if(cond == 2 && st.getQuestItemsCount(688) == 1)
				{
					htmltext = "daring_q0001_08.htm";
					st.takeItems(688, -1);
					st.giveItems(1079, 1, false);
					st.setCond(3);
					st.playSound("ItemSound.quest_middle");
					break;
				}
				if(cond == 3)
				{
					htmltext = "daring_q0001_09.htm";
					break;
				}
				if(cond != 4 || st.getQuestItemsCount(1080) != 1)
					break;
				htmltext = "daring_q0001_10.htm";
				st.takeItems(1080, -1);
				st.giveItems(7559, 1, false);
				st.giveItems(7570, 1, false);
				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1"))
				{
					st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				}
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
				break;
			}
			case 30006:
			{
				if(cond == 1 && st.getQuestItemsCount(688) == 0 && st.getQuestItemsCount(687) > 0)
				{
					htmltext = "rapunzel_q0001_01.htm";
					st.takeItems(687, -1);
					st.giveItems(688, 1, false);
					st.setCond(2);
					st.playSound("ItemSound.quest_middle");
					break;
				}
				if(cond == 2 && st.getQuestItemsCount(688) > 0)
				{
					htmltext = "rapunzel_q0001_02.htm";
					break;
				}
				if(cond <= 2 || st.getQuestItemsCount(1080) <= 0 && st.getQuestItemsCount(1079) <= 0)
					break;
				htmltext = "rapunzel_q0001_03.htm";
				break;
			}
			case 30033:
			{
				if(cond == 3 && st.getQuestItemsCount(1079) == 1)
				{
					htmltext = "baul_q0001_01.htm";
					st.takeItems(1079, -1);
					st.giveItems(1080, 1, false);
					st.setCond(4);
					st.playSound("ItemSound.quest_middle");
					break;
				}
				if(cond != 4)
					break;
				htmltext = "baul_q0001_02.htm";
			}
		}
		return htmltext;
	}
}