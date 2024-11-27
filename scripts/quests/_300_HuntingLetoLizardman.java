package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _300_HuntingLetoLizardman extends Quest implements ScriptFile
{
	private static final int RATH = 30126;
	private static final int BRACELET_OF_LIZARDMAN = 7139;
	private static final int ANIMAL_BONE = 1872;
	private static final int ANIMAL_SKIN = 1867;
	private static final int BRACELET_OF_LIZARDMAN_CHANCE = 70;
	
	public _300_HuntingLetoLizardman()
	{
		super(false);
		addStartNpc(RATH);
		int lizardman_id = 20577;
		while(lizardman_id <= 20582)
		{
			addKillId(lizardman_id++);
		}
		addQuestItem(BRACELET_OF_LIZARDMAN);
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != RATH)
		{
			return htmltext;
		}
		if(st.getState() == 1)
		{
			if(st.getPlayer().getLevel() < 34)
			{
				htmltext = "rarshints_q0300_0103.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "rarshints_q0300_0101.htm";
				st.setCond(0);
			}
		}
		else if(st.getQuestItemsCount(BRACELET_OF_LIZARDMAN) < 60)
		{
			htmltext = "rarshints_q0300_0106.htm";
			st.setCond(1);
		}
		else
		{
			htmltext = "rarshints_q0300_0105.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int _state = st.getState();
		if(event.equalsIgnoreCase("rarshints_q0300_0104.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("rarshints_q0300_0201.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(BRACELET_OF_LIZARDMAN) < 60)
			{
				htmltext = "rarshints_q0300_0202.htm";
				st.setCond(1);
			}
			else
			{
				st.takeItems(BRACELET_OF_LIZARDMAN, -1);
				switch(Rnd.get(3))
				{
					case 0:
					{
						st.giveItems(57, 30000, true);
						break;
					}
					case 1:
					{
						st.giveItems(ANIMAL_BONE, 50, true);
						break;
					}
					case 2:
					{
						st.giveItems(ANIMAL_SKIN, 50, true);
					}
				}
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		long _count = qs.getQuestItemsCount(BRACELET_OF_LIZARDMAN);
		if(_count < 60 && Rnd.chance(BRACELET_OF_LIZARDMAN_CHANCE))
		{
			qs.giveItems(BRACELET_OF_LIZARDMAN, 1);
			if(_count == 59)
			{
				qs.setCond(2);
				qs.playSound("ItemSound.quest_middle");
			}
			else
			{
				qs.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
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
}