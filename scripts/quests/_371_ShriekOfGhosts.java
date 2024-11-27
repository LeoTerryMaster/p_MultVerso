package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _371_ShriekOfGhosts extends Quest implements ScriptFile
{
	private static final int seer_reva = 30867;
	private static final int patrin = 30929;
	private static final int hallates_warrior = 20818;
	private static final int hallates_knight = 20820;
	private static final int hallates_commander = 20824;
	private static final int ancient_porcelain = 6002;
	private static final int ancient_porcelain_s = 6003;
	private static final int ancient_porcelain_a = 6004;
	private static final int ancient_porcelain_b = 6005;
	private static final int ancient_porcelain_c = 6006;
	private static final int ancient_funeral_urn = 5903;
	
	public _371_ShriekOfGhosts()
	{
		super(true);
		addStartNpc(seer_reva);
		addTalkId(patrin);
		addKillId(hallates_warrior, hallates_knight, hallates_commander);
		addQuestItem(ancient_funeral_urn);
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
		int npcId = npc.getNpcId();
		if(npcId == seer_reva)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("spirits_cry_secrets", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "seer_reva_q0371_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				if(st.getQuestItemsCount(ancient_funeral_urn) < 1)
				{
					htmltext = "seer_reva_q0371_06.htm";
				}
				else if(st.getQuestItemsCount(ancient_funeral_urn) >= 1 && st.getQuestItemsCount(ancient_funeral_urn) < 100)
				{
					st.giveItems(57, st.getQuestItemsCount(ancient_funeral_urn) * 1000 + 15000);
					st.takeItems(ancient_funeral_urn, -1);
					htmltext = "seer_reva_q0371_07.htm";
				}
				else if(st.getQuestItemsCount(ancient_funeral_urn) >= 100)
				{
					st.giveItems(57, st.getQuestItemsCount(ancient_funeral_urn) * 1000 + 37700);
					st.takeItems(ancient_funeral_urn, -1);
					htmltext = "seer_reva_q0371_08.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "seer_reva_q0371_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				if(st.getQuestItemsCount(ancient_funeral_urn) > 0)
				{
					st.giveItems(57, st.getQuestItemsCount(ancient_funeral_urn) * 1000);
				}
				st.takeItems(ancient_funeral_urn, -1);
				st.unset("spirits_cry_secrets");
				st.exitCurrentQuest(true);
				htmltext = "seer_reva_q0371_10.htm";
			}
		}
		else if(npcId == patrin && event.equalsIgnoreCase("reply_1"))
		{
			if(st.getQuestItemsCount(ancient_porcelain) < 1)
			{
				htmltext = "patrin_q0371_02.htm";
			}
			else if(st.getQuestItemsCount(ancient_porcelain) >= 1)
			{
				int i0 = Rnd.get(100);
				if(i0 < 2)
				{
					st.giveItems(ancient_porcelain_s, 1);
					st.takeItems(ancient_porcelain, 1);
					htmltext = "patrin_q0371_03.htm";
				}
				else if(i0 < 32)
				{
					st.giveItems(ancient_porcelain_a, 1);
					st.takeItems(ancient_porcelain, 1);
					htmltext = "patrin_q0371_04.htm";
				}
				else if(i0 < 62)
				{
					st.giveItems(ancient_porcelain_b, 1);
					st.takeItems(ancient_porcelain, 1);
					htmltext = "patrin_q0371_05.htm";
				}
				else if(i0 < 77)
				{
					st.giveItems(ancient_porcelain_c, 1);
					st.takeItems(ancient_porcelain, 1);
					htmltext = "patrin_q0371_06.htm";
				}
				else
				{
					st.giveItems(ancient_porcelain, 1);
					htmltext = "patrin_q0371_07.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("spirits_cry_secrets");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != seer_reva)
					break;
				if(st.getPlayer().getLevel() < 59)
				{
					htmltext = "seer_reva_q0371_01.htm";
					st.exitCurrentQuest(true);
					break;
				}
				if(st.getPlayer().getLevel() < 59)
					break;
				htmltext = "seer_reva_q0371_02.htm";
				break;
			}
			case 2:
			{
				if(npcId == seer_reva)
				{
					if(GetMemoState == 1 && st.getQuestItemsCount(ancient_porcelain) < 1)
					{
						htmltext = "seer_reva_q0371_04.htm";
						break;
					}
					if(GetMemoState != 1 || st.getQuestItemsCount(ancient_porcelain) < 1)
						break;
					htmltext = "seer_reva_q0371_05.htm";
					break;
				}
				if(npcId != patrin || GetMemoState != 1)
					break;
				htmltext = "patrin_q0371_01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("spirits_cry_secrets");
		int npcId = npc.getNpcId();
		if(GetMemoState == 1)
		{
			if(npcId == hallates_warrior)
			{
				int i4 = Rnd.get(1000);
				if(i4 < 350)
				{
					st.giveItems(ancient_funeral_urn, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i4 < 400)
				{
					st.giveItems(ancient_porcelain, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == hallates_knight)
			{
				int i4 = Rnd.get(1000);
				if(i4 < 583)
				{
					st.giveItems(ancient_funeral_urn, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i4 < 673)
				{
					st.giveItems(ancient_porcelain, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == hallates_commander)
			{
				int i4 = Rnd.get(1000);
				if(i4 < 458)
				{
					st.giveItems(ancient_funeral_urn, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i4 < 538)
				{
					st.giveItems(ancient_porcelain, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}