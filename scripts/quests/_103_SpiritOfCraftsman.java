package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.base.Race;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2.gameserver.scripts.ScriptFile;

public class _103_SpiritOfCraftsman extends Quest implements ScriptFile
{
	public final int KAROYDS_LETTER_ID = 968;
	public final int CECKTINONS_VOUCHER1_ID = 969;
	public final int CECKTINONS_VOUCHER2_ID = 970;
	public final int BONE_FRAGMENT1_ID = 1107;
	public final int SOUL_CATCHER_ID = 971;
	public final int PRESERVE_OIL_ID = 972;
	public final int ZOMBIE_HEAD_ID = 973;
	public final int STEELBENDERS_HEAD_ID = 974;
	public final int BLOODSABER_ID = 975;
	
	public _103_SpiritOfCraftsman()
	{
		super(false);
		addStartNpc(30307);
		addTalkId(30132);
		addTalkId(30144);
		addKillId(20015);
		addKillId(20020);
		addKillId(20455);
		addKillId(20517);
		addKillId(20518);
		addQuestItem(968, 969, 970, 1107, 971, 972, 973, 974);
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
		if(event.equalsIgnoreCase("blacksmith_karoyd_q0103_05.htm"))
		{
			st.giveItems(968, 1);
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
		int id = st.getState();
		if(id == 1)
		{
			st.setCond(0);
		}
		String htmltext = "noquest";
		if(npcId == 30307 && st.getCond() == 0)
		{
			if(st.getPlayer().getRace() != Race.darkelf)
			{
				htmltext = "blacksmith_karoyd_q0103_00.htm";
			}
			else
			{
				if(st.getPlayer().getLevel() >= 10)
				{
					htmltext = "blacksmith_karoyd_q0103_03.htm";
					return htmltext;
				}
				htmltext = "blacksmith_karoyd_q0103_02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30307 && st.getCond() == 0)
		{
			htmltext = "completed";
		}
		else if(id == 2)
		{
			if(npcId == 30307 && st.getCond() >= 1 && (st.getQuestItemsCount(968) >= 1 || st.getQuestItemsCount(969) >= 1 || st.getQuestItemsCount(970) >= 1))
			{
				htmltext = "blacksmith_karoyd_q0103_06.htm";
			}
			else if(npcId == 30132 && st.getCond() == 1 && st.getQuestItemsCount(968) == 1)
			{
				htmltext = "cecon_q0103_01.htm";
				st.setCond(2);
				st.takeItems(968, 1);
				st.giveItems(969, 1);
			}
			else if(npcId == 30132 && st.getCond() >= 2 && (st.getQuestItemsCount(969) >= 1 || st.getQuestItemsCount(970) >= 1))
			{
				htmltext = "cecon_q0103_02.htm";
			}
			else if(npcId == 30144 && st.getCond() == 2 && st.getQuestItemsCount(969) >= 1)
			{
				htmltext = "harne_q0103_01.htm";
				st.setCond(3);
				st.takeItems(969, 1);
				st.giveItems(970, 1);
			}
			else if(npcId == 30144 && st.getCond() == 3 && st.getQuestItemsCount(970) >= 1 && st.getQuestItemsCount(1107) < 10)
			{
				htmltext = "harne_q0103_02.htm";
			}
			else if(npcId == 30144 && st.getCond() == 4 && st.getQuestItemsCount(970) == 1 && st.getQuestItemsCount(1107) >= 10)
			{
				htmltext = "harne_q0103_03.htm";
				st.setCond(5);
				st.takeItems(970, 1);
				st.takeItems(1107, 10);
				st.giveItems(971, 1);
			}
			else if(npcId == 30144 && st.getCond() == 5 && st.getQuestItemsCount(971) == 1)
			{
				htmltext = "harne_q0103_04.htm";
			}
			else if(npcId == 30132 && st.getCond() == 5 && st.getQuestItemsCount(971) == 1)
			{
				htmltext = "cecon_q0103_03.htm";
				st.setCond(6);
				st.takeItems(971, 1);
				st.giveItems(972, 1);
			}
			else if(npcId == 30132 && st.getCond() == 6 && st.getQuestItemsCount(972) == 1 && st.getQuestItemsCount(973) == 0 && st.getQuestItemsCount(974) == 0)
			{
				htmltext = "cecon_q0103_04.htm";
			}
			else if(npcId == 30132 && st.getCond() == 7 && st.getQuestItemsCount(973) == 1)
			{
				htmltext = "cecon_q0103_05.htm";
				st.setCond(8);
				st.takeItems(973, 1);
				st.giveItems(974, 1);
			}
			else if(npcId == 30132 && st.getCond() == 8 && st.getQuestItemsCount(974) == 1)
			{
				htmltext = "cecon_q0103_06.htm";
			}
			else if(npcId == 30307 && st.getCond() == 8 && st.getQuestItemsCount(974) == 1)
			{
				htmltext = "blacksmith_karoyd_q0103_07.htm";
				st.takeItems(974, 1);
				st.giveItems(975, 1);
				st.giveItems(57, 19799, false);
				st.getPlayer().addExpAndSp(46663, 3999);
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
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if((npcId == 20517 || npcId == 20518 || npcId == 20455) && st.getCond() == 3)
		{
			if(st.getQuestItemsCount(970) == 1 && st.getQuestItemsCount(1107) < 10 && Rnd.chance(33))
			{
				st.giveItems(1107, 1);
				if(st.getQuestItemsCount(1107) == 10)
				{
					st.playSound("ItemSound.quest_middle");
					st.setCond(4);
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if((npcId == 20015 || npcId == 20020) && st.getCond() == 6 && st.getQuestItemsCount(972) == 1 && Rnd.chance(33))
		{
			st.giveItems(973, 1);
			st.playSound("ItemSound.quest_middle");
			st.takeItems(972, 1);
			st.setCond(7);
		}
		return null;
	}
}