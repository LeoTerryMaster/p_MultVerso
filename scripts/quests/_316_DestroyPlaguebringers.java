package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _316_DestroyPlaguebringers extends Quest implements ScriptFile
{
	private static final int Ellenia = 30155;
	private static final int Sukar_Wererat = 20040;
	private static final int Sukar_Wererat_Leader = 20047;
	private static final int Varool_Foulclaw = 27020;
	private static final int Wererats_Fang = 1042;
	private static final int Varool_Foulclaws_Fang = 1043;
	private static final int Wererats_Fang_Chance = 50;
	private static final int Varool_Foulclaws_Fang_Chance = 30;
	
	public _316_DestroyPlaguebringers()
	{
		super(false);
		addStartNpc(Ellenia);
		addKillId(Sukar_Wererat);
		addKillId(Sukar_Wererat_Leader);
		addKillId(Varool_Foulclaw);
		addQuestItem(Wererats_Fang);
		addQuestItem(Varool_Foulclaws_Fang);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("elliasin_q0316_04.htm") && _state == 1 && st.getPlayer().getRace() == Race.elf && st.getPlayer().getLevel() >= 18)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("elliasin_q0316_08.htm") && _state == 2)
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
		if(npc.getNpcId() != Ellenia)
		{
			return htmltext;
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getRace() != Race.elf)
			{
				htmltext = "elliasin_q0316_00.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() < 18)
			{
				htmltext = "elliasin_q0316_02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "elliasin_q0316_03.htm";
				st.setCond(0);
			}
		}
		else if(_state == 2)
		{
			long Reward = st.getQuestItemsCount(Wererats_Fang) * 60 + st.getQuestItemsCount(Varool_Foulclaws_Fang) * 10000;
			if(Reward > 0)
			{
				htmltext = "elliasin_q0316_07.htm";
				st.takeItems(Wererats_Fang, -1);
				st.takeItems(Varool_Foulclaws_Fang, -1);
				st.giveItems(57, Reward);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "elliasin_q0316_05.htm";
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
		if(npc.getNpcId() == Varool_Foulclaw && qs.getQuestItemsCount(Varool_Foulclaws_Fang) == 0 && Rnd.chance(Varool_Foulclaws_Fang_Chance))
		{
			qs.giveItems(Varool_Foulclaws_Fang, 1);
			qs.playSound("ItemSound.quest_itemget");
		}
		else if(Rnd.chance(Wererats_Fang_Chance))
		{
			qs.giveItems(Wererats_Fang, 1);
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