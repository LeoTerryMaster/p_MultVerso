package quests;

import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _227_TestOfTheReformer extends Quest implements ScriptFile
{
	private static final int Pupina = 30118;
	private static final int Sla = 30666;
	private static final int Katari = 30668;
	private static final int OlMahumPilgrimNPC = 30732;
	private static final int Kakan = 30669;
	private static final int Nyakuri = 30670;
	private static final int Ramus = 30667;
	private static final int BookOfReform = 2822;
	private static final int LetterOfIntroduction = 2823;
	private static final int SlasLetter = 2824;
	private static final int Greetings = 2825;
	private static final int OlMahumMoney = 2826;
	private static final int KatarisLetter = 2827;
	private static final int NyakurisLetter = 2828;
	private static final int KakansLetter = 3037;
	private static final int UndeadList = 2829;
	private static final int RamussLetter = 2830;
	private static final int RippedDiary = 2831;
	private static final int HugeNail = 2832;
	private static final int LetterOfBetrayer = 2833;
	private static final int BoneFragment1 = 2834;
	private static final int BoneFragment2 = 2835;
	private static final int BoneFragment3 = 2836;
	private static final int BoneFragment4 = 2837;
	private static final int BoneFragment5 = 2838;
	private static final int MarkOfReformer = 2821;
	private static final int NamelessRevenant = 27099;
	private static final int Aruraune = 27128;
	private static final int OlMahumInspector = 27129;
	private static final int OlMahumBetrayer = 27130;
	private static final int CrimsonWerewolf = 27131;
	private static final int KrudelLizardman = 27132;
	private static final int SilentHorror = 20404;
	private static final int SkeletonLord = 20104;
	private static final int SkeletonMarksman = 20102;
	private static final int MiserySkeleton = 20022;
	private static final int SkeletonArcher = 20100;
	public final int[][] DROPLIST_COND = {{18, 0, 20404, 0, 2834, 1, 70, 1}, {18, 0, 20104, 0, 2835, 1, 70, 1}, {18, 0, 20102, 0, 2836, 1, 70, 1}, {18, 0, 20022, 0, 2837, 1, 70, 1}, {18, 0, 20100, 0, 2838, 1, 70, 1}};
	
	public _227_TestOfTheReformer()
	{
		super(false);
		addStartNpc(30118);
		addTalkId(30666);
		addTalkId(30668);
		addTalkId(30732);
		addTalkId(30669);
		addTalkId(30670);
		addTalkId(30667);
		addKillId(27099);
		addKillId(27128);
		addKillId(27129);
		addKillId(27130);
		addKillId(27131);
		addKillId(27132);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
			addQuestItem(DROPLIST_COND[i][4]);
		}
		addQuestItem(2822, 2832, 2823, 2824, 2827, 2833, 2826, 2828, 2829, 2825, 3037, 2830, 2831);
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
		if(event.equalsIgnoreCase("30118-04.htm"))
		{
			st.giveItems(2822, 1);
			if(!st.getPlayer().getVarB("dd3"))
			{
				st.giveItems(7562, 60);
				st.getPlayer().setVar("dd3", "1", -1);
			}
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30118-06.htm"))
		{
			st.takeItems(2832, -1);
			st.takeItems(2822, -1);
			st.giveItems(2823, 1);
			st.setCond(4);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30666-04.htm"))
		{
			st.takeItems(2823, -1);
			st.giveItems(2824, 1);
			st.setCond(5);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30669-03.htm"))
		{
			if(GameObjectsStorage.getByNpcId(27131) == null)
			{
				st.setCond(12);
				st.setState(2);
				st.addSpawn(27131);
				st.startQuestTimer("Wait4", 300000);
			}
			else
			{
				if(!st.isRunningQuestTimer("Wait4"))
				{
					st.startQuestTimer("Wait4", 300000);
				}
				htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
			}
		}
		else if(event.equalsIgnoreCase("30670-03.htm"))
		{
			if(GameObjectsStorage.getByNpcId(27132) == null)
			{
				st.setCond(15);
				st.setState(2);
				st.addSpawn(27132);
				st.startQuestTimer("Wait5", 300000);
			}
			else
			{
				if(!st.isRunningQuestTimer("Wait5"))
				{
					st.startQuestTimer("Wait5", 300000);
				}
				htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
			}
		}
		else
		{
			if(event.equalsIgnoreCase("Wait1"))
			{
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27128);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				if(st.getCond() == 2)
				{
					st.setCond(1);
				}
				return null;
			}
			if(event.equalsIgnoreCase("Wait2"))
			{
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27129);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				if((isQuest = GameObjectsStorage.getByNpcId(30732)) != null)
				{
					isQuest.deleteMe();
				}
				if(st.getCond() == 6)
				{
					st.setCond(5);
				}
				return null;
			}
			if(event.equalsIgnoreCase("Wait3"))
			{
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27130);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				return null;
			}
			if(event.equalsIgnoreCase("Wait4"))
			{
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27131);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				if(st.getCond() == 12)
				{
					st.setCond(11);
				}
				return null;
			}
			if(event.equalsIgnoreCase("Wait5"))
			{
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27132);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				if(st.getCond() == 15)
				{
					st.setCond(14);
				}
				return null;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30118)
		{
			if(st.getQuestItemsCount(2821) != 0)
			{
				htmltext = "completed";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getClassId().getId() == 15 || st.getPlayer().getClassId().getId() == 42)
				{
					if(st.getPlayer().getLevel() >= 39)
					{
						htmltext = "30118-03.htm";
					}
					else
					{
						htmltext = "30118-01.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "30118-02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 3)
			{
				htmltext = "30118-05.htm";
			}
			else if(cond >= 4)
			{
				htmltext = "30118-07.htm";
			}
		}
		else if(npcId == 30666)
		{
			if(cond == 4)
			{
				htmltext = "30666-01.htm";
			}
			else if(cond == 5)
			{
				htmltext = "30666-05.htm";
			}
			else if(cond == 10)
			{
				st.takeItems(2826, -1);
				st.giveItems(2825, 3);
				htmltext = "30666-06.htm";
				st.setCond(11);
				st.setState(2);
			}
			else if(cond == 20)
			{
				st.takeItems(2827, -1);
				st.takeItems(3037, -1);
				st.takeItems(2828, -1);
				st.takeItems(2830, -1);
				st.giveItems(2821, 1);
				if(!st.getPlayer().getVarB("prof2.3"))
				{
					st.addExpAndSp(164032, 17500);
					st.getPlayer().setVar("prof2.3", "1", -1);
				}
				htmltext = "30666-07.htm";
				st.playSound("ItemSound.quest_finish");
				st.exitCurrentQuest(true);
			}
		}
		else if(npcId == 30668)
		{
			if(cond == 5 || cond == 6)
			{
				NpcInstance NPC = GameObjectsStorage.getByNpcId(30732);
				NpcInstance Mob = GameObjectsStorage.getByNpcId(27129);
				if(NPC == null && Mob == null)
				{
					st.takeItems(2824, -1);
					htmltext = "30668-01.htm";
					st.setCond(6);
					st.setState(2);
					st.addSpawn(30732);
					st.addSpawn(27129);
					st.startQuestTimer("Wait2", 300000);
				}
				else
				{
					if(!st.isRunningQuestTimer("Wait2"))
					{
						st.startQuestTimer("Wait2", 300000);
					}
					htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
				}
			}
			else if(cond == 8)
			{
				if(GameObjectsStorage.getByNpcId(27130) == null)
				{
					htmltext = "30668-02.htm";
					st.addSpawn(27130);
					st.startQuestTimer("Wait3", 300000);
				}
				else
				{
					if(!st.isRunningQuestTimer("Wait3"))
					{
						st.startQuestTimer("Wait3", 300000);
					}
					htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
				}
			}
			else if(cond == 9)
			{
				st.takeItems(2833, -1);
				st.giveItems(2827, 1);
				htmltext = "30668-03.htm";
				st.setCond(10);
				st.setState(2);
			}
		}
		else if(npcId == 30732)
		{
			if(cond == 7)
			{
				st.giveItems(2826, 1);
				htmltext = "30732-01.htm";
				st.setCond(8);
				st.setState(2);
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27129);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				if((isQuest = GameObjectsStorage.getByNpcId(30732)) != null)
				{
					isQuest.deleteMe();
				}
				st.cancelQuestTimer("Wait2");
			}
		}
		else if(npcId == 30669)
		{
			if(cond == 11 || cond == 12)
			{
				htmltext = "30669-01.htm";
			}
			else if(cond == 13)
			{
				st.takeItems(2825, 1);
				st.giveItems(3037, 1);
				htmltext = "30669-04.htm";
				st.setCond(14);
				st.setState(2);
			}
		}
		else if(npcId == 30670)
		{
			if(cond == 14 || cond == 15)
			{
				htmltext = "30670-01.htm";
			}
			else if(cond == 16)
			{
				st.takeItems(2825, 1);
				st.giveItems(2828, 1);
				htmltext = "30670-04.htm";
				st.setCond(17);
				st.setState(2);
			}
		}
		else if(npcId == 30667)
		{
			if(cond == 17)
			{
				st.takeItems(2825, -1);
				st.giveItems(2829, 1);
				htmltext = "30667-01.htm";
				st.setCond(18);
				st.setState(2);
			}
			else if(cond == 19)
			{
				st.takeItems(2834, -1);
				st.takeItems(2835, -1);
				st.takeItems(2836, -1);
				st.takeItems(2837, -1);
				st.takeItems(2838, -1);
				st.takeItems(2829, -1);
				st.giveItems(2830, 1);
				htmltext = "30667-03.htm";
				st.setCond(20);
				st.setState(2);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			if(cond != DROPLIST_COND[i][0] || npcId != DROPLIST_COND[i][2] || DROPLIST_COND[i][3] != 0 && st.getQuestItemsCount(DROPLIST_COND[i][3]) <= 0)
				continue;
			if(DROPLIST_COND[i][5] == 0)
			{
				st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], (double) DROPLIST_COND[i][6]);
				continue;
			}
			if(!st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], (double) DROPLIST_COND[i][6]) || DROPLIST_COND[i][1] == cond || DROPLIST_COND[i][1] == 0)
				continue;
			st.setCond(Integer.valueOf(DROPLIST_COND[i][1]).intValue());
			st.setState(2);
		}
		if(cond == 18 && st.getQuestItemsCount(2834) != 0 && st.getQuestItemsCount(2835) != 0 && st.getQuestItemsCount(2836) != 0 && st.getQuestItemsCount(2837) != 0 && st.getQuestItemsCount(2838) != 0)
		{
			st.setCond(19);
			st.setState(2);
		}
		else if(npcId == 27099 && (cond == 1 || cond == 2))
		{
			if(st.getQuestItemsCount(2831) < 6)
			{
				st.giveItems(2831, 1);
			}
			else if(GameObjectsStorage.getByNpcId(27128) == null)
			{
				st.takeItems(2831, -1);
				st.setCond(2);
				st.setState(2);
				st.addSpawn(27128);
				st.startQuestTimer("Wait1", 300000);
			}
			else if(!st.isRunningQuestTimer("Wait1"))
			{
				st.startQuestTimer("Wait1", 300000);
			}
		}
		else if(npcId == 27128)
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27128);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			if(cond == 2)
			{
				if(st.getQuestItemsCount(2832) == 0)
				{
					st.giveItems(2832, 1);
				}
				st.setCond(3);
				st.setState(2);
				st.cancelQuestTimer("Wait1");
			}
		}
		else if(npcId == 27129)
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27129);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait2");
			if(cond == 6)
			{
				st.setCond(7);
				st.setState(2);
			}
		}
		else if(npcId == 27130)
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27130);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait3");
			if(cond == 8)
			{
				if(st.getQuestItemsCount(2833) == 0)
				{
					st.giveItems(2833, 1);
				}
				st.setCond(9);
				st.setState(2);
			}
		}
		else if(npcId == 27131)
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27131);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait4");
			if(cond == 12)
			{
				st.setCond(13);
				st.setState(2);
			}
		}
		else if(npcId == 27132)
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27132);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait5");
			if(cond == 15)
			{
				st.setCond(16);
				st.setState(2);
			}
		}
		return null;
	}
}