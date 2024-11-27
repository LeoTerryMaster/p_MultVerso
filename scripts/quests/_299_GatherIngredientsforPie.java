package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _299_GatherIngredientsforPie extends Quest implements ScriptFile
{
	private static final int Emily = 30620;
	private static final int Lara = 30063;
	private static final int Bright = 30466;
	private static final int Wasp_Worker = 20934;
	private static final int Wasp_Leader = 20935;
	private static final int Varnish = 1865;
	private static final int Fruit_Basket = 7136;
	private static final int Avellan_Spice = 7137;
	private static final int Honey_Pouch = 7138;
	private static final int Wasp_Worker_Chance = 55;
	private static final int Wasp_Leader_Chance = 70;
	private static final int Reward_Varnish_Chance = 50;
	
	public _299_GatherIngredientsforPie()
	{
		super(false);
		addStartNpc(Emily);
		addTalkId(Lara);
		addTalkId(Bright);
		addKillId(Wasp_Worker);
		addKillId(Wasp_Leader);
		addQuestItem(Fruit_Basket);
		addQuestItem(Avellan_Spice);
		addQuestItem(Honey_Pouch);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		int cond = st.getCond();
		if(event.equalsIgnoreCase("emilly_q0299_0104.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("emilly_q0299_0201.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(Honey_Pouch) < 100)
			{
				return "emilly_q0299_0202.htm";
			}
			st.takeItems(Honey_Pouch, -1);
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("lars_q0299_0301.htm") && _state == 2 && cond == 3)
		{
			st.giveItems(Avellan_Spice, 1);
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("emilly_q0299_0401.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(Avellan_Spice) < 1)
			{
				return "emilly_q0299_0402.htm";
			}
			st.takeItems(Avellan_Spice, -1);
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("guard_bright_q0299_0501.htm") && _state == 2 && cond == 5)
		{
			st.giveItems(Fruit_Basket, 1);
			st.setCond(6);
		}
		else if(event.equalsIgnoreCase("emilly_q0299_0601.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(Fruit_Basket) < 1)
			{
				return "emilly_q0299_0602.htm";
			}
			st.takeItems(Fruit_Basket, -1);
			if(Rnd.chance(Reward_Varnish_Chance))
			{
				st.giveItems(Varnish, 50, true);
			}
			else
			{
				st.giveItems(57, 25000);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int _state = st.getState();
		int npcId = npc.getNpcId();
		if(_state == 1)
		{
			if(npcId != Emily)
			{
				return "noquest";
			}
			if(st.getPlayer().getLevel() >= 34)
			{
				st.setCond(0);
				return "emilly_q0299_0101.htm";
			}
			st.exitCurrentQuest(true);
			return "emilly_q0299_0102.htm";
		}
		int cond = st.getCond();
		if(npcId == Emily && _state == 2)
		{
			if(cond == 1 && st.getQuestItemsCount(Honey_Pouch) <= 99)
			{
				return "emilly_q0299_0106.htm";
			}
			if(cond == 2 && st.getQuestItemsCount(Honey_Pouch) >= 100)
			{
				return "emilly_q0299_0105.htm";
			}
			if(cond == 3 && st.getQuestItemsCount(Avellan_Spice) == 0)
			{
				return "emilly_q0299_0203.htm";
			}
			if(cond == 4 && st.getQuestItemsCount(Avellan_Spice) == 1)
			{
				return "emilly_q0299_0301.htm";
			}
			if(cond == 5 && st.getQuestItemsCount(Fruit_Basket) == 0)
			{
				return "emilly_q0299_0403.htm";
			}
			if(cond == 6 && st.getQuestItemsCount(Fruit_Basket) == 1)
			{
				return "emilly_q0299_0501.htm";
			}
		}
		if(npcId == Lara && _state == 2 && cond == 3)
		{
			return "lars_q0299_0201.htm";
		}
		if(npcId == Lara && _state == 2 && cond == 4)
		{
			return "lars_q0299_0302.htm";
		}
		if(npcId == Bright && _state == 2 && cond == 5)
		{
			return "guard_bright_q0299_0401.htm";
		}
		if(npcId == Bright && _state == 2 && cond == 5)
		{
			return "guard_bright_q0299_0502.htm";
		}
		return "noquest";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2 || qs.getCond() != 1 || qs.getQuestItemsCount(Honey_Pouch) >= 100)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(npcId == Wasp_Worker && Rnd.chance(Wasp_Worker_Chance) || npcId == Wasp_Leader && Rnd.chance(Wasp_Leader_Chance))
		{
			qs.giveItems(Honey_Pouch, 1);
			if(qs.getQuestItemsCount(Honey_Pouch) < 100)
			{
				qs.playSound("ItemSound.quest_itemget");
			}
			else
			{
				qs.setCond(2);
				qs.playSound("ItemSound.quest_middle");
			}
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