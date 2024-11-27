package quests;

import l2.commons.util.Rnd;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;

public class _021_HiddenTruth extends Quest implements ScriptFile
{
	public static final int DARIN = 30048;
	public static final int ROXXY = 30006;
	public static final int BAULRO = 30033;
	public static final int MysteriousWizard = 31522;
	public static final int Tombstone = 31523;
	public static final int GhostofvonHellmannId = 31524;
	public static final int GhostofvonHellmannsPageId = 31525;
	public static final int BrokenBookshelf = 31526;
	public static final int Agripel = 31348;
	public static final int Dominic = 31350;
	public static final int Benedict = 31349;
	public static final int Innocentin = 31328;
	public static final int CrossofEinhasad = 7140;
	public static final int CrossofEinhasadNextQuest = 7141;
	public NpcInstance GhostofvonHellmannsPage;
	public NpcInstance GhostofvonHellmann;
	
	public _021_HiddenTruth()
	{
		super(false);
		addStartNpc(31522);
		addTalkId(31523);
		addTalkId(31524);
		addTalkId(31525);
		addTalkId(31526);
		addTalkId(31348);
		addTalkId(31350);
		addTalkId(31349);
		addTalkId(31328);
	}
	
	private void spawnGhostofvonHellmannsPage()
	{
		GhostofvonHellmannsPage = Functions.spawn(new Location(51462, -54539, -3176), 31525);
	}
	
	private void despawnGhostofvonHellmannsPage()
	{
		if(GhostofvonHellmannsPage != null)
		{
			GhostofvonHellmannsPage.deleteMe();
		}
		GhostofvonHellmannsPage = null;
	}
	
	private void spawnGhostofvonHellmann()
	{
		GhostofvonHellmann = Functions.spawn(Location.findPointToStay(new Location(51432, -54570, -3136), 50, ReflectionManager.DEFAULT.getGeoIndex()), 31524);
	}
	
	private void despawnGhostofvonHellmann()
	{
		if(GhostofvonHellmann != null)
		{
			GhostofvonHellmann.deleteMe();
		}
		GhostofvonHellmann = null;
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
		if(event.equalsIgnoreCase("31522-02.htm"))
		{
			st.setState(2);
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("html"))
		{
			htmltext = "31328-05.htm";
		}
		else if(event.equalsIgnoreCase("31328-05.htm"))
		{
			st.unset("cond");
			st.takeItems(7140, -1);
			if(st.getQuestItemsCount(7141) == 0)
			{
				st.giveItems(7141, 1);
			}
			st.playSound("ItemSound.quest_finish");
			st.startQuestTimer("html", 1);
			htmltext = "Congratulations! You are completed this quest!<br>The Quest \"Tragedy In Von Hellmann Forest\" become available.<br>Show Cross of Einhasad to High Priest Tifaren.";
			st.exitCurrentQuest(false);
		}
		else if(event.equalsIgnoreCase("31523-03.htm"))
		{
			st.playSound("SkillSound5.horror_02");
			st.setCond(2);
			despawnGhostofvonHellmann();
			spawnGhostofvonHellmann();
		}
		else if(event.equalsIgnoreCase("31524-06.htm"))
		{
			st.setCond(3);
			despawnGhostofvonHellmannsPage();
			spawnGhostofvonHellmannsPage();
		}
		else if(event.equalsIgnoreCase("31526-03.htm"))
		{
			st.playSound("ItemSound.item_drop_equip_armor_cloth");
		}
		else if(event.equalsIgnoreCase("31526-08.htm"))
		{
			st.playSound("AmdSound.ed_chimes_05");
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("31526-14.htm"))
		{
			st.giveItems(7140, 1);
			st.setCond(6);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "no-quest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31522)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() > 54)
				{
					htmltext = "31522-01.htm";
				}
				else
				{
					htmltext = "31522-03.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "31522-05.htm";
			}
		}
		else if(npcId == 31523)
		{
			if(cond == 1)
			{
				htmltext = "31523-01.htm";
			}
			else if(cond == 2 || cond == 3)
			{
				htmltext = "31523-04.htm";
				st.playSound("SkillSound5.horror_02");
				despawnGhostofvonHellmann();
				spawnGhostofvonHellmann();
			}
		}
		else if(npcId == 31524)
		{
			if(cond == 2)
			{
				htmltext = "31524-01.htm";
			}
			else if(cond == 3)
			{
				htmltext = "31524-07b.htm";
			}
			else if(cond == 4)
			{
				htmltext = "31524-07c.htm";
			}
		}
		else if(npcId == 31525)
		{
			if(cond == 3 || cond == 4)
			{
				htmltext = "31525-01.htm";
				if(GhostofvonHellmannsPage == null || !GhostofvonHellmannsPage.isMoving())
				{
					htmltext = "31525-02.htm";
					if(cond == 3)
					{
						st.setCond(4);
					}
					despawnGhostofvonHellmannsPage();
				}
			}
		}
		else if(npcId == 31526)
		{
			if(cond == 4 || cond == 3)
			{
				despawnGhostofvonHellmannsPage();
				despawnGhostofvonHellmann();
				st.setCond(5);
				htmltext = "31526-01.htm";
			}
			else if(cond == 5)
			{
				htmltext = "31526-10.htm";
				st.playSound("AmdSound.ed_chimes_05");
			}
			else if(cond == 6)
			{
				htmltext = "31526-15.htm";
			}
		}
		else if(npcId == 31348 && st.getQuestItemsCount(7140) >= 1)
		{
			if(cond == 6)
			{
				if(st.getInt("DOMINIC") == 1 && st.getInt("BENEDICT") == 1)
				{
					htmltext = "31348-02.htm";
					st.setCond(7);
				}
				else
				{
					st.set("AGRIPEL", "1");
					htmltext = "31348-0" + Rnd.get(3) + ".htm";
				}
			}
			else if(cond == 7)
			{
				htmltext = "31348-03.htm";
			}
		}
		else if(npcId == 31350 && st.getQuestItemsCount(7140) >= 1)
		{
			if(cond == 6)
			{
				if(st.getInt("AGRIPEL") == 1 && st.getInt("BENEDICT") == 1)
				{
					htmltext = "31350-02.htm";
					st.setCond(7);
				}
				else
				{
					st.set("DOMINIC", "1");
					htmltext = "31350-0" + Rnd.get(3) + ".htm";
				}
			}
			else if(cond == 7)
			{
				htmltext = "31350-03.htm";
			}
		}
		else if(npcId == 31349 && st.getQuestItemsCount(7140) >= 1)
		{
			if(cond == 6)
			{
				if(st.getInt("AGRIPEL") == 1 && st.getInt("DOMINIC") == 1)
				{
					htmltext = "31349-02.htm";
					st.setCond(7);
				}
				else
				{
					st.set("BENEDICT", "1");
					htmltext = "31349-0" + Rnd.get(3) + ".htm";
				}
			}
			else if(cond == 7)
			{
				htmltext = "31349-03.htm";
			}
		}
		else if(npcId == 31328)
		{
			if(cond == 7)
			{
				if(st.getQuestItemsCount(7140) != 0)
				{
					htmltext = "31328-01.htm";
				}
			}
			else if(cond == 0)
			{
				htmltext = "31328-06.htm";
			}
		}
		return htmltext;
	}
}