package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _328_SenseForBusiness extends Quest implements ScriptFile
{
	private final int SARIEN = 30436;
	private final int MONSTER_EYE_CARCASS = 1347;
	private final int MONSTER_EYE_LENS = 1366;
	private final int BASILISK_GIZZARD = 1348;
	
	public _328_SenseForBusiness()
	{
		super(false);
		addStartNpc(SARIEN);
		addKillId(20055);
		addKillId(20059);
		addKillId(20067);
		addKillId(20068);
		addKillId(20070);
		addKillId(20072);
		addQuestItem(MONSTER_EYE_CARCASS);
		addQuestItem(MONSTER_EYE_LENS);
		addQuestItem(BASILISK_GIZZARD);
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
		if(event.equalsIgnoreCase("trader_salient_q0328_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("trader_salient_q0328_06.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int id = st.getState();
		if(id == 1)
		{
			st.setCond(0);
		}
		String htmltext;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() >= 21)
			{
				String htmltext2 = "trader_salient_q0328_02.htm";
				return htmltext2;
			}
			htmltext = "trader_salient_q0328_01.htm";
			st.exitCurrentQuest(true);
		}
		else
		{
			long lenses;
			long gizzard;
			long carcass = st.getQuestItemsCount(MONSTER_EYE_CARCASS);
			if(carcass + (lenses = st.getQuestItemsCount(MONSTER_EYE_LENS)) + (gizzard = st.getQuestItemsCount(BASILISK_GIZZARD)) > 0)
			{
				st.giveItems(57, 30 * carcass + 2000 * lenses + 75 * gizzard);
				st.takeItems(MONSTER_EYE_CARCASS, -1);
				st.takeItems(MONSTER_EYE_LENS, -1);
				st.takeItems(BASILISK_GIZZARD, -1);
				htmltext = "trader_salient_q0328_05.htm";
			}
			else
			{
				htmltext = "trader_salient_q0328_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int n = Rnd.get(1, 100);
		if(npcId == 20055)
		{
			if(n < 47)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(n < 49)
			{
				st.giveItems(MONSTER_EYE_LENS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20059)
		{
			if(n < 51)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(n < 53)
			{
				st.giveItems(MONSTER_EYE_LENS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20067)
		{
			if(n < 67)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(n < 69)
			{
				st.giveItems(MONSTER_EYE_LENS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20068)
		{
			if(n < 75)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(n < 77)
			{
				st.giveItems(MONSTER_EYE_LENS, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20070)
		{
			if(n < 50)
			{
				st.giveItems(BASILISK_GIZZARD, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20072 && n < 51)
		{
			st.giveItems(BASILISK_GIZZARD, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}