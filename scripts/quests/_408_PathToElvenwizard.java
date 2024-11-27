package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _408_PathToElvenwizard extends Quest implements ScriptFile
{
	public final int GREENIS = 30157;
	public final int THALIA = 30371;
	public final int ROSELLA = 30414;
	public final int NORTHWIND = 30423;
	public final int DRYAD_ELDER = 20019;
	public final int PINCER_SPIDER = 20466;
	public final int SUKAR_WERERAT_LEADER = 20047;
	public final int ROGELLIAS_LETTER_ID = 1218;
	public final int RED_DOWN_ID = 1219;
	public final int MAGICAL_POWERS_RUBY_ID = 1220;
	public final int PURE_AQUAMARINE_ID = 1221;
	public final int APPETIZING_APPLE_ID = 1222;
	public final int GOLD_LEAVES_ID = 1223;
	public final int IMMORTAL_LOVE_ID = 1224;
	public final int AMETHYST_ID = 1225;
	public final int NOBILITY_AMETHYST_ID = 1226;
	public final int FERTILITY_PERIDOT_ID = 1229;
	public final int ETERNITY_DIAMOND_ID = 1230;
	public final int CHARM_OF_GRAIN_ID = 1272;
	public final int SAP_OF_WORLD_TREE_ID = 1273;
	public final int LUCKY_POTPOURI_ID = 1274;
	
	public _408_PathToElvenwizard()
	{
		super(false);
		addStartNpc(30414);
		addTalkId(30157);
		addTalkId(30371);
		addTalkId(30423);
		addKillId(20019);
		addKillId(20466);
		addKillId(20047);
		addQuestItem(1218, 1229, 1224, 1222, 1272, 1220, 1273, 1221, 1274, 1226, 1223, 1219, 1225);
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
		if(event.equalsIgnoreCase("1"))
		{
			if(st.getPlayer().getClassId().getId() != 25)
			{
				htmltext = st.getPlayer().getClassId().getId() == 26 ? "rogellia_q0408_02a.htm" : "rogellia_q0408_03.htm";
			}
			else if(st.getPlayer().getLevel() < 18)
			{
				htmltext = "rogellia_q0408_04.htm";
			}
			else if(st.getQuestItemsCount(1230) > 0)
			{
				htmltext = "rogellia_q0408_05.htm";
			}
			else
			{
				st.setState(2);
				st.setCond(1);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(1229, 1);
				htmltext = "rogellia_q0408_06.htm";
			}
		}
		else if(event.equalsIgnoreCase("408_1"))
		{
			if(st.getQuestItemsCount(1220) > 0)
			{
				htmltext = "rogellia_q0408_10.htm";
			}
			else if(st.getQuestItemsCount(1220) < 1 && st.getQuestItemsCount(1229) > 0)
			{
				st.giveItems(1218, 1);
				htmltext = "rogellia_q0408_07.htm";
			}
		}
		else if(event.equalsIgnoreCase("408_4"))
		{
			if(st.getQuestItemsCount(1218) > 0)
			{
				st.takeItems(1218, -1);
				st.giveItems(1272, 1);
				htmltext = "grain_q0408_02.htm";
			}
		}
		else if(event.equalsIgnoreCase("408_2"))
		{
			if(st.getQuestItemsCount(1221) > 0)
			{
				htmltext = "rogellia_q0408_13.htm";
			}
			else if(st.getQuestItemsCount(1221) < 1 && st.getQuestItemsCount(1229) > 0)
			{
				st.giveItems(1222, 1);
				htmltext = "rogellia_q0408_14.htm";
			}
		}
		else if(event.equalsIgnoreCase("408_5"))
		{
			if(st.getQuestItemsCount(1222) > 0)
			{
				st.takeItems(1222, -1);
				st.giveItems(1273, 1);
				htmltext = "thalya_q0408_02.htm";
			}
		}
		else if(event.equalsIgnoreCase("408_3"))
		{
			if(st.getQuestItemsCount(1226) > 0)
			{
				htmltext = "rogellia_q0408_17.htm";
			}
			else if(st.getQuestItemsCount(1226) < 1 && st.getQuestItemsCount(1229) > 0)
			{
				st.giveItems(1224, 1);
				htmltext = "rogellia_q0408_18.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30414)
		{
			if(cond < 1)
			{
				htmltext = "rogellia_q0408_01.htm";
			}
			else if(st.getQuestItemsCount(1272) > 0)
			{
				if(st.getQuestItemsCount(1219) < 5)
				{
					htmltext = "rogellia_q0408_09.htm";
				}
				else if(st.getQuestItemsCount(1219) > 4)
				{
					htmltext = "rogellia_q0408_25.htm";
				}
				else if(st.getQuestItemsCount(1223) > 4)
				{
					htmltext = "rogellia_q0408_26.htm";
				}
			}
			else if(st.getQuestItemsCount(1222) > 0)
			{
				htmltext = "rogellia_q0408_15.htm";
			}
			else if(st.getQuestItemsCount(1224) > 0)
			{
				htmltext = "rogellia_q0408_19.htm";
			}
			else if(st.getQuestItemsCount(1273) > 0 && st.getQuestItemsCount(1223) < 5)
			{
				htmltext = "rogellia_q0408_16.htm";
			}
			else if(st.getQuestItemsCount(1274) > 0)
			{
				htmltext = st.getQuestItemsCount(1225) < 2 ? "rogellia_q0408_20.htm" : "rogellia_q0408_27.htm";
			}
			else if(st.getQuestItemsCount(1218) > 0)
			{
				htmltext = "rogellia_q0408_08.htm";
			}
			else if(st.getQuestItemsCount(1218) < 1 && st.getQuestItemsCount(1222) < 1 && st.getQuestItemsCount(1224) < 1 && st.getQuestItemsCount(1272) < 1 && st.getQuestItemsCount(1273) < 1 && st.getQuestItemsCount(1274) < 1 && st.getQuestItemsCount(1229) > 0)
			{
				if(st.getQuestItemsCount(1220) < 1 | st.getQuestItemsCount(1226) < 1 | st.getQuestItemsCount(1221) < 1)
				{
					htmltext = "rogellia_q0408_11.htm";
				}
				else if(st.getQuestItemsCount(1220) > 0 && st.getQuestItemsCount(1226) > 0 && st.getQuestItemsCount(1221) > 0)
				{
					st.takeItems(1220, -1);
					st.takeItems(1221, st.getQuestItemsCount(1221));
					st.takeItems(1226, st.getQuestItemsCount(1226));
					st.takeItems(1229, st.getQuestItemsCount(1229));
					htmltext = "rogellia_q0408_24.htm";
					if(st.getPlayer().getClassId().getLevel() == 1)
					{
						st.giveItems(1230, 1);
						if(!st.getPlayer().getVarB("prof1"))
						{
							st.getPlayer().setVar("prof1", "1", -1);
							st.addExpAndSp(3200, 1890);
						}
					}
					st.playSound("ItemSound.quest_finish");
					st.exitCurrentQuest(true);
				}
			}
		}
		else if(npcId == 30157 && cond > 0)
		{
			if(st.getQuestItemsCount(1218) > 0)
			{
				htmltext = "grain_q0408_01.htm";
			}
			else if(st.getQuestItemsCount(1272) > 0)
			{
				if(st.getQuestItemsCount(1219) < 5)
				{
					htmltext = "grain_q0408_03.htm";
				}
				else
				{
					st.takeItems(1219, -1);
					st.takeItems(1272, -1);
					st.giveItems(1220, 1);
					htmltext = "grain_q0408_04.htm";
				}
			}
		}
		else if(npcId == 30371 && cond > 0)
		{
			if(st.getQuestItemsCount(1222) > 0)
			{
				htmltext = "thalya_q0408_01.htm";
			}
			else if(st.getQuestItemsCount(1273) > 0)
			{
				if(st.getQuestItemsCount(1223) < 5)
				{
					htmltext = "thalya_q0408_03.htm";
				}
				else
				{
					st.takeItems(1223, -1);
					st.takeItems(1273, -1);
					st.giveItems(1221, 1);
					htmltext = "thalya_q0408_04.htm";
				}
			}
		}
		else if(npcId == 30423 && cond > 0)
		{
			if(st.getQuestItemsCount(1224) > 0)
			{
				st.takeItems(1224, -1);
				st.giveItems(1274, 1);
				htmltext = "northwindel_q0408_01.htm";
			}
			else if(st.getQuestItemsCount(1274) > 0)
			{
				if(st.getQuestItemsCount(1225) < 2)
				{
					htmltext = "northwindel_q0408_02.htm";
				}
				else
				{
					st.takeItems(1225, -1);
					st.takeItems(1274, -1);
					st.giveItems(1226, 1);
					htmltext = "northwindel_q0408_03.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20466)
		{
			if(cond > 0 && st.getQuestItemsCount(1272) > 0 && st.getQuestItemsCount(1219) < 5 && Rnd.chance(70))
			{
				st.giveItems(1219, 1);
				if(st.getQuestItemsCount(1219) < 5)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 20019)
		{
			if(cond > 0 && st.getQuestItemsCount(1273) > 0 && st.getQuestItemsCount(1223) < 5 && Rnd.chance(40))
			{
				st.giveItems(1223, 1);
				if(st.getQuestItemsCount(1223) < 5)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if(npcId == 20047 && cond > 0 && st.getQuestItemsCount(1274) > 0 && st.getQuestItemsCount(1225) < 2 && Rnd.chance(40))
		{
			st.giveItems(1225, 1);
			if(st.getQuestItemsCount(1225) < 2)
			{
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
}