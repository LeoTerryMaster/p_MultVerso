package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _053_LinnaeusSpecialBait extends Quest implements ScriptFile
{
	int Linnaeu = 31577;
	int CrimsonDrake = 20670;
	int HeartOfCrimsonDrake = 7624;
	int FlameFishingLure = 7613;
	Integer FishSkill = 1315;
	
	public _053_LinnaeusSpecialBait()
	{
		super(false);
		addStartNpc(Linnaeu);
		addTalkId(Linnaeu);
		addKillId(CrimsonDrake);
		addQuestItem(HeartOfCrimsonDrake);
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
		if(event.equals("fisher_linneaus_q0053_0104.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("fisher_linneaus_q0053_0201.htm"))
		{
			if(st.getQuestItemsCount(HeartOfCrimsonDrake) < 100)
			{
				htmltext = "fisher_linneaus_q0053_0202.htm";
			}
			else
			{
				st.unset("cond");
				st.takeItems(HeartOfCrimsonDrake, -1);
				st.giveItems(FlameFishingLure, 4);
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
		if(npcId == Linnaeu)
		{
			if(id == 1)
			{
				if(st.getPlayer().getLevel() < 60)
				{
					htmltext = "fisher_linneaus_q0053_0103.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 21)
				{
					htmltext = "fisher_linneaus_q0053_0101.htm";
				}
				else
				{
					htmltext = "fisher_linneaus_q0053_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
			{
				if(st.getQuestItemsCount(HeartOfCrimsonDrake) < 100)
				{
					htmltext = "fisher_linneaus_q0053_0106.htm";
					st.setCond(1);
				}
				else
				{
					htmltext = "fisher_linneaus_q0053_0105.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == CrimsonDrake && st.getCond() == 1 && st.getQuestItemsCount(HeartOfCrimsonDrake) < 100 && Rnd.chance(30))
		{
			st.giveItems(HeartOfCrimsonDrake, 1);
			if(st.getQuestItemsCount(HeartOfCrimsonDrake) == 100)
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