package quests;

import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _220_TestimonyOfGlory extends Quest implements ScriptFile
{
	private static final int Vokian = 30514;
	private static final int Chianta = 30642;
	private static final int Manakia = 30515;
	private static final int Kasman = 30501;
	private static final int Voltar = 30615;
	private static final int Kepra = 30616;
	private static final int Burai = 30617;
	private static final int Harak = 30618;
	private static final int Driko = 30619;
	private static final int Tanapi = 30571;
	private static final int Kakai = 30565;
	private static final int VokiansOrder = 3204;
	private static final int ManashenShard = 3205;
	private static final int TyrantTalon = 3206;
	private static final int GuardianBasiliskFang = 3207;
	private static final int VokiansOrder2 = 3208;
	private static final int NecklaceOfAuthority = 3209;
	private static final int ChiantaOrder1st = 3210;
	private static final int ScepterOfBreka = 3211;
	private static final int ScepterOfEnku = 3212;
	private static final int ScepterOfVuku = 3213;
	private static final int ScepterOfTurek = 3214;
	private static final int ScepterOfTunath = 3215;
	private static final int ChiantasOrder2rd = 3216;
	private static final int ChiantasOrder3rd = 3217;
	private static final int TamlinOrcSkull = 3218;
	private static final int TimakOrcHead = 3219;
	private static final int ScepterBox = 3220;
	private static final int PashikasHead = 3221;
	private static final int VultusHead = 3222;
	private static final int GloveOfVoltar = 3223;
	private static final int EnkuOverlordHead = 3224;
	private static final int GloveOfKepra = 3225;
	private static final int MakumBugbearHead = 3226;
	private static final int GloveOfBurai = 3227;
	private static final int ManakiaLetter1st = 3228;
	private static final int ManakiaLetter2st = 3229;
	private static final int KasmansLetter1rd = 3230;
	private static final int KasmansLetter2rd = 3231;
	private static final int KasmansLetter3rd = 3232;
	private static final int DrikosContract = 3233;
	private static final int StakatoDroneHusk = 3234;
	private static final int TanapisOrder = 3235;
	private static final int ScepterOfTantos = 3236;
	private static final int RitualBox = 3237;
	private static final int MarkOfGlory = 3203;
	private static final int Tyrant = 20192;
	private static final int TyrantKingpin = 20193;
	private static final int GuardianBasilisk = 20550;
	private static final int ManashenGargoyle = 20563;
	private static final int MarshStakatoDrone = 20234;
	private static final int PashikasSonOfVoltarQuestMonster = 27080;
	private static final int VultusSonOfVoltarQuestMonster = 27081;
	private static final int EnkuOrcOverlordQuestMonster = 27082;
	private static final int MakumBugbearThugQuestMonster = 27083;
	private static final int TimakOrc = 20583;
	private static final int TimakOrcArcher = 20584;
	private static final int TimakOrcSoldier = 20585;
	private static final int TimakOrcWarrior = 20586;
	private static final int TimakOrcShaman = 20587;
	private static final int TimakOrcOverlord = 20588;
	private static final int TamlinOrc = 20601;
	private static final int TamlinOrcArcher = 20602;
	private static final int RagnaOrcOverlord = 20778;
	private static final int RagnaOrcSeer = 20779;
	private static final int RevenantOfTantosChief = 27086;
	private static final int[][] DROPLIST_COND = {{1, 0, 20563, 3204, 3205, 10, 70, 1}, {1, 0, 20192, 3204, 3206, 10, 70, 1}, {1, 0, 20193, 3204, 3206, 10, 70, 1}, {1, 0, 20550, 3204, 3207, 10, 70, 1}, {4, 0, 20234, 3233, 3234, 30, 70, 1}, {4, 0, 27082, 3225, 3224, 4, 100, 1}, {4, 0, 27083, 3227, 3226, 2, 100, 1}, {6, 0, 20583, 3217, 3219, 20, 50, 1}, {6, 0, 20584, 3217, 3219, 20, 60, 1}, {6, 0, 20585, 3217, 3219, 20, 70, 1}, {6, 0, 20586, 3217, 3219, 20, 80, 1}, {6, 0, 20587, 3217, 3219, 20, 90, 1}, {6, 0, 20588, 3217, 3219, 20, 100, 1}, {6, 0, 20601, 3217, 3218, 20, 50, 1}, {6, 0, 20602, 3217, 3218, 20, 60, 1}};
	
	public _220_TestimonyOfGlory()
	{
		super(false);
		addStartNpc(30514);
		addTalkId(30642);
		addTalkId(30515);
		addTalkId(30501);
		addTalkId(30615);
		addTalkId(30616);
		addTalkId(30617);
		addTalkId(30618);
		addTalkId(30619);
		addTalkId(30571);
		addTalkId(30565);
		for(int i = 0;i < DROPLIST_COND.length;++i)
		{
			addKillId(DROPLIST_COND[i][2]);
		}
		addKillId(27080);
		addKillId(27081);
		addKillId(20778);
		addKillId(20779);
		addKillId(27086);
		addQuestItem(3204, 3208, 3209, 3210, 3228, 3229, 3230, 3231, 3232, 3211, 3221, 3222, 3223, 3225, 3212, 3214, 3227, 3215, 3233, 3216, 3217, 3220, 3235, 3236, 3237, 3205, 3206, 3207, 3234, 3224, 3226, 3219, 3218);
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
		if(event.equalsIgnoreCase("RETURN"))
		{
			return null;
		}
		String htmltext = event;
		if(event.equalsIgnoreCase("30514-05.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.giveItems(3204, 1);
			if(!st.getPlayer().getVarB("dd2"))
			{
				st.giveItems(7562, 102);
				st.getPlayer().setVar("dd2", "1", -1);
			}
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30642-03.htm"))
		{
			st.takeItems(3208, -1);
			st.giveItems(3210, 1);
			st.setCond(4);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30571-03.htm"))
		{
			st.takeItems(3220, -1);
			st.giveItems(3235, 1);
			st.setCond(9);
			st.setState(2);
		}
		else if(event.equalsIgnoreCase("30642-07.htm"))
		{
			st.takeItems(3211, -1);
			st.takeItems(3212, -1);
			st.takeItems(3213, -1);
			st.takeItems(3214, -1);
			st.takeItems(3215, -1);
			st.takeItems(3210, -1);
			if(st.getPlayer().getLevel() >= 38)
			{
				st.giveItems(3217, 1);
				st.setCond(6);
				st.setState(2);
			}
			else
			{
				htmltext = "30642-06.htm";
				st.giveItems(3216, 1);
			}
		}
		else if(event.equalsIgnoreCase("BREKA"))
		{
			if(st.getQuestItemsCount(3211) > 0)
			{
				htmltext = "30515-02.htm";
			}
			else if(st.getQuestItemsCount(3228) > 0)
			{
				htmltext = "30515-04.htm";
			}
			else
			{
				htmltext = "30515-03.htm";
				st.giveItems(3228, 1);
			}
		}
		else if(event.equalsIgnoreCase("ENKU"))
		{
			if(st.getQuestItemsCount(3212) > 0)
			{
				htmltext = "30515-05.htm";
			}
			else if(st.getQuestItemsCount(3229) > 0)
			{
				htmltext = "30515-07.htm";
			}
			else
			{
				htmltext = "30515-06.htm";
				st.giveItems(3229, 1);
			}
		}
		else if(event.equalsIgnoreCase("VUKU"))
		{
			if(st.getQuestItemsCount(3213) > 0)
			{
				htmltext = "30501-02.htm";
			}
			else if(st.getQuestItemsCount(3230) > 0)
			{
				htmltext = "30501-04.htm";
			}
			else
			{
				htmltext = "30501-03.htm";
				st.giveItems(3230, 1);
			}
		}
		else if(event.equalsIgnoreCase("TUREK"))
		{
			if(st.getQuestItemsCount(3214) > 0)
			{
				htmltext = "30501-05.htm";
			}
			else if(st.getQuestItemsCount(3231) > 0)
			{
				htmltext = "30501-07.htm";
			}
			else
			{
				htmltext = "30501-06.htm";
				st.giveItems(3231, 1);
			}
		}
		else if(event.equalsIgnoreCase("TUNATH"))
		{
			if(st.getQuestItemsCount(3215) > 0)
			{
				htmltext = "30501-08.htm";
			}
			else if(st.getQuestItemsCount(3232) > 0)
			{
				htmltext = "30501-10.htm";
			}
			else
			{
				htmltext = "30501-09.htm";
				st.giveItems(3232, 1);
			}
		}
		else if(event.equalsIgnoreCase("30615-04.htm"))
		{
			boolean spawn = false;
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27080);
			if(isQuest != null)
			{
				spawn = true;
			}
			isQuest = GameObjectsStorage.getByNpcId(27081);
			if(isQuest != null)
			{
				spawn = true;
			}
			if(spawn)
			{
				if(!st.isRunningQuestTimer("Wait1"))
				{
					st.startQuestTimer("Wait1", 300000);
				}
				htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
			}
			else
			{
				st.takeItems(3228, -1);
				st.giveItems(3223, 1);
				st.cancelQuestTimer("Wait1");
				st.startQuestTimer("PashikasSonOfVoltarQuestMonster", 200000);
				st.startQuestTimer("VultusSonOfVoltarQuestMonster", 200000);
				st.addSpawn(27080);
				st.addSpawn(27081);
				st.playSound("Itemsound.quest_before_battle");
			}
		}
		else if(event.equalsIgnoreCase("30616-04.htm"))
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27082);
			if(isQuest != null)
			{
				if(!st.isRunningQuestTimer("Wait2"))
				{
					st.startQuestTimer("Wait2", 300000);
				}
				htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
			}
			else
			{
				st.takeItems(3229, -1);
				st.giveItems(3225, 1);
				st.cancelQuestTimer("Wait2");
				st.startQuestTimer("EnkuOrcOverlordQuestMonster", 200000);
				st.addSpawn(27082);
				st.addSpawn(27082);
				st.addSpawn(27082);
				st.addSpawn(27082);
				st.playSound("Itemsound.quest_before_battle");
			}
		}
		else if(event.equalsIgnoreCase("30617-04.htm"))
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27083);
			if(isQuest != null)
			{
				if(!st.isRunningQuestTimer("Wait3"))
				{
					st.startQuestTimer("Wait3", 300000);
				}
				htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
			}
			else
			{
				st.takeItems(3231, -1);
				st.giveItems(3227, 1);
				st.cancelQuestTimer("Wait3");
				st.startQuestTimer("MakumBugbearThugQuestMonster", 200000);
				st.addSpawn(27083);
				st.addSpawn(27083);
				st.playSound("Itemsound.quest_before_battle");
			}
		}
		else if(event.equalsIgnoreCase("30618-03.htm"))
		{
			st.takeItems(3232, -1);
			st.giveItems(3215, 1);
			if(st.getQuestItemsCount(3211) != 0 && st.getQuestItemsCount(3212) != 0 && st.getQuestItemsCount(3213) != 0 && st.getQuestItemsCount(3214) != 0 && st.getQuestItemsCount(3215) != 0)
			{
				st.setCond(5);
				st.setState(2);
			}
		}
		else if(event.equalsIgnoreCase("30619-03.htm"))
		{
			st.takeItems(3230, -1);
			st.giveItems(3233, 1);
		}
		else if(event.equalsIgnoreCase("Wait1") || event.equalsIgnoreCase("PashikasSonOfVoltarQuestMonster") || event.equalsIgnoreCase("VultusSonOfVoltarQuestMonster"))
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27080);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			if((isQuest = GameObjectsStorage.getByNpcId(27081)) != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait1");
			st.cancelQuestTimer("PashikasSonOfVoltarQuestMonster");
		}
		else if(event.equalsIgnoreCase("Wait2") || event.equalsIgnoreCase("EnkuOrcOverlordQuestMonster"))
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27082);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			if((isQuest = GameObjectsStorage.getByNpcId(27082)) != null)
			{
				isQuest.deleteMe();
			}
			if((isQuest = GameObjectsStorage.getByNpcId(27082)) != null)
			{
				isQuest.deleteMe();
			}
			if((isQuest = GameObjectsStorage.getByNpcId(27082)) != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait2");
			st.cancelQuestTimer("EnkuOrcOverlordQuestMonster");
		}
		else if(event.equalsIgnoreCase("Wait3") || event.equalsIgnoreCase("MakumBugbearThugQuestMonster"))
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27083);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			if((isQuest = GameObjectsStorage.getByNpcId(27083)) != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait3");
			st.cancelQuestTimer("MakumBugbearThugQuestMonster");
		}
		else if(event.equalsIgnoreCase("Wait4") || event.equalsIgnoreCase("RevenantOfTantosChief"))
		{
			NpcInstance isQuest = GameObjectsStorage.getByNpcId(27086);
			if(isQuest != null)
			{
				isQuest.deleteMe();
			}
			st.cancelQuestTimer("Wait4");
			st.cancelQuestTimer("RevenantOfTantosChief");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30514)
		{
			if(st.getQuestItemsCount(3203) != 0)
			{
				htmltext = "completed";
				st.exitCurrentQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getClassId().getId() == 45 || st.getPlayer().getClassId().getId() == 47 || st.getPlayer().getClassId().getId() == 50)
				{
					if(st.getPlayer().getLevel() >= 37)
					{
						htmltext = "30514-03.htm";
					}
					else
					{
						htmltext = "30514-01.htm";
						st.exitCurrentQuest(true);
					}
				}
				else
				{
					htmltext = "30514-02.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
			{
				htmltext = "30514-06.htm";
			}
			else if(cond == 2)
			{
				st.takeItems(3204, -1);
				st.takeItems(3205, -10);
				st.takeItems(3206, -10);
				st.takeItems(3207, -10);
				st.giveItems(3208, 1);
				st.giveItems(3209, 1);
				htmltext = "30514-08.htm";
				st.setCond(3);
				st.setState(2);
			}
			else if(cond == 3)
			{
				htmltext = "30514-09.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30514-10.htm";
			}
		}
		else if(npcId == 30642)
		{
			if(cond == 3)
			{
				htmltext = "30642-01.htm";
			}
			else if(cond == 4)
			{
				htmltext = "30642-04.htm";
			}
			else if(cond == 5)
			{
				if(st.getQuestItemsCount(3210) > 0)
				{
					htmltext = "30642-05.htm";
				}
				else if(st.getQuestItemsCount(3216) > 0)
				{
					if(st.getPlayer().getLevel() >= 38)
					{
						st.takeItems(3216, -1);
						st.giveItems(3217, 1);
						htmltext = "30642-09.htm";
						st.setCond(6);
						st.setState(2);
					}
					else
					{
						htmltext = "30642-08.htm";
					}
				}
			}
			else if(cond == 6)
			{
				htmltext = "30642-10.htm";
			}
			else if(cond == 7)
			{
				st.takeItems(3209, -1);
				st.takeItems(3217, -1);
				st.takeItems(3218, -1);
				st.takeItems(3219, -1);
				st.giveItems(3220, 1);
				htmltext = "30642-11.htm";
				st.setCond(8);
				st.setState(2);
			}
			else if(cond == 8)
			{
				htmltext = "30642-12.htm";
			}
		}
		else if(npcId == 30515)
		{
			if(cond == 4)
			{
				htmltext = "30515-01.htm";
			}
		}
		else if(npcId == 30501)
		{
			if(cond == 4)
			{
				htmltext = "30501-01.htm";
			}
		}
		else if(npcId == 30615)
		{
			if(cond == 4)
			{
				if(st.getQuestItemsCount(3228) > 0)
				{
					htmltext = "30615-02.htm";
				}
				else if(st.getQuestItemsCount(3223) > 0 && (st.getQuestItemsCount(3221) == 0 || st.getQuestItemsCount(3222) == 0))
				{
					htmltext = "30615-05.htm";
					boolean sound = false;
					NpcInstance isQuest = GameObjectsStorage.getByNpcId(27080);
					if(isQuest == null)
					{
						sound = true;
						st.addSpawn(27080);
						st.startQuestTimer("PashikasSonOfVoltarQuestMonster", 200000);
					}
					isQuest = GameObjectsStorage.getByNpcId(27081);
					if(isQuest == null)
					{
						sound = true;
						st.addSpawn(27081);
						st.startQuestTimer("VultusSonOfVoltarQuestMonster", 200000);
					}
					if(sound)
					{
						st.playSound("Itemsound.quest_before_battle");
						st.cancelQuestTimer("Wait1");
					}
					else
					{
						st.startQuestTimer("Wait1", 300000);
						htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
					}
				}
				else if(st.getQuestItemsCount(3221) > 0 && st.getQuestItemsCount(3222) > 0)
				{
					st.takeItems(3221, -1);
					st.takeItems(3222, -1);
					st.takeItems(3223, -1);
					st.giveItems(3211, 1);
					htmltext = "30615-06.htm";
					if(st.getQuestItemsCount(3211) > 0 && st.getQuestItemsCount(3212) > 0 && st.getQuestItemsCount(3213) > 0 && st.getQuestItemsCount(3214) > 0 && st.getQuestItemsCount(3215) > 0)
					{
						st.setCond(5);
						st.setState(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else
				{
					htmltext = st.getQuestItemsCount(3211) > 0 ? "30615-07.htm" : "30615-01.htm";
				}
			}
		}
		else if(npcId == 30616)
		{
			if(cond == 4)
			{
				if(st.getQuestItemsCount(3229) > 0)
				{
					htmltext = "30616-02.htm";
				}
				else if(st.getQuestItemsCount(3225) > 0 && st.getQuestItemsCount(3224) < 4)
				{
					htmltext = "30616-05.htm";
					NpcInstance isQuest = GameObjectsStorage.getByNpcId(27082);
					if(isQuest != null)
					{
						st.startQuestTimer("Wait2", 300000);
						htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
					}
					else
					{
						st.cancelQuestTimer("Wait2");
						st.startQuestTimer("EnkuOrcOverlordQuestMonster", 200000);
						st.addSpawn(27082);
						st.addSpawn(27082);
						st.addSpawn(27082);
						st.addSpawn(27082);
						st.playSound("Itemsound.quest_before_battle");
					}
				}
				else if(st.getQuestItemsCount(3224) >= 4)
				{
					htmltext = "30616-06.htm";
					st.takeItems(3224, -1);
					st.takeItems(3225, -1);
					st.giveItems(3212, 1);
					if(st.getQuestItemsCount(3211) > 0 && st.getQuestItemsCount(3212) > 0 && st.getQuestItemsCount(3213) > 0 && st.getQuestItemsCount(3214) > 0 && st.getQuestItemsCount(3215) > 0)
					{
						st.setCond(5);
						st.setState(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else
				{
					htmltext = st.getQuestItemsCount(3212) > 0 ? "30616-07.htm" : "30616-01.htm";
				}
			}
		}
		else if(npcId == 30617)
		{
			if(cond == 4)
			{
				if(st.getQuestItemsCount(3231) > 0)
				{
					htmltext = "30617-02.htm";
				}
				else if(st.getQuestItemsCount(3227) > 0 && st.getQuestItemsCount(3226) < 2)
				{
					htmltext = "30617-05.htm";
					NpcInstance isQuest = GameObjectsStorage.getByNpcId(27083);
					if(isQuest != null)
					{
						st.startQuestTimer("Wait3", 300000);
						htmltext = "<html><head><body>Please wait 5 minutes</body></html>";
					}
					else
					{
						st.cancelQuestTimer("Wait3");
						st.startQuestTimer("MakumBugbearThugQuestMonster", 200000);
						st.addSpawn(27083);
						st.addSpawn(27083);
						st.playSound("Itemsound.quest_before_battle");
					}
				}
				else if(st.getQuestItemsCount(3226) == 2)
				{
					htmltext = "30617-06.htm";
					st.takeItems(3226, -1);
					st.takeItems(3227, -1);
					st.giveItems(3214, 1);
					if(st.getQuestItemsCount(3211) > 0 && st.getQuestItemsCount(3212) > 0 && st.getQuestItemsCount(3213) > 0 && st.getQuestItemsCount(3214) > 0 && st.getQuestItemsCount(3215) > 0)
					{
						st.setCond(5);
						st.setState(2);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else
				{
					htmltext = st.getQuestItemsCount(3214) > 0 ? "30617-07.htm" : "30617-01.htm";
				}
			}
		}
		else if(npcId == 30618)
		{
			if(cond == 4)
			{
				htmltext = st.getQuestItemsCount(3232) > 0 ? "30618-02.htm" : st.getQuestItemsCount(3215) > 0 ? "30618-04.htm" : "30618-01.htm";
			}
		}
		else if(npcId == 30619)
		{
			if(cond == 4)
			{
				if(st.getQuestItemsCount(3230) > 0)
				{
					htmltext = "30619-02.htm";
				}
				else if(st.getQuestItemsCount(3233) > 0)
				{
					if(st.getQuestItemsCount(3234) >= 30)
					{
						htmltext = "30619-05.htm";
						st.takeItems(3234, -1);
						st.takeItems(3233, -1);
						st.giveItems(3213, 1);
						if(st.getQuestItemsCount(3211) > 0 && st.getQuestItemsCount(3212) > 0 && st.getQuestItemsCount(3213) > 0 && st.getQuestItemsCount(3214) > 0 && st.getQuestItemsCount(3215) > 0)
						{
							st.setCond(5);
							st.setState(2);
							st.playSound("ItemSound.quest_middle");
						}
						else
						{
							st.playSound("ItemSound.quest_itemget");
						}
					}
					else
					{
						htmltext = "30619-04.htm";
					}
				}
				else
				{
					htmltext = st.getQuestItemsCount(3213) > 0 ? "30619-06.htm" : "30619-01.htm";
				}
			}
		}
		else if(npcId == 30571)
		{
			if(cond == 8)
			{
				htmltext = "30571-01.htm";
			}
			else if(cond == 9)
			{
				htmltext = "30571-04.htm";
			}
			else if(cond == 10)
			{
				st.takeItems(3236, -1);
				st.takeItems(3235, -1);
				st.giveItems(3237, 1);
				htmltext = "30571-05.htm";
				st.setCond(11);
				st.setState(2);
			}
			else if(cond == 11)
			{
				htmltext = "30571-06.htm";
			}
		}
		if(npcId == 30565 && cond == 11)
		{
			st.takeItems(3237, -1);
			st.giveItems(3203, 1);
			if(!st.getPlayer().getVarB("prof2.2"))
			{
				st.addExpAndSp(91457, 2500);
				st.getPlayer().setVar("prof2.2", "1", -1);
			}
			htmltext = "30565-02.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
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
		if(cond == 1 && st.getQuestItemsCount(3206) >= 10 && st.getQuestItemsCount(3207) >= 10 && st.getQuestItemsCount(3205) >= 10)
		{
			st.setCond(2);
			st.setState(2);
		}
		else if(cond == 4)
		{
			if(npcId == 27080)
			{
				st.cancelQuestTimer("PashikasSonOfVoltarQuestMonster");
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27080);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				if(st.getQuestItemsCount(3223) > 0 && st.getQuestItemsCount(3221) == 0)
				{
					st.giveItems(3221, 1);
				}
			}
			else if(npcId == 27081)
			{
				st.cancelQuestTimer("VultusSonOfVoltarQuestMonster");
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27081);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				if(st.getQuestItemsCount(3223) > 0 && st.getQuestItemsCount(3222) == 0)
				{
					st.giveItems(3222, 1);
				}
			}
		}
		else if(cond == 6 && st.getQuestItemsCount(3219) >= 20 && st.getQuestItemsCount(3218) >= 20)
		{
			st.setCond(7);
			st.setState(2);
		}
		else if(cond == 9)
		{
			if(npcId == 20778 || npcId == 20779)
			{
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27086);
				if(isQuest == null)
				{
					st.startQuestTimer("RevenantOfTantosChief", 300000);
					st.addSpawn(27086);
					st.playSound("Itemsound.quest_before_battle");
				}
				else if(!st.isRunningQuestTimer("Wait4"))
				{
					st.startQuestTimer("Wait4", 300000);
				}
			}
			else if(npcId == 27086)
			{
				st.cancelQuestTimer("RevenantOfTantosChief");
				st.cancelQuestTimer("Wait4");
				NpcInstance isQuest = GameObjectsStorage.getByNpcId(27086);
				if(isQuest != null)
				{
					isQuest.deleteMe();
				}
				st.giveItems(3236, 1);
				st.setCond(10);
				st.setState(2);
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
}