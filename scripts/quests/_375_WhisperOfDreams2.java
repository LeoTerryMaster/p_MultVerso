package quests;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class _375_WhisperOfDreams2 extends Quest implements ScriptFile
{
	private final int MANAKIA = 30515;
	private final int MSTONE = 5887;
	private final int K_HORN = 5888;
	private final int CH_SKULL = 5889;
	private final int CAVE_HOWLER = 20624;
	private final int KARIK = 20629;
	private final int[] REWARDS = {5348, 5352, 5350};
	private final List<Pair<Integer, Pair<Integer, Integer>>> DROPLIST = Arrays.asList(new Pair[] {Pair.of((Object) CAVE_HOWLER, (Object) Pair.of((Object) CH_SKULL, (Object) 100)), Pair.of((Object) KARIK, (Object) Pair.of((Object) K_HORN, (Object) 100))});
	private final String _default = "noquest";
	
	public _375_WhisperOfDreams2()
	{
		super(1);
		addStartNpc(MANAKIA);
		for(Pair<Integer, Pair<Integer, Integer>> e : DROPLIST)
		{
			addKillId(e.getLeft());
			addQuestItem((Integer) ((Pair) e.getRight()).getKey());
		}
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30515-6.htm"))
		{
			st.takeItems(MSTONE, 1);
			st.setState(2);
			st.setCond(1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30515-7.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = _default;
		int id = st.getState();
		if(id == 1)
		{
			st.setCond(0);
			htmltext = "30515-1.htm";
			if(st.getPlayer().getLevel() < 60)
			{
				htmltext = "30515-2.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getQuestItemsCount(MSTONE) < 1)
			{
				htmltext = "30515-3.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(id == 2)
		{
			boolean enoughItems = true;
			for(Pair<Integer, Pair<Integer, Integer>> e : DROPLIST)
			{
				if(st.getQuestItemsCount(((Integer) ((Pair) e.getRight()).getKey()).intValue()) >= (long) ((Integer) ((Pair) e.getRight()).getValue()).intValue())
					continue;
				enoughItems = false;
				break;
			}
			if(enoughItems)
			{
				st.takeItems(CH_SKULL, -1);
				st.takeItems(K_HORN, -1);
				int item = REWARDS[Rnd.get(REWARDS.length)];
				st.giveItems(item, 1);
				htmltext = "30515-4.htm";
				st.exitCurrentQuest(true);
			}
			else
			{
				htmltext = "30515-5.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcid = npc.getNpcId();
		int itemTypeId = 0;
		int requiredItemCount = 0;
		for(Pair<Integer, Pair<Integer, Integer>> e : DROPLIST)
		{
			if(npcid != e.getLeft())
				continue;
			itemTypeId = (Integer) ((Pair) e.getRight()).getKey();
			requiredItemCount = (Integer) ((Pair) e.getRight()).getValue();
		}
		Player partyMember = (Player) Rnd.get((List) st.getPartyMembers(2, Config.ALT_PARTY_DISTRIBUTION_RANGE, npc));
		QuestState partyMemberQuestState = partyMember.getQuestState(this);
		if(partyMemberQuestState == null)
		{
			return null;
		}
		long actualItemCount = partyMemberQuestState.getQuestItemsCount(itemTypeId);
		if(actualItemCount < (long) requiredItemCount)
		{
			int rewardItemCount = 1;
			if(actualItemCount + 1 < (long) requiredItemCount && Rnd.chance(20))
			{
				rewardItemCount = 2;
			}
			partyMemberQuestState.giveItems(itemTypeId, (long) rewardItemCount, true);
			partyMemberQuestState.playSound("ItemSound.quest_itemget");
		}
		else if(actualItemCount >= (long) requiredItemCount)
		{
			partyMemberQuestState.playSound("ItemSound.quest_middle");
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