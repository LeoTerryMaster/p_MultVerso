package quests;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.scripts.ScriptFile;

public class _111_ElrokianHuntersProof extends Quest implements ScriptFile
{
	private static final int Marquez = 32113;
	private static final int Asamah = 32115;
	private static final int Kirikachin = 32116;
	private static final int[] Velociraptor = {22196, 22197, 22198, 22218, 22223};
	private static final int[] Ornithomimus = {22200, 22201, 22202, 22219, 22224};
	private static final int[] Deinonychus = {22203, 22204, 22205, 22220, 22225};
	private static final int[] Pachycephalosaurus = {22208, 22209, 22210, 22221, 22226};
	private static final int DiaryFragment = 8768;
	private static final int OrnithomimusClaw = 8770;
	private static final int DeinonychusBone = 8771;
	private static final int PachycephalosaurusSkin = 8772;
	private static final int ElrokianTrap = 8763;
	private static final int TrapStone = 8764;
	
	public _111_ElrokianHuntersProof()
	{
		super(true);
		addStartNpc(32113);
		addTalkId(32115);
		addTalkId(32116);
		addKillId(Velociraptor);
		addKillId(Ornithomimus);
		addKillId(Deinonychus);
		addKillId(Pachycephalosaurus);
		addQuestItem(8768, 8770, 8771, 8772);
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
		int cond = st.getCond();
		Player player = st.getPlayer();
		if(event.equalsIgnoreCase("marquez_q111_2.htm") && cond == 0)
		{
			st.setCond(2);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("asamah_q111_2.htm"))
		{
			st.setCond(3);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("marquez_q111_4.htm"))
		{
			st.setCond(4);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("marquez_q111_6.htm"))
		{
			st.setCond(6);
			st.takeItems(8768, -1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("kirikachin_q111_2.htm"))
		{
			st.setCond(7);
			player.sendPacket(new PlaySound("EtcSound.elcroki_song_full"));
		}
		else if(event.equalsIgnoreCase("kirikachin_q111_3.htm"))
		{
			st.setCond(8);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("asamah_q111_4.htm"))
		{
			st.setCond(9);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("asamah_q111_5.htm"))
		{
			st.setCond(10);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("asamah_q111_7.htm"))
		{
			st.takeItems(8770, -1);
			st.takeItems(8771, -1);
			st.takeItems(8772, -1);
			st.setCond(12);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("asamah_q111_8.htm"))
		{
			st.giveItems(57, 1022636);
			st.giveItems(8763, 1);
			st.giveItems(8764, 100);
			st.setState(3);
			st.exitCurrentQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		String htmltext = event;
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 32113)
		{
			if(st.getPlayer().getLevel() >= 75 && cond == 0)
			{
				htmltext = "marquez_q111_1.htm";
			}
			else if(st.getPlayer().getLevel() < 75 && cond == 0)
			{
				htmltext = "marquez_q111_0.htm";
			}
			else if(cond == 3)
			{
				htmltext = "marquez_q111_3.htm";
			}
			else if(cond == 5)
			{
				htmltext = "marquez_q111_5.htm";
			}
		}
		else if(npcId == 32115)
		{
			if(cond == 2)
			{
				htmltext = "asamah_q111_1.htm";
			}
			else if(cond == 8)
			{
				htmltext = "asamah_q111_3.htm";
			}
			else if(cond == 11)
			{
				htmltext = "asamah_q111_6.htm";
			}
		}
		else if(npcId == 32116 && cond == 6)
		{
			htmltext = "kirikachin_q111_1.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int id = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 4)
		{
			for(int i : Velociraptor)
			{
				if(id != i || st.getQuestItemsCount(8768) >= 50)
					continue;
				st.giveItems(8768, 1, false);
				if(st.getQuestItemsCount(8768) == 50)
				{
					st.playSound("ItemSound.quest_middle");
					st.setCond(5);
					return null;
				}
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(cond == 10)
		{
			for(int i : Ornithomimus)
			{
				if(id != i || st.getQuestItemsCount(8770) >= 10)
					continue;
				st.giveItems(8770, 1, false);
				return null;
			}
			for(int i : Deinonychus)
			{
				if(id != i || st.getQuestItemsCount(8771) >= 10)
					continue;
				st.giveItems(8771, 1, false);
				return null;
			}
			for(int i : Pachycephalosaurus)
			{
				if(id != i || st.getQuestItemsCount(8772) >= 10)
					continue;
				st.giveItems(8772, 1, false);
				return null;
			}
			if(st.getQuestItemsCount(8770) >= 10 && st.getQuestItemsCount(8771) >= 10 && st.getQuestItemsCount(8772) >= 10)
			{
				st.setCond(11);
				st.playSound("ItemSound.quest_middle");
				return null;
			}
		}
		return null;
	}
}