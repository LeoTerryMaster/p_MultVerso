package quests;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _646_SignsOfRevolt extends Quest implements ScriptFile
{
	private static final int TORRANT = 32016;
	private static final int Ragna_Orc = 22029;
	private static final int Ragna_Orc_Sorcerer = 22044;
	private static final int Guardian_of_the_Ghost_Town = 22047;
	private static final int Varangkas_Succubus = 22049;
	private static final int Steel = 1880;
	private static final int Coarse_Bone_Powder = 1881;
	private static final int Leather = 1882;
	private static final int CURSED_DOLL = 8087;
	private static final int CURSED_DOLL_Chance = 75;
	
	public _646_SignsOfRevolt()
	{
		super(false);
		addStartNpc(TORRANT);
		int Ragna_Orc_id = Ragna_Orc;
		while(Ragna_Orc_id <= Ragna_Orc_Sorcerer)
		{
			addKillId(Ragna_Orc_id++);
		}
		addKillId(Guardian_of_the_Ghost_Town);
		addKillId(Varangkas_Succubus);
		addQuestItem(CURSED_DOLL);
	}
	
	private static String doReward(QuestState st, int reward_id, int _count)
	{
		if(st.getQuestItemsCount(CURSED_DOLL) < 180)
		{
			return null;
		}
		st.takeItems(CURSED_DOLL, -1);
		st.giveItems(reward_id, (long) _count, true);
		st.playSound("ItemSound.quest_finish");
		st.exitCurrentQuest(true);
		return "torant_q0646_0202.htm";
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("torant_q0646_0103.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else
		{
			if(event.equalsIgnoreCase("reward_adena") && _state == 2)
			{
				return doReward(st, 57, 21600);
			}
			if(event.equalsIgnoreCase("reward_cbp") && _state == 2)
			{
				return doReward(st, Coarse_Bone_Powder, 12);
			}
			if(event.equalsIgnoreCase("reward_steel") && _state == 2)
			{
				return doReward(st, Steel, 9);
			}
			if(event.equalsIgnoreCase("reward_leather") && _state == 2)
			{
				return doReward(st, Leather, 20);
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != TORRANT)
		{
			return htmltext;
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getLevel() < 40)
			{
				htmltext = "torant_q0646_0102.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "torant_q0646_0101.htm";
				st.setCond(0);
			}
		}
		else if(_state == 2)
		{
			htmltext = st.getQuestItemsCount(CURSED_DOLL) >= 180 ? "torant_q0646_0105.htm" : "torant_q0646_0106.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		Player player = qs.getRandomPartyMember(2, Config.ALT_PARTY_DISTRIBUTION_RANGE);
		if(player == null)
		{
			return null;
		}
		QuestState st = player.getQuestState(qs.getQuest().getName());
		long CURSED_DOLL_COUNT = st.getQuestItemsCount(CURSED_DOLL);
		if(CURSED_DOLL_COUNT < 180 && Rnd.chance(CURSED_DOLL_Chance))
		{
			st.giveItems(CURSED_DOLL, 1);
			if(CURSED_DOLL_COUNT == 179)
			{
				st.playSound("ItemSound.quest_middle");
				st.setCond(2);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
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
}