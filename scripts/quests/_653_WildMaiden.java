package quests;

import l2.gameserver.instancemanager.SpawnManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.spawn.PeriodOfDay;

import java.util.ArrayList;

public class _653_WildMaiden extends Quest implements ScriptFile
{
	public final int SUKI = 32013;
	public final int GALIBREDO = 30181;
	public final int SOE = 736;
	
	public _653_WildMaiden()
	{
		super(false);
		addStartNpc(32013);
		addTalkId(32013);
		addTalkId(30181);
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
	
	private NpcInstance findNpc(int npcId, Player player)
	{
		NpcInstance instance = null;
		ArrayList<NpcInstance> npclist = new ArrayList<>();
		for(Spawner spawn : SpawnManager.getInstance().getSpawners(PeriodOfDay.ALL.name()))
		{
			if(spawn.getCurrentNpcId() != npcId)
				continue;
			instance = spawn.getLastSpawn();
			npclist.add(instance);
		}
		for(NpcInstance npc : npclist)
		{
			if(!player.isInRange(npc, 1600))
				continue;
			return npc;
		}
		return instance;
	}
	
	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Player player = st.getPlayer();
		if(event.equalsIgnoreCase("spring_girl_sooki_q0653_03.htm"))
		{
			if(st.getQuestItemsCount(736) > 0)
			{
				st.setCond(1);
				st.setState(2);
				st.playSound("ItemSound.quest_accept");
				st.takeItems(736, 1);
				htmltext = "spring_girl_sooki_q0653_04a.htm";
				NpcInstance n = findNpc(32013, player);
				n.broadcastPacket(new MagicSkillUse(n, n, 2013, 1, 20000, 0));
				st.startQuestTimer("suki_timer", 20000);
			}
		}
		else if(event.equalsIgnoreCase("spring_girl_sooki_q0653_03.htm"))
		{
			st.exitCurrentQuest(false);
			st.playSound("ItemSound.quest_giveup");
		}
		else if(event.equalsIgnoreCase("suki_timer"))
		{
			NpcInstance n = findNpc(32013, player);
			n.deleteMe();
			htmltext = null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		if(npcId == 32013 && id == 1)
		{
			if(st.getPlayer().getLevel() >= 36)
			{
				htmltext = "spring_girl_sooki_q0653_01.htm";
			}
			else
			{
				htmltext = "spring_girl_sooki_q0653_01a.htm";
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == 30181 && st.getCond() == 1)
		{
			htmltext = "galicbredo_q0653_01.htm";
			st.giveItems(57, 2553);
			st.playSound("ItemSound.quest_finish");
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}
}