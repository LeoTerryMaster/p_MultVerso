package quests;

import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _260_HuntTheOrcs extends Quest implements ScriptFile
{
	private static final int ORC_AMULET = 1114;
	private static final int ORC_NECKLACE = 1115;
	
	public _260_HuntTheOrcs()
	{
		super(false);
		addStartNpc(30221);
		addKillId(20468, 20469, 20470, 20471, 20472, 20473);
		addQuestItem(1114, 1115);
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
		if(event.equals("sentinel_rayjien_q0260_03.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("sentinel_rayjien_q0260_06.htm"))
		{
			st.setCond(0);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
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
		if(npcId == 30221)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 6 && st.getPlayer().getRace() == Race.elf)
				{
					htmltext = "sentinel_rayjien_q0260_02.htm";
					return htmltext;
				}
				if(st.getPlayer().getRace() != Race.elf)
				{
					htmltext = "sentinel_rayjien_q0260_00.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 6)
				{
					htmltext = "sentinel_rayjien_q0260_01.htm";
					st.exitCurrentQuest(true);
				}
				else if(0 == 1 && st.getQuestItemsCount(1114) == 0 && st.getQuestItemsCount(1115) == 0)
				{
					htmltext = "sentinel_rayjien_q0260_04.htm";
				}
			}
			else if(cond == 1 && (st.getQuestItemsCount(1114) > 0 || st.getQuestItemsCount(1115) > 0))
			{
				htmltext = "sentinel_rayjien_q0260_05.htm";
				int adenaPay = 0;
				adenaPay = st.getQuestItemsCount(1114) >= 40 ? (int) ((long) adenaPay + st.getQuestItemsCount(1114) * 14) : (int) ((long) adenaPay + st.getQuestItemsCount(1114) * 12);
				adenaPay = st.getQuestItemsCount(1115) >= 40 ? (int) ((long) adenaPay + st.getQuestItemsCount(1115) * 40) : (int) ((long) adenaPay + st.getQuestItemsCount(1115) * 30);
				st.giveItems(57, (long) adenaPay, false);
				st.takeItems(1114, -1);
				st.takeItems(1115, -1);
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
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() > 0)
		{
			if(npcId == 20468 || npcId == 20469 || npcId == 20470)
			{
				st.rollAndGive(1114, 1, 14.0);
			}
			else if(npcId == 20471 || npcId == 20472 || npcId == 20473)
			{
				st.rollAndGive(1115, 1, 14.0);
			}
		}
		return null;
	}
}