package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _642_APowerfulPrimevalCreature extends Quest implements ScriptFile
{
	private static final int Dinn = 32105;
	private static final int Ancient_Egg = 18344;
	private static final int[] Dino = {22196, 22197, 22198, 22199, 22200, 22201, 22202, 22203, 22204, 22205, 22218, 22219, 22220, 22223, 22224, 22225, 22226, 22227};
	private static final int[] Rewards = {8690, 8692, 8694, 8696, 8698, 8700, 8702, 8704, 8706, 8708, 8710};
	private static final int Dinosaur_Tissue = 8774;
	private static final int Dinosaur_Egg = 8775;
	private static final int Dinosaur_Tissue_Chance = 60;
	private static final int Dinosaur_Egg_Chance = 60;
	
	public _642_APowerfulPrimevalCreature()
	{
		super(true);
		addStartNpc(Dinn);
		addKillId(Ancient_Egg);
		for(int dino_id : Dino)
		{
			addKillId(dino_id);
		}
		addQuestItem(Dinosaur_Tissue);
		addQuestItem(Dinosaur_Egg);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		long Dinosaur_Tissue_Count = st.getQuestItemsCount(Dinosaur_Tissue);
		if(event.equalsIgnoreCase("dindin_q0642_04.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("dindin_q0642_12.htm") && _state == 2)
		{
			if(Dinosaur_Tissue_Count == 0)
			{
				return "dindin_q0642_08a.htm";
			}
			st.takeItems(Dinosaur_Tissue, -1);
			st.giveItems(57, Dinosaur_Tissue_Count * 5000, false);
			st.playSound("ItemSound.quest_middle");
		}
		else
		{
			if(event.equalsIgnoreCase("0"))
			{
				return null;
			}
			if(_state == 2)
			{
				try
				{
					if(Dinosaur_Tissue_Count < 150 || st.getQuestItemsCount(Dinosaur_Egg) == 0)
					{
						return "dindin_q0642_08a.htm";
					}
					int rew_id = Integer.valueOf(event);
					for(int reward : Rewards)
					{
						if(reward != rew_id)
							continue;
						st.takeItems(Dinosaur_Tissue, 150);
						st.takeItems(Dinosaur_Egg, 1);
						st.giveItems(reward, 1, false);
						st.giveItems(57, 44000, false);
						st.playSound("ItemSound.quest_middle");
						return "dindin_q0642_12.htm";
					}
					return null;
				}
				catch(Exception e)
				{
					
				}
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		if(npc.getNpcId() != Dinn)
		{
			return "noquest";
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getLevel() < 75)
			{
				st.exitCurrentQuest(true);
				return "dindin_q0642_01a.htm";
			}
			st.setCond(0);
			return "dindin_q0642_01.htm";
		}
		if(_state == 2)
		{
			long Dinosaur_Tissue_Count = st.getQuestItemsCount(Dinosaur_Tissue);
			if(Dinosaur_Tissue_Count == 0)
			{
				return "dindin_q0642_08a.htm";
			}
			if(Dinosaur_Tissue_Count < 150 || st.getQuestItemsCount(Dinosaur_Egg) == 0)
			{
				return "dindin_q0642_07.htm";
			}
			return "dindin_q0642_07a.htm";
		}
		return "noquest";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2 || st.getCond() != 1)
		{
			return null;
		}
		if(npc.getNpcId() == Ancient_Egg)
		{
			st.rollAndGive(Dinosaur_Egg, 1, (double) Dinosaur_Egg_Chance);
		}
		else
		{
			st.rollAndGive(Dinosaur_Tissue, 1, (double) Dinosaur_Tissue_Chance);
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