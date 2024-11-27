package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _052_WilliesSpecialBait extends Quest implements ScriptFile
{
	private static final int Willie = 31574;
	private static final int[] TarlkBasilisks = {20573, 20574};
	private static final int EyeOfTarlkBasilisk = 7623;
	private static final int EarthFishingLure = 7612;
	private static final Integer FishSkill = 1315;
	
	public _052_WilliesSpecialBait()
	{
		super(false);
		addStartNpc(31574);
		addKillId(TarlkBasilisks);
		addQuestItem(7623);
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
		if(event.equals("fisher_willeri_q0052_0104.htm"))
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("fisher_willeri_q0052_0201.htm"))
		{
			if(st.getQuestItemsCount(7623) < 100)
			{
				htmltext = "fisher_willeri_q0052_0202.htm";
			}
			else
			{
				st.unset("cond");
				st.takeItems(7623, -1);
				st.giveItems(7612, 4);
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
		if(npcId == 31574)
		{
			if(id == 1)
			{
				if(st.getPlayer().getLevel() < 48)
				{
					htmltext = "fisher_willeri_q0052_0103.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getSkillLevel(FishSkill) >= 16)
				{
					htmltext = "fisher_willeri_q0052_0101.htm";
				}
				else
				{
					htmltext = "fisher_willeri_q0052_0102.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
			{
				if(st.getQuestItemsCount(7623) < 100)
				{
					htmltext = "fisher_willeri_q0052_0106.htm";
					st.setCond(1);
				}
				else
				{
					htmltext = "fisher_willeri_q0052_0105.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if((npcId == TarlkBasilisks[0] || npcId == TarlkBasilisks[1] && st.getCond() == 1) && st.getQuestItemsCount(7623) < 100 && Rnd.chance(30))
		{
			st.giveItems(7623, 1);
			if(st.getQuestItemsCount(7623) == 100)
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