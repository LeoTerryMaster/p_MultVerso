package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _368_TrespassingIntoTheSacredArea extends Quest implements ScriptFile
{
	private static final int RESTINA = 30926;
	private static final int BLADE_STAKATO_FANG = 5881;
	private static final int BLADE_STAKATO_FANG_BASECHANCE = 10;
	
	public _368_TrespassingIntoTheSacredArea()
	{
		super(false);
		addStartNpc(RESTINA);
		int Blade_Stakato_id = 20794;
		while(Blade_Stakato_id <= 20797)
		{
			addKillId(Blade_Stakato_id++);
		}
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != RESTINA)
		{
			return htmltext;
		}
		if(st.getState() == 1)
		{
			if(st.getPlayer().getLevel() < 36)
			{
				htmltext = "30926-00.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "30926-01.htm";
				st.setCond(0);
			}
		}
		else
		{
			long _count = st.getQuestItemsCount(BLADE_STAKATO_FANG);
			if(_count > 0)
			{
				htmltext = "30926-04.htm";
				st.takeItems(BLADE_STAKATO_FANG, -1);
				st.giveItems(57, _count * 2250);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "30926-03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("30926-02.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30926-05.htm") && _state == 2)
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		if(Rnd.chance(npc.getNpcId() - 20794 + BLADE_STAKATO_FANG_BASECHANCE))
		{
			qs.giveItems(BLADE_STAKATO_FANG, 1);
			qs.playSound("ItemSound.quest_itemget");
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