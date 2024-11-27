package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _157_RecoverSmuggled extends Quest implements ScriptFile
{
	private static final int wilph = 30005;
	private static final int giant_toad = 20121;
	private static final int adamantite_ore = 1024;
	private static final int buckler = 20;
	
	public _157_RecoverSmuggled()
	{
		super(false);
		addStartNpc(30005);
		addTalkId(30005);
		addKillId(20121);
		addQuestItem(1024);
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
		if(event.equals("quest_accept"))
		{
			st.setCond(1);
			st.set("recover_smuggled", String.valueOf(1), true);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
			htmltext = "wilph_q0157_05.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("recover_smuggled");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != 30005)
					break;
				if(st.getPlayer().getLevel() >= 5)
				{
					htmltext = "wilph_q0157_03.htm";
					break;
				}
				htmltext = "wilph_q0157_02.htm";
				st.exitCurrentQuest(true);
				break;
			}
			case 2:
			{
				if(npcId != 30005 || GetMemoState != 1)
					break;
				if(st.getQuestItemsCount(1024) < 20)
				{
					htmltext = "wilph_q0157_06.htm";
					break;
				}
				if(st.getQuestItemsCount(1024) < 20)
					break;
				st.takeItems(1024, -1);
				st.giveItems(20, 1);
				st.playSound("ItemSound.quest_finish");
				st.unset("recover_smuggled");
				htmltext = "wilph_q0157_07.htm";
				st.exitCurrentQuest(false);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("recover_smuggled");
		if(npcId == 20121 && GetMemoState == 1 && st.getQuestItemsCount(1024) < 20 && Rnd.get(10) < 4)
		{
			st.giveItems(1024, 1);
			if(st.getQuestItemsCount(1024) >= 20)
			{
				st.setCond(2);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}