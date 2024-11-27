package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _624_TheFinestIngredientsPart1 extends Quest implements ScriptFile
{
	private static final int JEREMY = 31521;
	private static final int HOT_SPRINGS_ATROX = 21321;
	private static final int HOT_SPRINGS_NEPENTHES = 21319;
	private static final int HOT_SPRINGS_ATROXSPAWN = 21317;
	private static final int HOT_SPRINGS_BANDERSNATCHLING = 21314;
	private static final int SECRET_SPICE = 7204;
	private static final int TRUNK_OF_NEPENTHES = 7202;
	private static final int FOOT_OF_BANDERSNATCHLING = 7203;
	private static final int CRYOLITE = 7080;
	private static final int SAUCE = 7205;
	
	public _624_TheFinestIngredientsPart1()
	{
		super(true);
		addStartNpc(JEREMY);
		addKillId(HOT_SPRINGS_ATROX);
		addKillId(HOT_SPRINGS_NEPENTHES);
		addKillId(HOT_SPRINGS_ATROXSPAWN);
		addKillId(HOT_SPRINGS_BANDERSNATCHLING);
		addQuestItem(TRUNK_OF_NEPENTHES);
		addQuestItem(FOOT_OF_BANDERSNATCHLING);
		addQuestItem(SECRET_SPICE);
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
		if(event.equalsIgnoreCase("jeremy_q0624_0104.htm"))
		{
			if(st.getPlayer().getLevel() >= 73)
			{
				st.setState(2);
				st.setCond(1);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "jeremy_q0624_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("jeremy_q0624_0201.htm"))
		{
			if(st.getQuestItemsCount(TRUNK_OF_NEPENTHES) == 50 && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) == 50 && st.getQuestItemsCount(SECRET_SPICE) == 50)
			{
				st.takeItems(TRUNK_OF_NEPENTHES, -1);
				st.takeItems(FOOT_OF_BANDERSNATCHLING, -1);
				st.takeItems(SECRET_SPICE, -1);
				st.playSound("ItemSound.quest_finish");
				st.giveItems(SAUCE, 1);
				st.giveItems(CRYOLITE, 1);
				htmltext = "jeremy_q0624_0201.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "jeremy_q0624_0202.htm";
				st.setCond(1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		String htmltext = cond == 0 ? "jeremy_q0624_0101.htm" : cond != 3 ? "jeremy_q0624_0106.htm" : "jeremy_q0624_0105.htm";
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(st.getCond() == 1)
		{
			if(npcId == HOT_SPRINGS_NEPENTHES && st.getQuestItemsCount(TRUNK_OF_NEPENTHES) < 50)
			{
				st.rollAndGive(TRUNK_OF_NEPENTHES, 1, 1, 50, 100.0);
			}
			else if(npcId == HOT_SPRINGS_BANDERSNATCHLING && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) < 50)
			{
				st.rollAndGive(FOOT_OF_BANDERSNATCHLING, 1, 1, 50, 100.0);
			}
			else if((npcId == HOT_SPRINGS_ATROX || npcId == HOT_SPRINGS_ATROXSPAWN) && st.getQuestItemsCount(SECRET_SPICE) < 50)
			{
				st.rollAndGive(SECRET_SPICE, 1, 1, 50, 100.0);
			}
			onKillCheck(st);
		}
		return null;
	}
	
	private void onKillCheck(QuestState st)
	{
		if(st.getQuestItemsCount(TRUNK_OF_NEPENTHES) == 50 && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) == 50 && st.getQuestItemsCount(SECRET_SPICE) == 50)
		{
			st.playSound("ItemSound.quest_middle");
			st.setCond(3);
		}
		else
		{
			st.playSound("ItemSound.quest_itemget");
		}
	}
}