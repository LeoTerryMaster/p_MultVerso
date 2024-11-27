package quests;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;

public class _635_InTheDimensionalRift extends Quest implements ScriptFile
{
	private static final int DIMENSION_FRAGMENT = 7079;
	private static final int[][] COORD = {new int[0], {-41572, 209731, -5087}, {42950, 143934, -5381}, {45256, 123906, -5411}, {46192, 170290, -4981}, {111273, 174015, -5437}, {-20221, -250795, -8160}, {-21726, 77385, -5171}, {140405, 79679, -5427}, {-52366, 79097, -4741}, {118311, 132797, -4829}, {172185, -17602, -4901}, {83000, 209213, -5439}, {-19500, 13508, -4901}, {113865, 84543, -6541}};
	
	public _635_InTheDimensionalRift()
	{
		super(false);
		int npcId;
		for(npcId = 31494;npcId < 31508;++npcId)
		{
			addStartNpc(npcId);
		}
		for(npcId = 31095;npcId <= 31126;++npcId)
		{
			if(npcId == 31111 || npcId == 31112 || npcId == 31113)
				continue;
			addStartNpc(npcId);
		}
		npcId = 31488;
		while(npcId < 31494)
		{
			addTalkId(npcId++);
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
		int id = st.getInt("id");
		String loc = st.get("loc");
		if(event.equals("5.htm"))
		{
			if(id > 0 || loc != null)
			{
				if(isZiggurat(st.getPlayer().getLastNpc().getNpcId()) && !takeAdena(st))
				{
					htmltext = "Sorry...";
					st.exitCurrentQuest(true);
					return htmltext;
				}
				st.setState(2);
				st.setCond(1);
				st.getPlayer().teleToLocation(-114790, -180576, -6781);
			}
			else
			{
				htmltext = "What are you trying to do?";
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("6.htm"))
		{
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		int npcId = npc.getNpcId();
		int id = st.getInt("id");
		String loc = st.get("loc");
		if(isZiggurat(npcId) || isKeeper(npcId))
		{
			if(st.getPlayer().getLevel() < 20)
			{
				st.exitCurrentQuest(true);
				htmltext = "1.htm";
			}
			else if(st.getQuestItemsCount(7079) == 0)
			{
				htmltext = isKeeper(npcId) ? "3.htm" : "3-ziggurat.htm";
			}
			else
			{
				st.set("loc", st.getPlayer().getLoc().toString());
				htmltext = isKeeper(npcId) ? "4.htm" : "4-ziggurat.htm";
			}
		}
		else if(id > 0)
		{
			int[] coord = COORD[id];
			st.getPlayer().teleToLocation(coord[0], coord[1], coord[2]);
			htmltext = "7.htm";
			st.exitCurrentQuest(true);
		}
		else if(loc != null)
		{
			st.getPlayer().teleToLocation(Location.parseLoc(loc));
			htmltext = "7.htm";
			st.exitCurrentQuest(true);
		}
		else
		{
			htmltext = "Where are you from?";
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}
	
	private boolean takeAdena(QuestState st)
	{
		int level = st.getPlayer().getLevel();
		int fee = level < 30 ? 2000 : level < 40 ? 4500 : level < 50 ? 8000 : level < 60 ? 12500 : level < 70 ? 18000 : 24500;
		if(!st.getPlayer().reduceAdena((long) fee, true))
		{
			st.getPlayer().sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return false;
		}
		return true;
	}
	
	private boolean isZiggurat(int id)
	{
		return id >= 31095 && id <= 31126;
	}
	
	private boolean isKeeper(int id)
	{
		return id >= 31494 && id <= 31508;
	}
}