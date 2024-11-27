package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _112_WalkOfFate extends Quest implements ScriptFile
{
	private static final int Livina = 30572;
	private static final int Karuda = 32017;
	private static final int EnchantD = 956;
	
	public _112_WalkOfFate()
	{
		super(false);
		addStartNpc(30572);
		addTalkId(32017);
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
		if(event.equalsIgnoreCase("karuda_q0112_0201.htm"))
		{
			st.giveItems(57, 4665, true);
			st.giveItems(956, 1, false);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("seer_livina_q0112_0104.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30572)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 20)
				{
					htmltext = "seer_livina_q0112_0101.htm";
				}
				else
				{
					htmltext = "seer_livina_q0112_0103.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "seer_livina_q0112_0105.htm";
			}
		}
		else if(npcId == 32017 && cond == 1)
		{
			htmltext = "karuda_q0112_0101.htm";
		}
		return htmltext;
	}
}