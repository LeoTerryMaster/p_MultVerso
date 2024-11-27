package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _273_InvadersOfHolyland extends Quest implements ScriptFile
{
	public final int BLACK_SOULSTONE = 1475;
	public final int RED_SOULSTONE = 1476;
	
	public _273_InvadersOfHolyland()
	{
		super(false);
		addStartNpc(30566);
		addKillId(20311, 20312, 20313);
		addQuestItem(1475, 1476);
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
		if(event.equals("atuba_chief_varkees_q0273_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("atuba_chief_varkees_q0273_07.htm"))
		{
			st.setCond(0);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		else if(event.equals("atuba_chief_varkees_q0273_08.htm"))
		{
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
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.orc)
			{
				htmltext = "atuba_chief_varkees_q0273_00.htm";
				st.exitCurrentQuest(true);
				return htmltext;
			}
			else
			{
				if(st.getPlayer().getLevel() >= 6)
					return "atuba_chief_varkees_q0273_02.htm";
				htmltext = "atuba_chief_varkees_q0273_01.htm";
				st.exitCurrentQuest(true);
			}
			return htmltext;
		}
		else
		{
			if(cond <= 0)
				return htmltext;
			if(st.getQuestItemsCount(1475) == 0 && st.getQuestItemsCount(1476) == 0)
			{
				return "atuba_chief_varkees_q0273_04.htm";
			}
			long adena = 0;
			if(st.getQuestItemsCount(1475) > 0)
			{
				htmltext = "atuba_chief_varkees_q0273_05.htm";
				adena += st.getQuestItemsCount(1475) * 5;
			}
			if(st.getQuestItemsCount(1476) > 0)
			{
				htmltext = "atuba_chief_varkees_q0273_06.htm";
				adena += st.getQuestItemsCount(1476) * 50;
			}
			st.takeAllItems(1475, 1476);
			st.giveItems(57, adena);
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
			st.exitCurrentQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20311)
		{
			if(cond == 1)
			{
				if(Rnd.chance(90))
				{
					st.giveItems(1475, 1);
				}
				else
				{
					st.giveItems(1476, 1);
				}
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20312)
		{
			if(cond == 1)
			{
				if(Rnd.chance(87))
				{
					st.giveItems(1475, 1);
				}
				else
				{
					st.giveItems(1476, 1);
				}
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == 20313 && cond == 1)
		{
			if(Rnd.chance(77))
			{
				st.giveItems(1475, 1);
			}
			else
			{
				st.giveItems(1476, 1);
			}
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
}