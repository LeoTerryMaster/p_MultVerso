package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _366_SilverHairedShaman extends Quest implements ScriptFile
{
	private static final int DIETER = 30111;
	private static final int SAIRON = 20986;
	private static final int SAIRONS_DOLL = 20987;
	private static final int SAIRONS_PUPPET = 20988;
	private static final int ADENA_PER_ONE = 500;
	private static final int START_ADENA = 12070;
	private static final int SAIRONS_SILVER_HAIR = 5874;
	
	public _366_SilverHairedShaman()
	{
		super(false);
		addStartNpc(30111);
		addKillId(20986);
		addKillId(20987);
		addKillId(20988);
		addQuestItem(5874);
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
		if(event.equalsIgnoreCase("30111-02.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30111-quit.htm"))
		{
			st.takeItems(5874, -1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(id == 1)
		{
			st.setCond(0);
		}
		else
		{
			cond = st.getCond();
		}
		String htmltext = "noquest";
		if(npcId == 30111)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 48)
				{
					htmltext = "30111-01.htm";
				}
				else
				{
					htmltext = "30111-00.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 && st.getQuestItemsCount(5874) == 0)
			{
				htmltext = "30111-03.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(5874) >= 1)
			{
				st.giveItems(57, st.getQuestItemsCount(5874) * 500 + 12070);
				st.takeItems(5874, -1);
				htmltext = "30111-have.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1 && Rnd.chance(66))
		{
			st.giveItems(5874, 1);
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}
}