package quests;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

public class _306_CrystalOfFireice extends Quest implements ScriptFile
{
	private static final int Katerina = 30004;
	private static final int Salamander = 20109;
	private static final int Undine = 20110;
	private static final int Salamander_Elder = 20112;
	private static final int Undine_Elder = 20113;
	private static final int Salamander_Noble = 20114;
	private static final int Undine_Noble = 20115;
	private static final int Flame_Shard = 1020;
	private static final int Ice_Shard = 1021;
	private static final int Chance = 30;
	private static final int Elder_Chance = 40;
	private static final int Noble_Chance = 50;
	
	public _306_CrystalOfFireice()
	{
		super(false);
		addStartNpc(Katerina);
		addKillId(Salamander);
		addKillId(Undine);
		addKillId(Salamander_Elder);
		addKillId(Undine_Elder);
		addKillId(Salamander_Noble);
		addKillId(Undine_Noble);
		addQuestItem(Flame_Shard);
		addQuestItem(Ice_Shard);
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("katrine_q0306_04.htm") && _state == 1)
		{
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("katrine_q0306_08.htm") && _state == 2)
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != Katerina)
		{
			return htmltext;
		}
		int _state = st.getState();
		if(_state == 1)
		{
			if(st.getPlayer().getLevel() < 17)
			{
				htmltext = "katrine_q0306_02.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "katrine_q0306_03.htm";
				st.setCond(0);
			}
		}
		else if(_state == 2)
		{
			long Shrads_count = st.getQuestItemsCount(Flame_Shard) + st.getQuestItemsCount(Ice_Shard);
			long Reward = Shrads_count * 30L + (long) (Shrads_count >= 10L ? 5000 : 0);
			if(Reward > 0L)
			{
				htmltext = "katrine_q0306_07.htm";
				st.takeItems(Flame_Shard, -1L);
				st.takeItems(Ice_Shard, -1L);
				st.giveItems(57, Reward);
			}
			else
			{
				htmltext = "katrine_q0306_05.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != 2)
		{
			return null;
		}
		int npcId = npc.getNpcId();
		if(!(npcId != Salamander && npcId != Undine || Rnd.chance(Chance)))
		{
			return null;
		}
		if(!(npcId != Salamander_Elder && npcId != Undine_Elder || Rnd.chance(Elder_Chance)))
		{
			return null;
		}
		if(!(npcId != Salamander_Noble && npcId != Undine_Noble || Rnd.chance(Noble_Chance)))
		{
			return null;
		}
		qs.giveItems(npcId == Salamander || npcId == Salamander_Elder || npcId == Salamander_Noble ? Flame_Shard : Ice_Shard, 1);
		qs.playSound("ItemSound.quest_itemget");
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