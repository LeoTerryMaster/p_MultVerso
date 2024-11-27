package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _324_SweetestVenom extends Quest implements ScriptFile
{
	private static final int ASTARON = 30351;
	private static final int Prowler = 20034;
	private static final int Venomous_Spider = 20038;
	private static final int Arachnid_Tracker = 20043;
	private static final int VENOM_SAC = 1077;
	private static final int VENOM_SAC_BASECHANCE = 60;
	
	public _324_SweetestVenom()
	{
		super(false);
		addStartNpc(ASTARON);
		addKillId(Prowler);
		addKillId(Venomous_Spider);
		addKillId(Arachnid_Tracker);
		addQuestItem(VENOM_SAC);
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != ASTARON)
		{
			return htmltext;
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getLevel() >= 18)
			{
				htmltext = "astaron_q0324_03.htm";
				st.setCond(0);
			}
			else
			{
				htmltext = "astaron_q0324_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(_state == 2)
		{
			long _count = st.getQuestItemsCount(VENOM_SAC);
			if(_count >= 10)
			{
				htmltext = "astaron_q0324_06.htm";
				st.takeItems(VENOM_SAC, -1);
				st.giveItems(57, 5810);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "astaron_q0324_05.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("astaron_q0324_04.htm") && st.getState() == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
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
		long _count = qs.getQuestItemsCount(VENOM_SAC);
		int _chance = VENOM_SAC_BASECHANCE + (npc.getNpcId() - Prowler) / 4 * 12;
		if(_count < 10 && Rnd.chance(_chance))
		{
			qs.giveItems(VENOM_SAC, 1);
			if(_count == 9)
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