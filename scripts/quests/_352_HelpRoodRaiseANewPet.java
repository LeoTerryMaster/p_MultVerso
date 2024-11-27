package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _352_HelpRoodRaiseANewPet extends Quest implements ScriptFile
{
	private static final int pet_manager_rood = 31067;
	private static final int lienrik = 20786;
	private static final int lienrik_lad = 20787;
	private static final int lienlik_egg1 = 5860;
	private static final int lienlik_egg2 = 5861;
	
	public _352_HelpRoodRaiseANewPet()
	{
		super(false);
		addStartNpc(pet_manager_rood);
		addKillId(lienrik, lienrik_lad);
		addQuestItem(lienlik_egg1, lienlik_egg2);
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
		if(npcId == pet_manager_rood)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.set("how_about_new_pet", String.valueOf(1), true);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				htmltext = "pet_manager_rood_q0352_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
			{
				htmltext = "pet_manager_rood_q0352_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				htmltext = "pet_manager_rood_q0352_10.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				st.unset("how_about_new_pet");
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
				htmltext = "pet_manager_rood_q0352_11.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				htmltext = "pet_manager_rood_q0352_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int GetMemoState = st.getInt("how_about_new_pet");
		int npcId = npc.getNpcId();
		int id = st.getState();
		switch(id)
		{
			case 1:
			{
				if(npcId != pet_manager_rood)
					break;
				if(st.getPlayer().getLevel() >= 39)
				{
					htmltext = "pet_manager_rood_q0352_02.htm";
					break;
				}
				st.exitCurrentQuest(true);
				htmltext = "pet_manager_rood_q0352_01.htm";
				break;
			}
			case 2:
			{
				if(npcId != pet_manager_rood || GetMemoState != 1)
					break;
				if(st.getQuestItemsCount(lienlik_egg1) < 1 && st.getQuestItemsCount(lienlik_egg2) < 1)
				{
					htmltext = "pet_manager_rood_q0352_06.htm";
					break;
				}
				if(st.getQuestItemsCount(lienlik_egg1) >= 1 && st.getQuestItemsCount(lienlik_egg2) < 1)
				{
					if(st.getQuestItemsCount(lienlik_egg1) >= 10)
					{
						st.giveItems(57, st.getQuestItemsCount(lienlik_egg1) * 34 + 4000);
					}
					else
					{
						st.giveItems(57, st.getQuestItemsCount(lienlik_egg1) * 34 + 2000);
					}
					st.takeItems(lienlik_egg1, -1);
					htmltext = "pet_manager_rood_q0352_07.htm";
					break;
				}
				if(st.getQuestItemsCount(lienlik_egg2) < 1)
					break;
				st.giveItems(57, 4000 + st.getQuestItemsCount(lienlik_egg1) * 34 + st.getQuestItemsCount(lienlik_egg2) * 1025);
				st.takeItems(lienlik_egg2, -1);
				st.takeItems(lienlik_egg1, -1);
				htmltext = "pet_manager_rood_q0352_08.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("how_about_new_pet");
		int npcId = npc.getNpcId();
		if(GetMemoState == 1)
		{
			if(npcId == lienrik)
			{
				int i0 = Rnd.get(100);
				if(i0 < 46)
				{
					st.giveItems(lienlik_egg1, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i0 < 48)
				{
					st.giveItems(lienlik_egg2, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if(npcId == lienrik_lad)
			{
				int i0 = Rnd.get(100);
				if(i0 < 69)
				{
					st.giveItems(lienlik_egg1, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else if(i0 < 71)
				{
					st.giveItems(lienlik_egg2, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
}