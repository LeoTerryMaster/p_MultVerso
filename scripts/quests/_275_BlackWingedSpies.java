package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _275_BlackWingedSpies extends Quest implements ScriptFile
{
	private static final int Tantus = 30567;
	private static final int Darkwing_Bat = 20316;
	private static final int Varangkas_Tracker = 27043;
	private static final int Darkwing_Bat_Fang = 1478;
	private static final int Varangkas_Parasite = 1479;
	private static final int Varangkas_Parasite_Chance = 10;
	
	public _275_BlackWingedSpies()
	{
		super(false);
		addStartNpc(Tantus);
		addKillId(Darkwing_Bat);
		addKillId(Varangkas_Tracker);
		addQuestItem(Darkwing_Bat_Fang);
		addQuestItem(Varangkas_Parasite);
	}
	
	private static void spawn_Varangkas_Tracker(QuestState st)
	{
		if(st.getQuestItemsCount(Varangkas_Parasite) > 0)
		{
			st.takeItems(Varangkas_Parasite, -1);
		}
		st.giveItems(Varangkas_Parasite, 1);
		st.addSpawn(Varangkas_Tracker);
	}
	
	public static void give_Darkwing_Bat_Fang(QuestState st, long _count)
	{
		long max_inc = 70 - st.getQuestItemsCount(Darkwing_Bat_Fang);
		if(max_inc < 1)
		{
			return;
		}
		if(_count > max_inc)
		{
			_count = max_inc;
		}
		st.giveItems(Darkwing_Bat_Fang, _count);
		st.playSound(_count == max_inc ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
		if(_count == max_inc)
		{
			st.setCond(2);
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("neruga_chief_tantus_q0275_03.htm") && st.getState() == 1)
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
		if(npc.getNpcId() != Tantus)
		{
			return "noquest";
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getRace() != Race.orc)
			{
				st.exitCurrentQuest(true);
				return "neruga_chief_tantus_q0275_00.htm";
			}
			if(st.getPlayer().getLevel() < 11)
			{
				st.exitCurrentQuest(true);
				return "neruga_chief_tantus_q0275_01.htm";
			}
			st.setCond(0);
			return "neruga_chief_tantus_q0275_02.htm";
		}
		if(_state != 2)
		{
			return "noquest";
		}
		int cond = st.getCond();
		if(st.getQuestItemsCount(Darkwing_Bat_Fang) < 70)
		{
			if(cond != 1)
			{
				st.setCond(1);
			}
			return "neruga_chief_tantus_q0275_04.htm";
		}
		if(cond == 2)
		{
			st.giveItems(57, 4550);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
			return "neruga_chief_tantus_q0275_05.htm";
		}
		return "noquest";
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		long Darkwing_Bat_Fang_count = qs.getQuestItemsCount(Darkwing_Bat_Fang);
		if(npcId == Darkwing_Bat && Darkwing_Bat_Fang_count < 70)
		{
			if(Darkwing_Bat_Fang_count > 10 && Darkwing_Bat_Fang_count < 65 && Rnd.chance(Varangkas_Parasite_Chance))
			{
				spawn_Varangkas_Tracker(qs);
				return null;
			}
			give_Darkwing_Bat_Fang(qs, 1);
		}
		else if(npcId == Varangkas_Tracker && Darkwing_Bat_Fang_count < 70 && qs.getQuestItemsCount(Varangkas_Parasite) > 0)
		{
			qs.takeItems(Varangkas_Parasite, -1);
			give_Darkwing_Bat_Fang(qs, 5);
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