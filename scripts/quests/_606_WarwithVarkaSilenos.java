package quests;

import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _606_WarwithVarkaSilenos extends Quest implements ScriptFile
{
	private static final int KADUN_ZU_KETRA = 31370;
	private static final int VARKAS_MANE = 7233;
	private static final int VARKAS_MANE_DROP_CHANCE = 80;
	private static final int HORN_OF_BUFFALO = 7186;
	private static final int[] VARKA_NPC_LIST = new int[20];
	
	public _606_WarwithVarkaSilenos()
	{
		super(true);
		addStartNpc(31370);
		VARKA_NPC_LIST[0] = 21350;
		VARKA_NPC_LIST[1] = 21351;
		VARKA_NPC_LIST[2] = 21353;
		VARKA_NPC_LIST[3] = 21354;
		VARKA_NPC_LIST[4] = 21355;
		VARKA_NPC_LIST[5] = 21357;
		VARKA_NPC_LIST[6] = 21358;
		VARKA_NPC_LIST[7] = 21360;
		VARKA_NPC_LIST[8] = 21361;
		VARKA_NPC_LIST[9] = 21362;
		VARKA_NPC_LIST[10] = 21364;
		VARKA_NPC_LIST[11] = 21365;
		VARKA_NPC_LIST[12] = 21366;
		VARKA_NPC_LIST[13] = 21368;
		VARKA_NPC_LIST[14] = 21369;
		VARKA_NPC_LIST[15] = 21370;
		VARKA_NPC_LIST[16] = 21371;
		VARKA_NPC_LIST[17] = 21372;
		VARKA_NPC_LIST[18] = 21373;
		VARKA_NPC_LIST[19] = 21374;
		addKillId(VARKA_NPC_LIST);
		addQuestItem(7233);
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
		if(event.equals("quest_accept"))
		{
			htmltext = "elder_kadun_zu_ketra_q0606_0104.htm";
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equals("606_3"))
		{
			long ec = st.getQuestItemsCount(7233) / 5;
			if(ec > 0)
			{
				htmltext = "elder_kadun_zu_ketra_q0606_0202.htm";
				st.takeItems(7233, ec * 5);
				st.giveItems(7186, ec);
			}
			else
			{
				htmltext = "elder_kadun_zu_ketra_q0606_0203.htm";
			}
		}
		else if(event.equals("606_4"))
		{
			htmltext = "elder_kadun_zu_ketra_q0606_0204.htm";
			st.takeItems(7233, -1);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 74)
			{
				htmltext = "elder_kadun_zu_ketra_q0606_0101.htm";
			}
			else
			{
				htmltext = "elder_kadun_zu_ketra_q0606_0103.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1 && st.getQuestItemsCount(7233) == 0)
		{
			htmltext = "elder_kadun_zu_ketra_q0606_0106.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(7233) > 0)
		{
			htmltext = "elder_kadun_zu_ketra_q0606_0105.htm";
		}
		return htmltext;
	}
	
	public boolean isVarkaNpc(int npc)
	{
		for(int i : VARKA_NPC_LIST)
		{
			if(npc != i)
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(isVarkaNpc(npc.getNpcId()) && st.getCond() == 1)
		{
			st.rollAndGive(7233, 1, 80.0);
		}
		return null;
	}
}