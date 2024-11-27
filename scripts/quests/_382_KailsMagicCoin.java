package quests;

import l2.commons.util.Rnd;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class _382_KailsMagicCoin extends Quest implements ScriptFile
{
	private static final int ROYAL_MEMBERSHIP = 5898;
	private static final int VERGARA = 30687;
	private static final Map<Integer, int[]> MOBS = new HashMap<>();
	
	static
	{
		MOBS.put(21017, new int[] {5961});
		MOBS.put(21019, new int[] {5962});
		MOBS.put(21020, new int[] {5963});
		MOBS.put(21022, new int[] {5961, 5962, 5963});
	}
	
	public _382_KailsMagicCoin()
	{
		super(false);
		addStartNpc(VERGARA);
		Iterator<Integer> iterator = MOBS.keySet().iterator();
		while(iterator.hasNext())
		{
			int mobId = iterator.next();
			addKillId(mobId);
		}
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
		if(event.equalsIgnoreCase("head_blacksmith_vergara_q0382_03.htm"))
		{
			if(st.getPlayer().getLevel() >= 55 && st.getQuestItemsCount(ROYAL_MEMBERSHIP) > 0)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "head_blacksmith_vergara_q0382_01.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("list"))
		{
			MultiSellHolder.getInstance().SeparateAndSend(382, st.getPlayer(), 0.0);
			htmltext = null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		int cond = st.getCond();
		if(st.getQuestItemsCount(ROYAL_MEMBERSHIP) == 0 || st.getPlayer().getLevel() < 55)
		{
			htmltext = "head_blacksmith_vergara_q0382_01.htm";
			st.exitCurrentQuest(true);
		}
		else
		{
			htmltext = cond == 0 ? "head_blacksmith_vergara_q0382_02.htm" : "head_blacksmith_vergara_q0382_04.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != 2 || st.getQuestItemsCount(ROYAL_MEMBERSHIP) == 0)
		{
			return null;
		}
		int[] droplist = MOBS.get(npc.getNpcId());
		st.rollAndGive(droplist[Rnd.get(droplist.length)], 1, 10.0);
		return null;
	}
}