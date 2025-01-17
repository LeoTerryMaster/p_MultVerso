package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _345_MethodToRaiseTheDead extends Quest implements ScriptFile
{
	int VICTIMS_ARM_BONE = 4274;
	int VICTIMS_THIGH_BONE = 4275;
	int VICTIMS_SKULL = 4276;
	int VICTIMS_RIB_BONE = 4277;
	int VICTIMS_SPINE = 4278;
	int USELESS_BONE_PIECES = 4280;
	int POWDER_TO_SUMMON_DEAD_SOULS = 4281;
	int BILL_OF_IASON_HEINE = 4310;
	int CHANCE = 15;
	int CHANCE2 = 50;
	
	public _345_MethodToRaiseTheDead()
	{
		super(false);
		addStartNpc(30970);
		addTalkId(30970);
		addTalkId(30970);
		addTalkId(30912);
		addTalkId(30973);
		addQuestItem(VICTIMS_ARM_BONE, VICTIMS_THIGH_BONE, VICTIMS_SKULL, VICTIMS_RIB_BONE, VICTIMS_SPINE, POWDER_TO_SUMMON_DEAD_SOULS);
		addKillId(20789);
		addKillId(20791);
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
		if(event.equals("1"))
		{
			st.setCond(1);
			st.setState(2);
			htmltext = "dorothy_the_locksmith_q0345_03.htm";
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("2"))
		{
			st.setCond(2);
			htmltext = "dorothy_the_locksmith_q0345_07.htm";
		}
		else if(event.equals("3"))
		{
			if(st.getQuestItemsCount(57) >= 1000)
			{
				st.takeItems(57, 1000);
				st.giveItems(POWDER_TO_SUMMON_DEAD_SOULS, 1);
				st.setCond(3);
				htmltext = "magister_xenovia_q0345_03.htm";
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				htmltext = "<html><head><body>You dont have enough adena!</body></html>";
			}
		}
		else if(event.equals("4"))
		{
			htmltext = "medium_jar_q0345_07.htm";
			st.takeItems(POWDER_TO_SUMMON_DEAD_SOULS, -1);
			st.takeItems(VICTIMS_ARM_BONE, -1);
			st.takeItems(VICTIMS_THIGH_BONE, -1);
			st.takeItems(VICTIMS_SKULL, -1);
			st.takeItems(VICTIMS_RIB_BONE, -1);
			st.takeItems(VICTIMS_SPINE, -1);
			st.setCond(6);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int id = st.getState();
		int level = st.getPlayer().getLevel();
		int cond = st.getCond();
		long amount = st.getQuestItemsCount(USELESS_BONE_PIECES);
		if(npcId == 30970)
		{
			if(id == 1)
			{
				if(level >= 35)
				{
					htmltext = "dorothy_the_locksmith_q0345_02.htm";
				}
				else
				{
					htmltext = "dorothy_the_locksmith_q0345_01.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1 && st.getQuestItemsCount(VICTIMS_ARM_BONE) > 0 && st.getQuestItemsCount(VICTIMS_THIGH_BONE) > 0 && st.getQuestItemsCount(VICTIMS_SKULL) > 0 && st.getQuestItemsCount(VICTIMS_RIB_BONE) > 0 && st.getQuestItemsCount(VICTIMS_SPINE) > 0)
			{
				htmltext = "dorothy_the_locksmith_q0345_06.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(VICTIMS_ARM_BONE) + st.getQuestItemsCount(VICTIMS_THIGH_BONE) + st.getQuestItemsCount(VICTIMS_SKULL) + st.getQuestItemsCount(VICTIMS_RIB_BONE) + st.getQuestItemsCount(VICTIMS_SPINE) < 5)
			{
				htmltext = "dorothy_the_locksmith_q0345_05.htm";
			}
			else if(cond == 7)
			{
				htmltext = "dorothy_the_locksmith_q0345_14.htm";
				st.setCond(1);
				st.giveItems(57, amount * 238);
				st.giveItems(BILL_OF_IASON_HEINE, (long) (Rnd.get(7) + 1));
				st.takeItems(USELESS_BONE_PIECES, -1);
			}
		}
		if(npcId == 30912)
		{
			if(cond == 2)
			{
				htmltext = "magister_xenovia_q0345_01.htm";
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond == 3)
			{
				htmltext = "<html><head><body>What did the urn say?</body></html>";
			}
			else if(cond == 6)
			{
				htmltext = "magister_xenovia_q0345_07.htm";
				st.setCond(7);
			}
		}
		if(npcId == 30973 && cond == 3)
		{
			htmltext = "medium_jar_q0345_01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int random = Rnd.get(100);
		if(random <= CHANCE)
		{
			if(st.getQuestItemsCount(VICTIMS_ARM_BONE) == 0)
			{
				st.giveItems(VICTIMS_ARM_BONE, 1);
			}
			else if(st.getQuestItemsCount(VICTIMS_THIGH_BONE) == 0)
			{
				st.giveItems(VICTIMS_THIGH_BONE, 1);
			}
			else if(st.getQuestItemsCount(VICTIMS_SKULL) == 0)
			{
				st.giveItems(VICTIMS_SKULL, 1);
			}
			else if(st.getQuestItemsCount(VICTIMS_RIB_BONE) == 0)
			{
				st.giveItems(VICTIMS_RIB_BONE, 1);
			}
			else if(st.getQuestItemsCount(VICTIMS_SPINE) == 0)
			{
				st.giveItems(VICTIMS_SPINE, 1);
			}
		}
		if(random <= CHANCE2)
		{
			st.giveItems(USELESS_BONE_PIECES, (long) (Rnd.get(8) + 1));
		}
		return null;
	}
}