package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _106_ForgottenTruth extends Quest implements ScriptFile
{
	int ONYX_TALISMAN1 = 984;
	int ONYX_TALISMAN2 = 985;
	int ANCIENT_SCROLL = 986;
	int ANCIENT_CLAY_TABLET = 987;
	int KARTAS_TRANSLATION = 988;
	int ELDRITCH_DAGGER = 989;
	int ELDRITCH_STAFF = 2373;
	
	public _106_ForgottenTruth()
	{
		super(false);
		addStartNpc(30358);
		addTalkId(30133);
		addKillId(27070);
		addQuestItem(KARTAS_TRANSLATION, ONYX_TALISMAN1, ONYX_TALISMAN2, ANCIENT_SCROLL, ANCIENT_CLAY_TABLET);
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
		if(event.equals("tetrarch_thifiell_q0106_05.htm"))
		{
			st.giveItems(ONYX_TALISMAN1, 1);
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30358)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.darkelf)
				{
					htmltext = "tetrarch_thifiell_q0106_00.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() >= 10)
				{
					htmltext = "tetrarch_thifiell_q0106_03.htm";
				}
				else
				{
					htmltext = "tetrarch_thifiell_q0106_02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond > 0 && (st.getQuestItemsCount(ONYX_TALISMAN1) > 0 || st.getQuestItemsCount(ONYX_TALISMAN2) > 0) && st.getQuestItemsCount(KARTAS_TRANSLATION) == 0)
			{
				htmltext = "tetrarch_thifiell_q0106_06.htm";
			}
			else if(cond == 4 && st.getQuestItemsCount(KARTAS_TRANSLATION) > 0)
			{
				htmltext = "tetrarch_thifiell_q0106_07.htm";
				st.takeItems(KARTAS_TRANSLATION, -1);
				if(st.getPlayer().isMageClass())
				{
					st.giveItems(ELDRITCH_STAFF, 1);
				}
				else
				{
					st.giveItems(ELDRITCH_DAGGER, 1);
				}
				st.giveItems(57, 10266, false);
				st.getPlayer().addExpAndSp(24195, 2074);
				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3"))
				{
					st.getPlayer().setVar("p1q3", "1", -1);
					st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
					st.giveItems(1060, 100);
					for(int item = 4412;item <= 4417;++item)
					{
						st.giveItems(item, 10);
					}
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
				st.exitCurrentQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		else if(npcId == 30133)
		{
			if(cond == 1 && st.getQuestItemsCount(ONYX_TALISMAN1) > 0)
			{
				htmltext = "karta_q0106_01.htm";
				st.takeItems(ONYX_TALISMAN1, -1);
				st.giveItems(ONYX_TALISMAN2, 1);
				st.setCond(2);
			}
			else if(cond == 2 && st.getQuestItemsCount(ONYX_TALISMAN2) > 0 && (st.getQuestItemsCount(ANCIENT_SCROLL) == 0 || st.getQuestItemsCount(ANCIENT_CLAY_TABLET) == 0))
			{
				htmltext = "karta_q0106_02.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(ANCIENT_SCROLL) > 0 && st.getQuestItemsCount(ANCIENT_CLAY_TABLET) > 0)
			{
				htmltext = "karta_q0106_03.htm";
				st.takeItems(ONYX_TALISMAN2, -1);
				st.takeItems(ANCIENT_SCROLL, -1);
				st.takeItems(ANCIENT_CLAY_TABLET, -1);
				st.giveItems(KARTAS_TRANSLATION, 1);
				st.setCond(4);
			}
			else if(cond == 4 && st.getQuestItemsCount(KARTAS_TRANSLATION) > 0)
			{
				htmltext = "karta_q0106_04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == 27070 && st.getCond() == 2 && st.getQuestItemsCount(ONYX_TALISMAN2) > 0)
		{
			if(Rnd.chance(20) && st.getQuestItemsCount(ANCIENT_SCROLL) == 0)
			{
				st.giveItems(ANCIENT_SCROLL, 1);
				st.playSound("ItemSound.quest_middle");
			}
			else if(Rnd.chance(10) && st.getQuestItemsCount(ANCIENT_CLAY_TABLET) == 0)
			{
				st.giveItems(ANCIENT_CLAY_TABLET, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		if(st.getQuestItemsCount(ANCIENT_SCROLL) > 0 && st.getQuestItemsCount(ANCIENT_CLAY_TABLET) > 0)
		{
			st.setCond(3);
		}
		return null;
	}
}