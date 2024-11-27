package quests;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.data.xml.holder.SoulCrystalHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.SoulCrystal;
import l2.gameserver.templates.npc.AbsorbInfo;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class _350_EnhanceYourWeapon extends Quest implements ScriptFile
{
	private static final int RED_SOUL_CRYSTAL0_ID = 4629;
	private static final int GREEN_SOUL_CRYSTAL0_ID = 4640;
	private static final int BLUE_SOUL_CRYSTAL0_ID = 4651;
	private static final int Jurek = 30115;
	private static final int Gideon = 30194;
	private static final int Winonin = 30856;
	
	public _350_EnhanceYourWeapon()
	{
		super(false);
		addStartNpc(30115);
		addStartNpc(30194);
		addStartNpc(30856);
		for(NpcTemplate template : NpcHolder.getInstance().getAll())
		{
			if(template == null || template.getAbsorbInfo().isEmpty())
				continue;
			addKillId(template.npcId);
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
		if(event.equalsIgnoreCase("30115-04.htm") || event.equalsIgnoreCase("30194-04.htm") || event.equalsIgnoreCase("30856-04.htm"))
		{
			st.setCond(1);
			st.setState(2);
			st.playSound("ItemSound.quest_accept");
		}
		if(event.equalsIgnoreCase("30115-09.htm") || event.equalsIgnoreCase("30194-09.htm") || event.equalsIgnoreCase("30856-09.htm"))
		{
			st.giveItems(4629, 1);
		}
		if(event.equalsIgnoreCase("30115-10.htm") || event.equalsIgnoreCase("30194-10.htm") || event.equalsIgnoreCase("30856-10.htm"))
		{
			st.giveItems(4640, 1);
		}
		if(event.equalsIgnoreCase("30115-11.htm") || event.equalsIgnoreCase("30194-11.htm") || event.equalsIgnoreCase("30856-11.htm"))
		{
			st.giveItems(4651, 1);
		}
		if(event.equalsIgnoreCase("exit.htm"))
		{
			st.exitCurrentQuest(true);
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String npcId = str((long) npc.getNpcId());
		String htmltext;
		int id = st.getState();
		if(st.getQuestItemsCount(4629) == 0 && st.getQuestItemsCount(4640) == 0 && st.getQuestItemsCount(4651) == 0)
		{
			htmltext = id == 1 ? npcId + "-01.htm" : npcId + "-21.htm";
		}
		else
		{
			if(id == 1)
			{
				st.setCond(1);
				st.setState(2);
			}
			htmltext = npcId + "-03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		Player player = qs.getPlayer();
		if(player == null || !npc.isMonster())
		{
			return null;
		}
		ArrayList<PlayerResult> list;
		if(player.getParty() == null)
		{
			list = new ArrayList<>(1);
			list.add(new PlayerResult(player));
		}
		else
		{
			list = new ArrayList(player.getParty().getMemberCount());
			list.add(new PlayerResult(player));
			for(Player m : player.getParty().getPartyMembers())
			{
				if(m == player || !m.isInRange(npc.getLoc(), (long) Config.ALT_PARTY_DISTRIBUTION_RANGE))
					continue;
				list.add(new PlayerResult(m));
			}
		}
		for(AbsorbInfo info : npc.getTemplate().getAbsorbInfo())
		{
			calcAbsorb(list, (MonsterInstance) npc, info);
		}
		for(PlayerResult r : list)
		{
			r.send();
		}
		return null;
	}
	
	private void calcAbsorb(List<PlayerResult> players, MonsterInstance npc, AbsorbInfo info)
	{
		List<PlayerResult> targets;
		int memberSize;
		switch(info.getAbsorbType())
		{
			case LAST_HIT:
			{
				targets = Collections.singletonList(players.get(0));
				break;
			}
			case PARTY_ALL:
			{
				targets = players;
				break;
			}
			case PARTY_RANDOM:
			{
				memberSize = players.size();
				if(memberSize == 1)
				{
					targets = Collections.singletonList(players.get(0));
					break;
				}
				int size = Rnd.get(memberSize);
				targets = new ArrayList<>(size);
				ArrayList<PlayerResult> temp = new ArrayList<>(players);
				Collections.shuffle(temp);
				for(int i = 0;i < size;++i)
				{
					targets.add(temp.get(i));
				}
				break;
			}
			case PARTY_ONE:
			{
				memberSize = players.size();
				if(memberSize == 1)
				{
					targets = Collections.singletonList(players.get(0));
					break;
				}
				int rnd = Rnd.get(memberSize);
				targets = Collections.singletonList(players.get(rnd));
				break;
			}
			default:
			{
				return;
			}
		}
		for(PlayerResult target : targets)
		{
			if(target == null || target.getMessage() == SystemMsg.THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL)
				continue;
			Player targetPlayer = target.getPlayer();
			if(info.isSkill() && !npc.isAbsorbed(targetPlayer) || targetPlayer.getQuestState(_350_EnhanceYourWeapon.class) == null)
				continue;
			boolean resonate = false;
			SoulCrystal soulCrystal = null;
			ItemInstance[] items = targetPlayer.getInventory().getItems();
			for(ItemInstance item : items)
			{
				SoulCrystal crystal = SoulCrystalHolder.getInstance().getCrystal(item.getItemId());
				if(crystal == null)
					continue;
				target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL);
				if(soulCrystal != null)
				{
					target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL);
					resonate = true;
					break;
				}
				soulCrystal = crystal;
			}
			if(resonate || soulCrystal == null)
				continue;
			if(!info.canAbsorb(soulCrystal.getLevel() + 1))
			{
				target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_THE_SOUL);
				continue;
			}
			int nextItemId = 0;
			if(info.getCursedChance() > 0 && soulCrystal.getCursedNextItemId() > 0)
			{
				int n = nextItemId = Rnd.chance(info.getCursedChance()) ? soulCrystal.getCursedNextItemId() : 0;
			}
			if(nextItemId == 0)
			{
				int n = nextItemId = Rnd.chance(info.getChance()) ? soulCrystal.getNextItemId() : 0;
			}
			if(nextItemId == 0)
			{
				target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL);
				continue;
			}
			if(targetPlayer.consumeItem(soulCrystal.getItemId(), 1))
			{
				targetPlayer.getInventory().addItem(nextItemId, 1);
				targetPlayer.sendPacket(SystemMessage2.obtainItems(nextItemId, (long) 1, 0));
				target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL);
				continue;
			}
			target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL);
		}
	}
	
	private static class PlayerResult
	{
		private final Player _player;
		private SystemMsg _message;
		
		public PlayerResult(Player player)
		{
			_player = player;
		}
		
		public Player getPlayer()
		{
			return _player;
		}
		
		public SystemMsg getMessage()
		{
			return _message;
		}
		
		public void setMessage(SystemMsg message)
		{
			_message = message;
		}
		
		public void send()
		{
			if(_message != null)
			{
				_player.sendPacket(_message);
			}
		}
	}
}