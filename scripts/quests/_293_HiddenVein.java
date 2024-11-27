package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _293_HiddenVein extends Quest implements ScriptFile
{
	private static final int Filaur = 30535;
	private static final int Chichirin = 30539;
	private static final int Utuku_Orc = 20446;
	private static final int Utuku_Orc_Archer = 20447;
	private static final int Utuku_Orc_Grunt = 20448;
	private static final int Chrysolite_Ore = 1488;
	private static final int Torn_Map_Fragment = 1489;
	private static final int Hidden_Ore_Map = 1490;
	private static final int Torn_Map_Fragment_Chance = 5;
	private static final int Chrysolite_Ore_Chance = 45;
	
	public _293_HiddenVein()
	{
		super(false);
		addStartNpc(Filaur);
		addTalkId(Chichirin);
		addKillId(Utuku_Orc);
		addKillId(Utuku_Orc_Archer);
		addKillId(Utuku_Orc_Grunt);
		addQuestItem(Chrysolite_Ore);
		addQuestItem(Torn_Map_Fragment);
		addQuestItem(Hidden_Ore_Map);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("elder_filaur_q0293_03.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("elder_filaur_q0293_06.htm") && _state == 2)
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("chichirin_q0293_03.htm") && _state == 2)
		{
			if(st.getQuestItemsCount(Torn_Map_Fragment) < 4)
			{
				return "chichirin_q0293_02.htm";
			}
			st.takeItems(Torn_Map_Fragment, 4);
			st.giveItems(Hidden_Ore_Map, 1);
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
			if(npcId != Filaur)
			{
				return "noquest";
			}
			if(st.getPlayer().getRace() != Race.dwarf)
			{
				st.exitCurrentQuest(true);
				return "elder_filaur_q0293_00.htm";
			}
			if(st.getPlayer().getLevel() < 6)
			{
				st.exitCurrentQuest(true);
				return "elder_filaur_q0293_01.htm";
			}
			st.setCond(0);
			return "elder_filaur_q0293_02.htm";
		}
		if(_state != 2)
		{
			return "noquest";
		}
		if(npcId == Filaur)
		{
			long Chrysolite_Ore_count = st.getQuestItemsCount(Chrysolite_Ore);
			long Hidden_Ore_Map_count = st.getQuestItemsCount(Hidden_Ore_Map);
			long reward = st.getQuestItemsCount(Chrysolite_Ore) * 10 + st.getQuestItemsCount(Hidden_Ore_Map) * 1000;
			if(reward == 0)
			{
				return "elder_filaur_q0293_04.htm";
			}
			if(Chrysolite_Ore_count > 0)
			{
				st.takeItems(Chrysolite_Ore, -1);
			}
			if(Hidden_Ore_Map_count > 0)
			{
				st.takeItems(Hidden_Ore_Map, -1);
			}
			st.giveItems(57, reward);
			if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q2"))
			{
				st.getPlayer().setVar("p1q2", "1", -1);
				st.getPlayer().sendPacket(new ExShowScreenMessage("Acquisition of Soulshot for beginners complete.\n                  Go find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
				QuestState qs = st.getPlayer().getQuestState(_255_Tutorial.class);
				if(qs != null && qs.getInt("Ex") != 10)
				{
					st.showQuestionMark(26);
					qs.set("Ex", "10");
					if(st.getPlayer().getClassId().isMage())
					{
						st.playTutorialVoice("tutorial_voice_027");
						st.giveItems(5790, 3000);
					}
					else
					{
						st.playTutorialVoice("tutorial_voice_026");
						st.giveItems(5789, 6000);
					}
				}
			}
			return Chrysolite_Ore_count > 0 && Hidden_Ore_Map_count > 0 ? "elder_filaur_q0293_09.htm" : Hidden_Ore_Map_count > 0 ? "elder_filaur_q0293_08.htm" : "elder_filaur_q0293_05.htm";
		}
		if(npcId == Chichirin)
		{
			return "chichirin_q0293_01.htm";
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
		if(Rnd.chance(Torn_Map_Fragment_Chance))
		{
			qs.giveItems(Torn_Map_Fragment, 1);
			qs.playSound("ItemSound.quest_itemget");
		}
		else if(Rnd.chance(Chrysolite_Ore_Chance))
		{
			qs.giveItems(Chrysolite_Ore, 1);
			qs.playSound("ItemSound.quest_itemget");
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