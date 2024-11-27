package quests;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

import java.util.List;

public class _246_PossessorOfaPreciousSoul3 extends Quest implements ScriptFile
{
	int CARADINES_LETTER_2_PART = 7678;
	int RING_OF_GODDESS_WATERBINDER = 7591;
	int NECKLACE_OF_GODDESS_EVERGREEN = 7592;
	int STAFF_OF_GODDESS_RAIN_SONG = 7593;
	int CARADINES_LETTER = 7679;
	int RELIC_BOX = 7594;
	
	public _246_PossessorOfaPreciousSoul3()
	{
		super(true);
		addStartNpc(31740);
		addTalkId(31740);
		addTalkId(31741);
		addTalkId(30721);
		addKillId(21541);
		addKillId(21544);
		addKillId(25325);
		addQuestItem(RING_OF_GODDESS_WATERBINDER, NECKLACE_OF_GODDESS_EVERGREEN, STAFF_OF_GODDESS_RAIN_SONG);
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
		if(event.equals("31740-2.htm"))
		{
			st.setCond(1);
			st.takeItems(CARADINES_LETTER_2_PART, 1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("31741-2.htm"))
		{
			st.setCond(2);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equals("31741-4.htm"))
		{
			st.setCond(4);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equals("31741-6.htm"))
		{
			st.takeItems(RING_OF_GODDESS_WATERBINDER, 1);
			st.takeItems(NECKLACE_OF_GODDESS_EVERGREEN, 1);
			st.takeItems(STAFF_OF_GODDESS_RAIN_SONG, 1);
			st.setCond(6);
			st.giveItems(RELIC_BOX, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equals("30721-2.htm"))
		{
			st.takeItems(RELIC_BOX, 1);
			st.giveItems(CARADINES_LETTER, 1);
			st.unset("cond");
			st.exitCurrentQuest(false);
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31740)
		{
			if(cond == 0)
			{
				if(st.getQuestItemsCount(CARADINES_LETTER_2_PART) >= 1 && st.getPlayer().isSubClassActive() && st.getPlayer().getLevel() >= 65)
				{
					htmltext = "31740-1.htm";
				}
				else
				{
					htmltext = "31740-0.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 && st.getPlayer().isSubClassActive())
			{
				htmltext = "31740-2r.htm";
			}
		}
		else if(npcId == 31741 && st.getPlayer().isSubClassActive())
		{
			if(cond == 1)
			{
				htmltext = "31741-1.htm";
			}
			else if(!(cond != 2 && cond != 3 || st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) >= 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) >= 1))
			{
				htmltext = "8743-2r.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
			{
				htmltext = "31741-3.htm";
			}
			else if(cond == 4)
			{
				htmltext = "31741-4.htm";
			}
			else if((cond == 4 || cond == 5) && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) < 1)
			{
				htmltext = "31741-4r.htm";
			}
			else if(cond == 5 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1 && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) == 1)
			{
				htmltext = "31741-5.htm";
			}
			else if(cond == 6)
			{
				htmltext = "31741-6r.htm";
			}
		}
		else if(npcId == 30721 && st.getPlayer().isSubClassActive() && cond == 6 && st.getQuestItemsCount(RELIC_BOX) == 1)
		{
			htmltext = "30721-1.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 2 && st.getPlayer().isSubClassActive())
		{
			if(!Rnd.chance(15))
				return null;
			if(npcId == 21541 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 0)
			{
				st.giveItems(RING_OF_GODDESS_WATERBINDER, 1);
				if(st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
				{
					st.setCond(3);
				}
				st.playSound("ItemSound.quest_itemget");
				return null;
			}
			else
			{
				if(npcId != 21544 || st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) != 0)
					return null;
				st.giveItems(NECKLACE_OF_GODDESS_EVERGREEN, 1);
				if(st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1)
				{
					st.setCond(3);
				}
				st.playSound("ItemSound.quest_itemget");
			}
			return null;
		}
		else
		{
			if(cond != 4 || !st.getPlayer().isSubClassActive() || npcId != 25325 || st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) != 0)
				return null;
			Player player = st.getPlayer();
			List<Player> partyMembers = st.getPartyMembers(2, Config.ALT_PARTY_DISTRIBUTION_RANGE, player);
			for(Player partyMember : partyMembers)
			{
				QuestState pqs = partyMember.getQuestState(this);
				if(pqs == null || !partyMember.isSubClassActive() || pqs.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) != 0 || pqs.getCond() != 4)
					continue;
				pqs.giveItems(STAFF_OF_GODDESS_RAIN_SONG, 1);
				pqs.setCond(5);
				pqs.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}