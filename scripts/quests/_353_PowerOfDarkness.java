package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _353_PowerOfDarkness extends Quest implements ScriptFile
{
	private static final int GALMAN = 31044;
	private static final int Malruk_Succubus = 20283;
	private static final int Malruk_Succubus_Turen = 20284;
	private static final int Malruk_Succubus2 = 20244;
	private static final int Malruk_Succubus_Turen2 = 20245;
	private static final int STONE = 5862;
	private static final int ADENA = 57;
	private static final int STONE_CHANCE = 50;
	
	public _353_PowerOfDarkness()
	{
		super(false);
		addStartNpc(GALMAN);
		addKillId(Malruk_Succubus);
		addKillId(Malruk_Succubus_Turen);
		addKillId(Malruk_Succubus2);
		addKillId(Malruk_Succubus_Turen2);
		addQuestItem(STONE);
	}
	
	@Override
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		int id = qs.getState();
		if(event.equalsIgnoreCase("31044-04.htm") && id == 1)
		{
			qs.setState(2);
			qs.setCond(1);
			qs.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("31044-08.htm") && id == 2)
		{
			qs.playSound("ItemSound.quest_finish");
			qs.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState qs)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != GALMAN)
		{
			return htmltext;
		}
		if(qs.getState() == 1)
		{
			if(qs.getPlayer().getLevel() >= 55)
			{
				htmltext = "31044-02.htm";
				qs.setCond(0);
			}
			else
			{
				htmltext = "31044-01.htm";
				qs.exitCurrentQuest(true);
			}
		}
		else
		{
			long stone_count = qs.getQuestItemsCount(STONE);
			if(stone_count > 0)
			{
				htmltext = "31044-06.htm";
				qs.takeItems(STONE, -1);
				qs.giveItems(ADENA, 2500 + 230 * stone_count);
				qs.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "31044-05.htm";
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
		if(Rnd.chance(STONE_CHANCE))
		{
			qs.giveItems(STONE, 1);
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