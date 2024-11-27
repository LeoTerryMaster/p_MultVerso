package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _051_OFullesSpecialBait extends Quest implements ScriptFile
{
	int OFulle = 31572;
	int FetteredSoul = 20552;
	int LostBaitIngredient = 7622;
	int IcyAirFishingLure = 7611;
	Integer FishSkill = 1315;
	
	public _051_OFullesSpecialBait()
	{
		super(false);
		addStartNpc(OFulle);
		addTalkId(OFulle);
		addKillId(FetteredSoul);
		addQuestItem(LostBaitIngredient);
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
		if(event.equals("fisher_ofulle_q0051_0104.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("fisher_ofulle_q0051_0201.htm"))
		{
			if(st.getQuestItemsCount(LostBaitIngredient) < 100)
			{
				htmltext = "fisher_ofulle_q0051_0202.htm";
			}
			else
			{
				st.unset("cond");
				st.takeItems(LostBaitIngredient, -1);
				st.giveItems(IcyAirFishingLure, 4);
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(false);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		int id = st.getState();
		if(npcId == OFulle)
		{
			if(id == 1)
			{
				if(st.getPlayer().getLevel() < 36)
				{
					htmltext = "fisher_ofulle_q0051_0103.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 11)
				{
					htmltext = "fisher_ofulle_q0051_0101.htm";
				}
				else
				{
					htmltext = "fisher_ofulle_q0051_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
			{
				if(st.getQuestItemsCount(LostBaitIngredient) < 100)
				{
					htmltext = "fisher_ofulle_q0051_0106.htm";
					st.setCond(1);
				}
				else
				{
					htmltext = "fisher_ofulle_q0051_0105.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == FetteredSoul && st.getCond() == 1 && st.getQuestItemsCount(LostBaitIngredient) < 100 && Rnd.chance(30))
		{
			st.giveItems(LostBaitIngredient, 1);
			if(st.getQuestItemsCount(LostBaitIngredient) == 100)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
}