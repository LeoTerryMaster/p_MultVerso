package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.SkillTable;

public class _367_ElectrifyingRecharge extends Quest implements ScriptFile
{
	private static final int LORAIN = 30673;
	private static final int CATHEROK = 21035;
	private static final int Titan_Lamp_First = 5875;
	private static final int Titan_Lamp_Last = 5879;
	private static final int Broken_Titan_Lamp = 5880;
	private static final int broke_chance = 3;
	private static final int uplight_chance = 7;
	
	public _367_ElectrifyingRecharge()
	{
		super(false);
		addStartNpc(LORAIN);
		addKillId(CATHEROK);
		int Titan_Lamp_id = Titan_Lamp_First;
		while(Titan_Lamp_id <= Titan_Lamp_Last)
		{
			addQuestItem(Titan_Lamp_id++);
		}
		addQuestItem(Broken_Titan_Lamp);
	}
	
	private static boolean takeAllLamps(QuestState st)
	{
		boolean result = false;
		for(int Titan_Lamp_id = Titan_Lamp_First;Titan_Lamp_id <= Titan_Lamp_Last;++Titan_Lamp_id)
		{
			if(st.getQuestItemsCount(Titan_Lamp_id) <= 0)
				continue;
			result = true;
			st.takeItems(Titan_Lamp_id, -1);
		}
		if(st.getQuestItemsCount(Broken_Titan_Lamp) > 0)
		{
			result = true;
			st.takeItems(Broken_Titan_Lamp, -1);
		}
		return result;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("30673-03.htm") && _state == 1)
		{
			takeAllLamps(st);
			st.giveItems(Titan_Lamp_First, 1);
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30673-07.htm") && _state == 2)
		{
			takeAllLamps(st);
			st.giveItems(Titan_Lamp_First, 1);
		}
		else if(event.equalsIgnoreCase("30673-08.htm") && _state == 2)
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != LORAIN)
		{
			return htmltext;
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getLevel() < 37)
			{
				htmltext = "30673-02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "30673-01.htm";
				st.setCond(0);
			}
		}
		else if(_state == 2)
		{
			if(st.getQuestItemsCount(Titan_Lamp_Last) > 0)
			{
				htmltext = "30673-06.htm";
				takeAllLamps(st);
				st.giveItems(4553 + Rnd.get(12), 1);
				st.playSound("ItemSound.quest_middle");
			}
			else if(st.getQuestItemsCount(Broken_Titan_Lamp) > 0)
			{
				htmltext = "30673-05.htm";
				takeAllLamps(st);
				st.giveItems(Titan_Lamp_First, 1);
			}
			else
			{
				htmltext = "30673-04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		if(qs.getQuestItemsCount(Broken_Titan_Lamp) > 0)
		{
			return null;
		}
		if(Rnd.chance(uplight_chance))
		{
			for(int Titan_Lamp_id = Titan_Lamp_First;Titan_Lamp_id < Titan_Lamp_Last;++Titan_Lamp_id)
			{
				if(qs.getQuestItemsCount(Titan_Lamp_id) > 0)
				{
					int Titan_Lamp_Next = Titan_Lamp_id + 1;
					takeAllLamps(qs);
					qs.giveItems(Titan_Lamp_Next, 1);
					if(Titan_Lamp_Next == Titan_Lamp_Last)
					{
						qs.setCond(2);
						qs.playSound("ItemSound.quest_middle");
					}
					else
					{
						qs.playSound("ItemSound.quest_itemget");
					}
					npc.doCast(SkillTable.getInstance().getInfo(4072, 4), qs.getPlayer(), true);
					return null;
				}
				if(!Rnd.chance(broke_chance) || !takeAllLamps(qs))
					continue;
				qs.giveItems(Broken_Titan_Lamp, 1);
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