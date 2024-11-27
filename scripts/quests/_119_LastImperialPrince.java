package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _119_LastImperialPrince extends Quest implements ScriptFile
{
	private static final int printessa_spirit = 31453;
	private static final int frintessa_nurse = 32009;
	private static final int q_antique_brooch = 7262;
	
	public _119_LastImperialPrince()
	{
		super(false);
		addStartNpc(31453);
		addTalkId(32009);
		addQuestItem(7262);
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
		int GetMemoState = st.getInt("the_last_imperial_prince");
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.set("the_last_imperial_prince", String.valueOf(1), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "printessa_spirit_q0119_06.htm";
		}
		else if(event.equalsIgnoreCase("reply_4") && GetMemoState == 2)
		{
			st.giveItems(57, 68787, true);
			st.playSound("ItemSound.quest_finish");
			st.unset("the_last_imperial_prince");
			htmltext = "printessa_spirit_q0119_10.htm";
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			htmltext = st.getQuestItemsCount(7262) >= 1 ? "frintessa_nurse_q0119_02.htm" : "frintessa_nurse_q0119_02a.htm";
		}
		else if(event.equalsIgnoreCase("reply_2") && GetMemoState == 1 && st.getQuestItemsCount(7262) >= 1)
		{
			st.setCond(2);
			st.set("the_last_imperial_prince", String.valueOf(2), true);
			st.playSound("ItemSound.quest_middle");
			htmltext = "frintessa_nurse_q0119_03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("the_last_imperial_prince");
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31453)
					break;
				if(st.getPlayer().getLevel() >= 74 && st.getQuestItemsCount(7262) >= 1)
				{
					htmltext = "printessa_spirit_q0119_01.htm";
					break;
				}
				htmltext = "printessa_spirit_q0119_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId == 31453)
				{
					if(GetMemoState == 1 && st.getQuestItemsCount(7262) >= 1)
					{
						htmltext = "printessa_spirit_q0119_07.htm";
						break;
					}
					if(GetMemoState == 1 && st.getQuestItemsCount(7262) == 0)
					{
						htmltext = "printessa_spirit_q0119_07a.htm";
						st.exitCurrentQuest(true);
						break;
					}
					if(GetMemoState != 2)
						break;
					htmltext = "printessa_spirit_q0119_08.htm";
					break;
				}
				if(npcId != 32009)
					break;
				if(GetMemoState == 1)
				{
					htmltext = "frintessa_nurse_q0119_01.htm";
					break;
				}
				if(GetMemoState != 2)
					break;
				htmltext = "frintessa_nurse_q0119_04.htm";
				break;
			}
			case 3:
			{
				if(npcId != 31453)
					break;
				htmltext = "printessa_spirit_q0119_03.htm";
			}
		}
		return htmltext;
	}
}