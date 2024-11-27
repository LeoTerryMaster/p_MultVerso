package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _652_AnAgedExAdventurer extends Quest implements ScriptFile
{
	private static final int Tantan = 32012;
	private static final int Sara = 30180;
	private static final int SoulshotCgrade = 1464;
	private static final int ScrollEnchantArmorD = 956;
	
	public _652_AnAgedExAdventurer()
	{
		super(false);
		addStartNpc(32012);
		addTalkId(30180);
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
		if(event.equalsIgnoreCase("retired_oldman_tantan_q0652_03.htm") && st.getQuestItemsCount(1464) >= 100)
		{
			st.setCond(1);
			st.setState(2);
			st.takeItems(1464, 100);
			st.playSound("ItemSound.quest_accept");
			htmltext = "retired_oldman_tantan_q0652_04.htm";
		}
		else
		{
			htmltext = "retired_oldman_tantan_q0652_03.htm";
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_giveup");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 32012)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 46)
				{
					htmltext = "retired_oldman_tantan_q0652_01a.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "retired_oldman_tantan_q0652_01.htm";
				}
			}
		}
		else if(npcId == 30180 && cond == 1)
		{
			htmltext = "sara_q0652_01.htm";
			st.giveItems(57, 5026, true);
			if(Rnd.chance(50))
			{
				st.giveItems(956, 1, false);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
}