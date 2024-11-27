package quests;

import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _276_HestuiTotem extends Quest implements ScriptFile
{
	private static final int Tanapi = 30571;
	private static final int Kasha_Bear = 20479;
	private static final int Kasha_Bear_Totem_Spirit = 27044;
	private static final int Leather_Pants = 29;
	private static final int Totem_of_Hestui = 1500;
	private static final int Kasha_Parasite = 1480;
	private static final int Kasha_Crystal = 1481;
	
	public _276_HestuiTotem()
	{
		super(false);
		addStartNpc(Tanapi);
		addKillId(Kasha_Bear);
		addKillId(Kasha_Bear_Totem_Spirit);
		addQuestItem(Kasha_Parasite);
		addQuestItem(Kasha_Crystal);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("seer_tanapi_q0276_03.htm") && st.getState() == 1 && st.getPlayer().getRace() == Race.orc && st.getPlayer().getLevel() >= 15)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != Tanapi)
		{
			return htmltext;
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getRace() != Race.orc)
			{
				htmltext = "seer_tanapi_q0276_00.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getPlayer().getLevel() < 15)
			{
				htmltext = "seer_tanapi_q0276_01.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "seer_tanapi_q0276_02.htm";
				st.setCond(0);
			}
		}
		else if(_state == 2)
		{
			if(st.getQuestItemsCount(Kasha_Crystal) > 0)
			{
				htmltext = "seer_tanapi_q0276_05.htm";
				st.takeItems(Kasha_Parasite, -1);
				st.takeItems(Kasha_Crystal, -1);
				st.giveItems(Leather_Pants, 1);
				st.giveItems(Totem_of_Hestui, 1);
				if(st.getRateQuestsReward() > 1.0)
				{
					st.giveItems(57, Math.round((double) ItemHolder.getInstance().getTemplate(Totem_of_Hestui).getReferencePrice() * (st.getRateQuestsReward() - 1.0) / 2.0), false);
				}
				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4"))
				{
					st.getPlayer().setVar("p1q4", "1", -1);
					st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				}
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "seer_tanapi_q0276_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(npcId == Kasha_Bear && qs.getQuestItemsCount(Kasha_Crystal) == 0)
		{
			if(qs.getQuestItemsCount(Kasha_Parasite) < 50)
			{
				qs.giveItems(Kasha_Parasite, 1);
				qs.playSound("ItemSound.quest_itemget");
			}
			else
			{
				qs.takeItems(Kasha_Parasite, -1);
				qs.addSpawn(Kasha_Bear_Totem_Spirit);
			}
		}
		else if(npcId == Kasha_Bear_Totem_Spirit && qs.getQuestItemsCount(Kasha_Crystal) == 0)
		{
			qs.giveItems(Kasha_Crystal, 1);
			qs.playSound("ItemSound.quest_middle");
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