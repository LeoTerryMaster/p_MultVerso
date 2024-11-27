package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _020_BringUpWithLove extends Quest implements ScriptFile
{
	private static final int beast_herder_tunatun = 31537;
	private static final int q_jewel_of_innocent = 7185;
	
	public _020_BringUpWithLove()
	{
		super(false);
		addStartNpc(31537);
		addTalkId(31537);
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
		int GetMemoState = st.getInt("givemelove");
		int npcId = npc.getNpcId();
		if(npcId == 31537)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("givemelove", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "beast_herder_tunatun_q0020_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "beast_herder_tunatun_q0020_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "beast_herder_tunatun_q0020_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				htmltext = "beast_herder_tunatun_q0020_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				htmltext = "beast_herder_tunatun_q0020_06.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				htmltext = "beast_herder_tunatun_q0020_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_6") && GetMemoState == 1 && st.getQuestItemsCount(7185) >= 1)
			{
				st.takeItems(7185, -1);
				st.giveItems(57, 68500);
				st.unset("givemelove");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
				htmltext = "beast_herder_tunatun_q0020_12.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("givemelove");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 31537)
					break;
				if(st.getPlayer().getLevel() >= 65)
				{
					htmltext = "beast_herder_tunatun_q0020_01.htm";
					break;
				}
				st.exitCurrentQuest(true);
				htmltext = "beast_herder_tunatun_q0020_02.htm";
				break;
			}
			case 2:
			{
				if(npcId != 31537)
					break;
				if(GetMemoState == 1 && st.getQuestItemsCount(7185) == 0)
				{
					htmltext = "beast_herder_tunatun_q0020_10.htm";
					break;
				}
				if(GetMemoState != 1 || st.getQuestItemsCount(7185) < 1)
					break;
				htmltext = "beast_herder_tunatun_q0020_11.htm";
			}
		}
		return htmltext;
	}
}