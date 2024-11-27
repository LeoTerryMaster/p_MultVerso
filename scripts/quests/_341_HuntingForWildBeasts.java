package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _341_HuntingForWildBeasts extends Quest implements ScriptFile
{
	private static final int PANO = 30078;
	private static final int Red_Bear = 20021;
	private static final int Dion_Grizzly = 20203;
	private static final int Brown_Bear = 20310;
	private static final int Grizzly_Bear = 20335;
	private static final int BEAR_SKIN = 4259;
	private static final int BEAR_SKIN_CHANCE = 40;
	
	public _341_HuntingForWildBeasts()
	{
		super(false);
		addStartNpc(PANO);
		addKillId(Red_Bear);
		addKillId(Dion_Grizzly);
		addKillId(Brown_Bear);
		addKillId(Grizzly_Bear);
		addQuestItem(BEAR_SKIN);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept") && st.getState() == 1)
		{
			htmltext = "pano_q0341_04.htm";
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != PANO)
		{
			return htmltext;
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getLevel() >= 20)
			{
				htmltext = "pano_q0341_01.htm";
				st.setCond(0);
			}
			else
			{
				htmltext = "pano_q0341_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(_state == 2)
		{
			if(st.getQuestItemsCount(BEAR_SKIN) >= 20)
			{
				htmltext = "pano_q0341_05.htm";
				st.takeItems(BEAR_SKIN, -1);
				st.giveItems(57, 3710);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "pano_q0341_06.htm";
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
		long BEAR_SKIN_COUNT = qs.getQuestItemsCount(BEAR_SKIN);
		if(BEAR_SKIN_COUNT < 20 && Rnd.chance(BEAR_SKIN_CHANCE))
		{
			qs.giveItems(BEAR_SKIN, 1);
			if(BEAR_SKIN_COUNT == 19)
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